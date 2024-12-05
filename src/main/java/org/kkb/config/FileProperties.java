package org.kkb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;


@Component
public class FileProperties {
    @Value("${file.path.receive}")
    private String receivePath;

    @Value("${file.path.ready}")
    private String ready;

    @Value("${file.path.process}")
    private String process;

    @Value("${file.path.completed}")
    private String completed;

    @Value("${file.path.error}")
    private String error;

    @Value("${file.line.limit}")
    private int lineLimit;

    @Value("${file.single.target}")
    private String singleTarget;

    @Value("${file.header.skip:Y}")
    private String headerSkip;

    @Value("${file.header}")
    private String[] fileHeader;


    public int getLineLimit() { return lineLimit; }
    public void setLineLimit(int lineLimit) { this.lineLimit = lineLimit; }

    public String getCompleted() {
        if(completed != null && !completed.endsWith(File.separator)) completed += File.separator;
        return completed;
    }
    public void setCompleted(String completed) { this.completed = completed; }

    public String getError() {
        if(error != null && !error.endsWith(File.separator)) error += File.separator;
        return error;
    }
    public void setError(String error) { this.error = error; }

    public String getProcess() {
        if(process != null && !process.endsWith(File.separator)) process += File.separator;
        return process;
    }
    public void setProcess(String process) { this.process = process; }

    public String getReady() {
        if(ready != null && !ready.endsWith(File.separator)) ready += File.separator;
        return ready;
    }
    public void setReady(String ready) { this.ready = ready; }

    public String getReceivePath() {
        if(receivePath != null && !receivePath.endsWith(File.separator)) receivePath += File.separator;
        return receivePath;
    }
    public void setReceivePath(String receivePath) { this.receivePath = receivePath; }

    public String getSingleTarget() { return singleTarget; }
    public void setSingleTarget(String singleTarget) { this.singleTarget = singleTarget; }

    public String getHeaderSkip() { return headerSkip; }
    public void setHeaderSkip(String headerSkip) { this.headerSkip = headerSkip; }

    public String[] getFileHeader() { return fileHeader; }
    public void setFileHeader(String[] fileHeader) { this.fileHeader = fileHeader; }
}
