package org.weiboad.ragnar.server;

import org.springframework.boot.system.ApplicationPidFileWriter;
import org.weiboad.ragnar.logpusher.LogPusherMain;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})


public class RagnarApplication {


    public static void main(String[] args) {
        String usage =
                "Ragnar Tiny Server\r\n" +
                        "Usage:\r\n" +
                        "\t\tjava -jar com.ragnar.server.RagnarserverApplication.jar -type server\r\n" +
                        "\t\tjava -jar com.ragnar.server.RagnarserverApplication.jar -type logpush -path ./ -host "
                        + "127.0.0.1:8888\r\n";

        String type = "server";//服务类型
        String host = "127.0.0.1:8888";//推送接口host设置
        String path = "/";//扫描日志路径
        String outtime = "";//过期日志清理如果传输按天传输
        Integer threadcount = 10;//线程数量默认十个
        System.out.println("Lib Path:" + System.getProperty("java.library.path"));

        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);
            System.exit(0);
        }

        for (int i = 0; i < args.length; i++) {
            if ("-type".equals(args[i])) {
                type = args[i + 1];
                i++;
            }
            if ("-host".equals(args[i])) {
                host = args[i + 1];
                i++;
            }
            if ("-path".equals(args[i])) {
                path = args[i + 1];
                i++;
            }
            if ("-outtime".equals(args[i])) {
                outtime = args[i + 1];
                i++;
            }
            if ("-threadcount".equals(args[i])) {
                threadcount = Integer.valueOf(args[i + 1]);
                i++;
            }
        }

        //decide which app will start
        if (type.equals("server")) {
            SpringApplication springApplication = new SpringApplication(RagnarApplication.class);
            springApplication.addListeners(new ApplicationPidFileWriter());
            springApplication.run(args);
        } else if (type.equals("logpush")) {
            LogPusherMain tail = new LogPusherMain();
            tail.start(path, host, outtime, threadcount);
        } else {
            System.out.println(usage);
            System.exit(0);
        }
    }
}