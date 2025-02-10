package dev.cheng;

import com.beust.jcommander.Parameter;

public class Args {
    @Parameter(names = {"--file"}, description = "zip file path")
    private String filePath;

    @Parameter(names = {"--pass"}, description = "zip file password")
    private String pass;

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
