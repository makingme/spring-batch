package org.kkb.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvFileSplitter {
    public final static String DELIMITER = "_";
    // TODO: 파일명을 유니크하게 변경하여 관리되도록 추가 필요
    public static List<File> splitCsvFile(File inputFile, String destination, int linesPerFile, boolean hasHeader) throws IOException {
        Path destinationPath = Paths.get(destination);
        if(!Files.exists(destinationPath)){
            Files.createDirectory(destinationPath);
        }
        if(!Files.isDirectory(destinationPath)){
            throw new IOException(destinationPath + " is not a directory");
        }
        String originalFileName = getFileNameWithoutExtension(inputFile);
        originalFileName = "split"+ DELIMITER + originalFileName;
        List<File> splitFiles = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        // 헤더 읽기
        if(hasHeader) reader.readLine();
        // 파일 경로 보강
        if(!destination.endsWith(File.separator)){
            destination = destination + File.separator;
        }

        int fileIndex = 0;
        List<String> currentFileLines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            currentFileLines.add(line);
            if (currentFileLines.size() == linesPerFile) {
                splitFiles.add(writeSplitFile((destination+originalFileName + DELIMITER + fileIndex), currentFileLines));
                fileIndex++;
                currentFileLines.clear();
            }
        }

        // 마지막 남은 줄 처리
        if (!currentFileLines.isEmpty()) {
            splitFiles.add(writeSplitFile((destination+originalFileName + DELIMITER + fileIndex), currentFileLines));
        }

        reader.close();
        return splitFiles;
    }

    private static File writeSplitFile(String targetFile, List<String> lines) throws IOException {
        File file = new File(targetFile);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
        return file;
    }

    private static String getUniqueFileNameWithoutExtension(File file) {
        final String delimiter = "_";
        String fileName = getFileNameWithoutExtension(file);
        return fileName;
    }

    private static String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? fileName : fileName.substring(0, lastDotIndex);
    }
}
