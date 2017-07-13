package org.weiboad.ragnar.logpusher;

import org.weiboad.ragnar.common.Toolbox;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    private static Options buildCommandLineOptions() {
        Options options = new Options();

        Option option = new Option("c",true,"Logpusher config file");
        option.setRequired(false);
        options.addOption(option);

        option = new Option("p", false, "Print logpusher config");
        options.addOption(option);

        return options;
    }

    public static void main(String[] args) {

        try {

            final CommandLine commandLine = Toolbox.parseCmdLine("Logpusher",
                    args, buildCommandLineOptions(), new PosixParser());
            if (null == commandLine) {
                Toolbox.exit(0);
            }

            final LogPusherConfig logPusherConfig = new LogPusherConfig();
            if (commandLine.hasOption("c")) {
                File configFile = new File(commandLine.getOptionValue("c"));
                if (!configFile.exists()) {
                    Toolbox.die("Fail to load config file[" + configFile.getPath() + "]");
                }

                InputStream in = new BufferedInputStream(new FileInputStream(configFile));
                Properties properties = new Properties();
                properties.load(in);
                Toolbox.properties2Object(properties, logPusherConfig);

                in.close();
                Toolbox.msg("Load config file[" + configFile.getPath() + "]");
            }

            if (commandLine.hasOption("p")) {
                Toolbox.printObjectProperties(log, logPusherConfig);
            }

            final LogMonitor logMonitor = new LogMonitor();
            String pushType = logPusherConfig.getPushType();
            if (pushType.equalsIgnoreCase("http")) {
                //use http post to push the log to server
                logMonitor.startHttpPush(logPusherConfig.getHost(), logPusherConfig.getThreadCount());
            } else if (pushType.equalsIgnoreCase("kafka")) {
                //use the kafka transform log
                logMonitor.startKafkaPush(logPusherConfig.getKafkaTopic(), logPusherConfig.getKafkaServer());
            } else {
                Toolbox.die("pushType:[" + pushType + "] not support.");
            }
            //main work start
            logMonitor.startFileScan(logPusherConfig.getPath(), logPusherConfig.getOutTime());

        } catch (Exception e) {
            Toolbox.die("Startup logpusher error", e);
        }

    }
}