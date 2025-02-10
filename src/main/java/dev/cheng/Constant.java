package dev.cheng;

import java.nio.file.FileSystems;

public class Constant {
    public static final String splitDir = "split";
    public static final String tmpDir = "tmp";
    public static final String currentDir = System.getProperty("user.dir");
    public static final String fileSeparator = FileSystems.getDefault().getSeparator();
}
