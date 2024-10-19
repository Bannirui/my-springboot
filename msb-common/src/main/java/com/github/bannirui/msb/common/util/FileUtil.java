package com.github.bannirui.msb.common.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

    /**
     * 文件是否可写.
     */
    public static Boolean canWrite(File file) {
        if (file.isDirectory()) {
            try {
                file = new File(file, "canWriteTestDeleteOnExit.temp");
                if (file.exists()) {
                    boolean checkWrite = checkWrite(file);
                    if (!deleteFile(file)) {
                        file.deleteOnExit();
                    }

                    return checkWrite;
                } else if (file.createNewFile()) {
                    if (!deleteFile(file)) {
                        file.deleteOnExit();
                    }

                    return true;
                } else {
                    return false;
                }
            } catch (Exception var2) {
                return false;
            }
        } else {
            return checkWrite(file);
        }
    }

    private static boolean checkWrite(File file) {
        boolean delete = !file.exists();
        boolean result = false;
        boolean ret;
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write("");
            fw.flush();
            result = true;
            return true;
        } catch (IOException var15) {
            ret = false;
        } finally {
            if (delete && result) {
                deleteFile(file);
            }
        }
        return ret;
    }

    public static boolean deleteFile(File file) {
        return deleteFile(file, true);
    }

    /**
     * 删除文件.
     */
    public static boolean deleteFile(File file, boolean delDir) {
        if (!file.exists()) {
            return true;
        } else if (file.isFile()) {
            return file.delete();
        } else {
            boolean result = true;
            File[] children = file.listFiles();
            for (int i = 0; i < children.length; ++i) {
                result = deleteFile(children[i], delDir);
                if (!result) {
                    return false;
                }
            }
            if (delDir) {
                result = file.delete();
            }
            return result;
        }
    }
}
