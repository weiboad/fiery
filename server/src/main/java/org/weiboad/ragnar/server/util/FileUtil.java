package org.weiboad.ragnar.server.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class FileUtil {

    //递归删除空目录，但是不删除文件
    public static boolean deleteDir(String dirpath) {

        //prevent the wrong path make the server down
        if (dirpath.trim().equals("/") || dirpath.trim().equals("\\")) {
            return false;
        }

        File dir = new File(dirpath);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(dir + "/" + children[i]);
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    //list the sub folder list
    public static HashMap<String, String> subFolderList(String path) throws IOException {

        HashMap<String, String> result = new HashMap<>();

        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    result.put(file2.getName(), file2.getCanonicalPath());
                }
            }
        }
        return result;
    }
}
