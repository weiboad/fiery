package org.weiboad.ragnar.logpusher;

public class Main {
    public static void main(String[] args) {
        String usage =
                "Ragnar Tiny Server\r\n" +
                        "Usage:\r\n" +
                        "\t\tjava -jar logpusher.jar  -path ./ -host 127.0.0.1:8888 -threadcount 10 \r\n";

        String host = "127.0.0.1:9090"; //推送接口host设置
        String path = "./";             //扫描日志路径
        Integer outtimeInt = 0;         // 日志清理
        Integer threadcount = 10;       //线程数量默认


        //system variable
        System.out.println("Lib Path:" + System.getProperty("java.library.path"));

        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);
            System.exit(0);
        }

        for (int i = 0; i < args.length; i++) {

            if ("-host".equals(args[i])) {
                host = args[i + 1];
                i++;
            }
            if ("-path".equals(args[i])) {
                path = args[i + 1];
                i++;
            }
            if ("-outtime".equals(args[i])) {
                try {
                    outtimeInt = Integer.parseInt(args[i + 1]);
                } catch (Exception e) {
                    outtimeInt = 0;
                }
                i++;
            }
            if ("-threadcount".equals(args[i])) {
                threadcount = Integer.valueOf(args[i + 1]);
                i++;
            }
        }
        LogMonitor tail = new LogMonitor();
        tail.start(path, host, outtimeInt, threadcount);

    }
}