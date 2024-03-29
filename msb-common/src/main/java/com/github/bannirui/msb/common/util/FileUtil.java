package com.github.bannirui.msb.common.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * 文件工具类.
 */
public class FileUtil {

    /**
     * 目录\文件可读.
     */
    public static boolean canRead(File file) {
        if (file == null) {
            return false;
        }
        if (file.isDirectory()) {
            try {
                File[] listFiles = file.listFiles();
                return listFiles != null;
            } catch (Exception e) {
                return false;
            }
        } else {
            return file.exists() && checkRead(file);
        }
    }

    /**
     * 目录\文件可读.
     */
    public static boolean canRead(String file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        return canRead(new File(file));
    }

    /**
     * 文件是否可读.
     */
    private static boolean checkRead(File file) {
        FileReader fd = null;
        boolean succ = false;
        try {
            fd = new FileReader(file);
            if (fd.read() != -1) {
                // ignore
            }
            return true;
        } catch (IOException var13) {
            succ = false;
        } finally {
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return succ;
    }
}
