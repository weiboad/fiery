package com.ragnar.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import logpusher.LogPusherMain;


@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})


public class RagnarserverApplication {
    @Bean
    EmbeddedServletContainerCustomizer containerCustomizer() throws Exception {
        return (ConfigurableEmbeddedServletContainer container) -> {
            if (container instanceof TomcatEmbeddedServletContainerFactory) {
                TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container;
                tomcat.addConnectorCustomizers(
                        (connector) -> {
                            connector.setMaxPostSize(1000000000); // 10 MB
                        }
                );
            }
        };
    }
    public static void main(String[] args) {
        String usage =
                "Ragnar Tiny Server\r\n" +
                        "Usage:\r\n" +
                        "\t\tjava -jar com.ragnar.server.RaganrserverApplication.jar -type server\r\n" +
                        "\t\tjava -jar com.ragnar.server.RaganrserverApplication.jar -type logpush -path ./ -host 127.0.0.1:8888\r\n";

        String type = "server";
        String host = "127.0.0.1:8888";
        String path = "/";
        String outtime = "";
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
        }

        //decide which app will start
        if (type.equals("server")) {
            SpringApplication.run(RagnarserverApplication.class, args);
        } else if (type.equals("logpush")) {
            LogPusherMain tail = new LogPusherMain();
            tail.start(path, host,outtime);
        } else {
            System.out.println(usage);
            System.exit(0);
        }
    }
}