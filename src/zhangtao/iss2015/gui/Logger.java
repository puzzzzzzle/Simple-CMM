package zhangtao.iss2015.gui;

import java.io.*;
import java.nio.file.Files;

public abstract class Logger {
    private final static String path = "./logs/";

    public static void writeToLogs(String info, String name) throws IOException {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            Files.createDirectory(file.toPath());
        }
        if (file.exists() || file.isDirectory()) {
            String mPath = path + name + ".cmmLog";
            try (PrintWriter writer = new PrintWriter(
                    new BufferedOutputStream(
                            new FileOutputStream(mPath)
                    )
            )) {
                writer.println(info);
            }
        }
    }
}
