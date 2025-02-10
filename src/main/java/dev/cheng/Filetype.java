package dev.cheng;

import java.nio.file.Path;

public enum Filetype {
    Alipay,
    Wechat;

    static Filetype of(String filepath) {
        Path path = Path.of(filepath);
        String filename = path.getFileName().toString();
        DB.zipFilename = filename;
        if (filename.startsWith("交易流水证明_用于个人对账")) {
            DB.splitFilename = "alipay";
            return Filetype.Alipay;
        } else if (filename.startsWith("微信支付账单")) {
            DB.splitFilename = "wechat";
            return Filetype.Wechat;
        } else {
            throw new IllegalArgumentException("Can not detect filetype: " + filepath);
        }
    }
}
