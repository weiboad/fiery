###Start up
 * run cmd on log collect server:java -jar Fiery-xxxx.jar -type server
 * run cmd on server : java -jar Fiery-xxxx.jar -type org.weiboad.ragnar.server.logpusher
 * browser open 127.0.0.1:8888/ragnar/ to see the trace ui

###Windows 7 64 Version depend
 * when you see the "java.lang.UnsatisfiedLinkError: \AppData\Local\Temp\librocksdbjni4671838933304338394.dll: Can't find dependent libraries" you must install the vs2015(vc++ runtime)
 * https://www.microsoft.com/en-us/download/details.aspx?id=48145
