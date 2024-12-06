package org.kkb.module;

import org.kkb.config.FileProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class FileSplitter {

    @Autowired
    private FileProperties fileProperties;
    private final String DELIMITER  = "_";
    public List<File> splitFile(String filePath, int chunkSize, boolean hasHeader) throws IOException {
        List<File> splitFiles = new ArrayList<>();
        Path path = Paths.get(filePath);


        String uniqueFileName = getUniqueFileName(path);
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            List<String> buffer = new ArrayList<>();
            int chunkNumber = 0;
            String header = null;
            if(hasHeader) {
                header = reader.readLine();
                chunkSize+=1;
            }
            // 파일명 구조: {원본파일명}_{최종수정일자}_{파일크기}_{청크번호}.csv
            while ((line = reader.readLine()) != null) {
                if(hasHeader && buffer.isEmpty()){
                    buffer.add(header);
                }
                buffer.add(line);
                if (buffer.size() == chunkSize) {
                    File splitFile = writeChunkToFile(buffer, uniqueFileName+DELIMITER+chunkNumber+".csv", fileProperties.getReady());
                    splitFiles.add(splitFile);
                    chunkNumber++;
                    buffer.clear();
                }
            }

            if (!buffer.isEmpty()) {
                File splitFile = writeChunkToFile(buffer, uniqueFileName+DELIMITER+chunkNumber+".csv", fileProperties.getReady());
                splitFiles.add(splitFile);
            }
            Path destinationDir = Paths.get(fileProperties.getBackup());
            Path destination =  destinationDir.resolve(path.getFileName());
            Files.move(path, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        return splitFiles;
    }

    private File writeChunkToFile(List<String> lines, String uniqueFileName, String destination) throws IOException {
        Path splitFilePath = Paths.get(destination, uniqueFileName);

        // 디렉토리 생성 (존재하지 않는 경우)
        Files.createDirectories(splitFilePath.getParent());

        // 파일 쓰기 (존재하지 않는 경우 새로 생성)
        Files.write(splitFilePath, lines, StandardOpenOption.CREATE_NEW);

        return splitFilePath.toFile();
    }

    // 파일명에 사용할 수 없는 문자 제거
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9가-힣.-]", "_");
    }

    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? fileName : fileName.substring(0, lastDotIndex);
    }

    private String getUniqueFileName(Path path) throws IOException {
        // 원본 파일 정보 추출
        String originalFileName = path.getFileName().toString();
        originalFileName= getFileNameWithoutExtension(originalFileName);
        originalFileName = sanitizeFileName(originalFileName);

        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

        // 파일 최종 수정 날짜 포맷
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String lastModifiedDate = dateFormat.format(new Date(attrs.lastModifiedTime().toMillis()));

        // 파일 크기
        long fileSize = attrs.size();
        return String.format("%s"+DELIMITER+"%s"+DELIMITER+"%d", sanitizeFileName(originalFileName), lastModifiedDate, fileSize);
    }
}
