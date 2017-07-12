package org.weiboad.ragnar.logpusher;

public class Main {
    public static void main(String[] args) {
        String usage =
                "Ragnar Tiny Server\r\n" +
                        "Usage:\r\n" +
                        "\t\tjava -jar logpusher.jar  -path ./ -host 127.0.0.1:8888 -threadcount 10 \r\n";

        String host = "127.0.0.1:9090"; //推送接口host设置
        String path = "./";             //扫描日志路径
        String pushType = "http";       //默认http 推送，可选项kafka
        String kafkaTopic = "";       //默认http 推送，可选项kafka
        String kafkaServer = "";       //默认http 推送，可选项kafka

        Integer outtimeInt = 0;         // 日志清理
        Integer threadcount = 10;       //线程数量默认


        //system variable
        System.out.println("Lib Path:" + System.getProperty("java.library.path"));

        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);
            System.exit(0);
        }

        for (int i = 0; i < args.length; i++) {
            //basic parameter
            if ("-path".equals(args[i])) {
                path = args[i + 1];
                i++;
            }

            //expire clean up
            if ("-outtime".equals(args[i])) {
                try {
                    outtimeInt = Integer.parseInt(args[i + 1]);
                } catch (Exception e) {
                    outtimeInt = 0;
                }
                i++;
            }

            //push type
            if ("-pushtype".equals(args[i])) {
                pushType = args[i + 1];
                i++;
            }

            //http parameter
            //http post type
            if ("-host".equals(args[i])) {
                host = args[i + 1];
                i++;
            }
            //curl thread
            if ("-threadcount".equals(args[i])) {
                threadcount = Integer.valueOf(args[i + 1]);
                i++;
            }

            //kafka parameter
            //kafka pusher
            if ("-kafkatopic".equals(args[i])) {
                kafkaTopic = args[i + 1];
                i++;
            }
            //server
            if ("-kafkaserver".equals(args[i])) {
                kafkaServer = args[i + 1];
                i++;
            }

        }
        LogMonitor logMonitor = new LogMonitor();

        if (pushType.equalsIgnoreCase("http")) {
            //use http post to push the log to server
            logMonitor.startHttpPush(host, threadcount);
        } else if (pushType.equalsIgnoreCase("kafka")) {
            //use the kafka transform log
            logMonitor.startKafkaPush(kafkaTopic, kafkaServer);
        } else {
            System.out.println("-pushtype parameter is wrong only support kafka or http.");
            System.exit(4);
        }
        //main work start
        logMonitor.startFileScan(path, outtimeInt);

    }
}