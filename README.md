Fiery
====== 
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/44598eccff7c4eb58f6d66d1d51ff415)](https://www.codacy.com/app/xcl_rockman/fiery?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=weiboad/fiery/&amp;utm_campaign=Badge_Grade)
![logo](https://img.shields.io/badge/status-alpha-red.svg)
[![Build Status](https://travis-ci.org/weiboad/fiery.png)](https://travis-ci.org/weiboad/fiery)
[![License](https://img.shields.io/badge/license-apache2-blue.svg)](LICENSE) 

[中文文档](./README_CN.md)
 
![showtrace](docs/imgs/showtrace.png)

 Fiery is an APM-Application Performance Management for the PHP

### Introduction
 * All Service in an jar.easy to deploy
 * Burial SDK: integrated burial package for php project
 * Log Pusher: collect the ragnarsdk logs and push to the server
 * Server: index, storage, statistics the logs, web Management interface
 * Goto [wiki](https://github.com/weiboad/fiery/wiki) to get More Infomation
 
### Requirements
 * PHP 5.3 or later with bcmath
 * Linux, OS X and Windows
 * Memory: 2G or more 
 * Java 8 Runtime

### Getting Started
 1. Download [Java 8 Runtime](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
 2. tar xvf jdk-1.8.tar.gz
 3. Download Recent Relasese Jar on [Release page](https://github.com/weiboad/fiery/releases)
 4. mkdir logs index db
 5. Startup the Fiery Server by command:

> java -XX:-MaxFDLimit -Xms3750m -Xmx3750m -XX:ReservedCodeCacheSize=240m -XX:+UseCompressedOops -jar ragnarserver-0.5.3-SNAPSHOT.jar --server.port=9090

 6. Browse the web address http://127.0.0.1:9090/ragnar/

### Burial SDK
 * [Integrated On PHP Project](https://github.com/weiboad/fierysdk/blob/master/README.md)


### LogPusher


> nohup java -XX:-MaxFDLimit -Xms128m -Xmx450m -XX:ReservedCodeCacheSize=240m -XX:+UseCompressedOops -jar logpusher-0.5.3-SNAPSHOT.jar -type logpush -path ragnarsdklogpath -host fieryserverip:port -outtime 7 &

Tips: now the log pusher supported the kafka 0.9(if you want new version kafka please change pom.xml kafka client depend  version)

#### Todo
 * Service Manage Shell(startup stop restart)
 * Persistent memory statistics
 * Statsd


## Contact Us
 * WeiboAD ADINF Team
 * QQ Group: 318051466
 
