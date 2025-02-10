package dev.cheng;

import com.beust.jcommander.JCommander;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class App {

    public static void main(String[] argv) {
        App app = new App();
        app.run(argv);
    }

    public void run(String[] argv) {
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(argv);

        Filetype filetype = Filetype.of(args.getFilePath());

        System.out.println("Filetype: " + filetype);
        unzip(filetype, args.getFilePath(), args.getPass());

        String csvPath = getCsvPath();
        System.out.println("Csv path: " + csvPath);

        tidyUpCsvFile(filetype, csvPath);

        split(csvPath, 10);
    }

    public void unzip(Filetype filetype, String zipPath, String zipPassword) {
        try (var zip = new ZipFile(zipPath, zipPassword.toCharArray())) {
            String destDir = Constant.currentDir + Constant.fileSeparator + Constant.tmpDir;
            Path destDirPath = Path.of(destDir);
            if (!Files.exists(destDirPath)) {
                Files.createDirectory(destDirPath);
            }
            List<FileHeader> fileHeaders = zip.getFileHeaders();
            if (fileHeaders.isEmpty()) {
                System.out.println("No files in zip");
                return;
            }
            switch (filetype) {
                case Wechat -> DB.csvFilename = fileHeaders.get(1).getFileName();
                case Alipay -> DB.csvFilename = fileHeaders.getFirst().getFileName();
            }
            zip.extractAll(destDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCsvPath() {
        return Constant.currentDir + Constant.fileSeparator + Constant.tmpDir + Constant.fileSeparator + DB.csvFilename;
    }

    public void tidyUpCsvFile(Filetype filetype, String csvPath) {
        Charset srcCharset = StandardCharsets.UTF_8;
        int skipLines = 0;
        switch (filetype) {
            case Alipay -> {
                skipLines = 24;
                srcCharset = Charset.forName("GBK");
            }
            case Wechat -> skipLines = 16;
        }

        Path path = Path.of(csvPath);
        Path tmpPath = Path.of(csvPath + ".tmp");

        try (BufferedReader reader = Files.newBufferedReader(path, srcCharset); BufferedWriter writer =
                Files.newBufferedWriter(tmpPath)) {
            for (int i = 0; i < skipLines; i++) {
                if (reader.readLine() == null) return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

            Files.move(tmpPath, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void split(String csvPath, int n) {
        String destDir =
                Constant.currentDir + Constant.fileSeparator + Constant.tmpDir + Constant.fileSeparator + Constant.splitDir;
        Path destPath = Path.of(destDir);

        Path path = Path.of(csvPath);
        try {
            List<String> lines = Files.readAllLines(path);
            if (lines.isEmpty()) {
                System.out.println("Csv is empty");
                return;
            }
            String header = lines.getFirst();
            lines.removeFirst();
            Collections.reverse(lines);

            if (Files.exists(destPath)) {
                try (Stream<Path> paths = Files.list(destPath)) {
                    paths.forEach(item -> {
                        try {
                            Files.delete(item);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } else {
                Files.createDirectory(destPath);
            }
            int fileIndex = 1;
            for (int i = 0; i < lines.size(); i += n) {
                List<String> chunk = lines.subList(i, Math.min(i + n, lines.size()));

                List<String> newFileContent = new ArrayList<>();
                newFileContent.add(header);
                newFileContent.addAll(chunk);

                String outputFilePath = destDir + Constant.fileSeparator + DB.splitFilename + fileIndex + ".csv";
                Files.write(Paths.get(outputFilePath), newFileContent);

                System.out.println("生成文件：" + outputFilePath);
                fileIndex++;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}