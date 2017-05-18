java -Xms128m -Xmx750m -XX:ReservedCodeCacheSize=240m -XX:+UseCompressedOops -jar target/ragnarserver-0.5.1-SNAPSHOT.jar -type logpush -path ./logcollector/ -host 127.0.0.1:9090 -outtime 7
