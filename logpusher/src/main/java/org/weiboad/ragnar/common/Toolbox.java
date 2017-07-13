package org.weiboad.ragnar.common;

import org.apache.commons.cli.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class Toolbox {

    public static void exit(int status) {
        System.exit(status);
    }

    public static void msg(String m) {
        System.out.println(m);
    }

    public static String banner(String msg) {
        return "---------------- " + msg + " ----------------";
    }

    public static String printMem(long bytes) {
        double dbytes = (double) bytes;
        DecimalFormat df = new DecimalFormat("#.##");

        if (dbytes < 1024) {
            return df.format(bytes);
        } else if (dbytes < 1024 * 1024) {
            return df.format(dbytes / 1024);
        } else if (dbytes < 1024 * 1024 * 1024) {
            return df.format(dbytes / 1024 / 1024) + "M";
        } else if (dbytes < 1024 * 1024 * 1024 * 1024L) {
            return df.format(dbytes / 1024 / 1024 / 1024) + "G";
        } else {
            return "Too big to show you";
        }
    }

    public static void die(String msg) {
        die(msg, null);
    }


    public static void die(String msg, Exception e) {
        System.err.println(msg);

        if (e != null) {
            System.err.println("Exception: " + e + "\n");
        }

        Thread.dumpStack();
        exit(-1);
    }

    public static CommandLine parseCmdLine(final String appName, String[] args, Options options,
                                           CommandLineParser parser) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
            if (commandLine.hasOption('h')) {
                hf.printHelp(appName, options, true);
                return null;
            }
        } catch (ParseException e) {
            hf.printHelp(appName, options, true);
        }

        return commandLine;
    }

    public static String fileGetContent(final String filePath) {
        File file = new File(filePath);
        return fileGetContent(file);
    }

    @Nullable
    public static String fileGetContent(final File file) {
        if (file.exists()) {
            char[] data = new char[(int) file.length()];
            boolean result = false;

            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
                int len = fileReader.read(data);
                result = len == data.length;
            } catch (IOException e) {
                // e.printStackTrace();
            } finally {
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (result) {
                return new String(data);
            }
        }
        return null;
    }

    public static void filePutContent(final String str, final String filePath) throws IOException {
        filePutContent(str, new File(filePath));
    }

    public static void filePutContent(final String str, final File file) throws IOException {
        File fileParent = file.getParentFile();
        if (fileParent != null) {
            fileParent.mkdirs();
        }
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(str);
        } catch (IOException e) {
            throw e;
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    public static void printObjectProperties(final Logger log, final Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                String name = field.getName();
                if (!name.startsWith("this")) {
                    Object value = null;
                    try {
                        field.setAccessible(true);
                        value = field.get(object);
                        if (null == value) {
                            value = "";
                        }
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    if (log != null) {
                        log.info(name + "=" + value);
                    }
                }
            }
        }
    }

    public static String properties2String(final Properties properties) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getValue() != null) {
                sb.append(entry.getKey().toString())
                        .append("=").append(entry.getValue().toString())
                        .append("\n");
            }
        }
        return sb.toString();
    }

    public static Properties string2Properties(final String str) {
        Properties properties = new Properties();
        try {
            InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return properties;
    }

    public static Properties object2Properties(final Object object) {
        Properties properties = new Properties();

        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                String name = field.getName();
                if (!name.startsWith("this")) {
                    Object value = null;
                    try {
                        field.setAccessible(true);
                        value = field.get(object);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    if (value != null) {
                        properties.setProperty(name, value.toString());
                    }
                }
            }
        }

        return properties;
    }

    public static void properties2Object(final Properties p, final Object object) {
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            String mn = method.getName();
            if (mn.startsWith("set")) {
                try {
                    String tmp = mn.substring(4);
                    String first = mn.substring(3, 4);

                    String key = first.toLowerCase() + tmp;
                    String property = p.getProperty(key);
                    if (property != null) {
                        Class<?>[] pt = method.getParameterTypes();
                        if (pt != null && pt.length > 0) {
                            String cn = pt[0].getSimpleName();
                            Object arg = null;
                            if (cn.equals("int") || cn.equals("Integer")) {
                                arg = Integer.parseInt(property);
                            } else if (cn.equals("long") || cn.equals("Long")) {
                                arg = Long.parseLong(property);
                            } else if (cn.equals("double") || cn.equals("Double")) {
                                arg = Double.parseDouble(property);
                            } else if (cn.equals("boolean") || cn.equals("Boolean")) {
                                arg = Boolean.parseBoolean(property);
                            } else if (cn.equals("float") || cn.equals("Float")) {
                                arg = Float.parseFloat(property);
                            } else if (cn.equals("String")) {
                                arg = property;
                            } else {
                                continue;
                            }
                            method.invoke(object, arg);
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }

    @NotNull
    public static String formatTime(long millis) {
        long sec = millis / 1000;
        long min = sec / 60;
        sec = sec % 60;
        long hr = min / 60;
        min = min % 60;

        return hr + ":" + min + ":" + sec;
    }

    public static boolean deleteDirectory(String directory)
    {
        return deleteDirectory(new File(directory));
    }

    public static boolean deleteDirectory(File directory)
    {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    } else {
                        f.delete();
                    }
                }
            }
        }
        return directory.delete();
    }

    public static boolean deleteFile(String file)
    {
        return new File(file).delete();
    }

    public static void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        } catch (InterruptedException e)
        {
        }
    }

    public static String newSessionId() {
        return UUID.randomUUID().toString();
    }

    public static String getGCStats() {
        long totalGC = 0;
        long gcTime = 0;

        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = gc.getCollectionCount();

            if (count >= 0) {
                totalGC += count;
            }

            long time = gc.getCollectionTime();

            if (time >= 0) {
                gcTime += time;
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append(banner("memory stats"));
        sb.append("\n- total collections: " + totalGC);
        sb.append("\n- total collection time: " + formatTime(gcTime));

        Runtime runtime = Runtime.getRuntime();
        sb.append("\n- total memory: " + printMem(runtime.totalMemory()));

        return sb.toString();
    }

}
