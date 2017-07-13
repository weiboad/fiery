Fiery
====== 

![logo](https://img.shields.io/badge/status-alpha-red.svg)
[![Build Status](https://travis-ci.org/weiboad/fiery.png)](https://travis-ci.org/weiboad/fiery)
[![License](https://img.shields.io/badge/license-apache2-blue.svg)](LICENSE)


 Fiery 是一款为PHP性能跟踪监控系统，可以方便的查看线上调用关系，性能，回放请求过程，参数，系统异常统计等
![showtrace](docs/imgs/showtrace.png)


### 特点简介
 * 部署简单方便，所有服务都集成在jar包内，除Java Runtime无其他依赖，开箱即用。

### 模块及功能
 * 埋点库: RagnarSDK提供PHP侵入式性能埋点库，集成到 项目入口、Curl类及Mysql基础类即可
 * 日志收集: LogPusher服务负责监控收集埋点库产生的日志更新，并推送到服务端
 * 统计存储服务: Server接收日志，并对日志进行整理、存储、汇总、索引、统计分析功能
 * 更多信息请到 [wiki](https://github.com/weiboad/fiery/wiki) 获取

### 最低配置
 * PHP 5.3 or later with bcmath
 * Linux, OS X 、Windows
 * 内存: 2G+
 * Java 8 Runtime

### fiery server 服务端安装
 1. 下载并安装 [Java 8 Runtime](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
 2. 下载Fiery最新的 Fiery [Release page](https://github.com/weiboad/fiery/releases) jar包
 3. 在jar所在目录创建文件夹 mkdir logs index db
 4. 通过以下命令启动主服务:
 ```
 nohup java -XX:-MaxFDLimit -Xms3750m -Xmx3750m -XX:ReservedCodeCacheSize=240m -XX:+UseCompressedOops -jar ragnarserver-0.5.3-SNAPSHOT.jar --server.port=9090 &
 ```
 5. 服务启动后 浏览器访问地址： http://127.0.0.1:9090/ragnar/

### fierysdk 埋点库植入
 * [埋点库相关介绍](https://github.com/weiboad/fierysdk/blob/master/README.md)


### LogPusher 日志抓取
 日志推送服务，可以监控一个目录下所有日志是否有更新，并将内容推送到主服务,安装步骤如上，新版支持推送日志到kafka
 ```
 nohup java -XX:-MaxFDLimit -Xms128m -Xmx450m -XX:ReservedCodeCacheSize=240m -XX:+UseCompressedOops -jar logpusher-0.5.3-SNAPSHOT.jar  -c ./conf/logpusher.properties &
 ```

### 参数及配置文件说明
Logpusher(使用配置文件，由命令行参数-c指定[配置文件](./conf/logpusher.properties)路径)

|      配置选项        |      选项      |   说明    |
| --------------- |:-------------:| ---------:|
|path            | 要监控的日志路径 | 此选项用于logpusher 指向ragnarsdk产生日志目录|
|outTime         | 本地日志保存时间| 超时日志会被自动清理 |
|pushType       | 推送类型 http（默认） kafka（用于大流量用户）|日志传输模式|
|host            | 127.0.0.1:9090 | 日志收集到后会推送到fiery server,pushtype=http时用的参数|
|threadCount     | 推送日志线程数默认8  |越大越快，但是CPU使用会增加|
|kafkaServer    | kafka服务器列表如 10.10.1.1:9192,10.10.1.2:9192| kafka broker服务器列表|
|kafkaTopic    | kafka内的topic如fiery_test|向那个topic推送日志|

Server(使用命令行参数)

|      命令行参数        |      选项      |   说明    |
| --------------- |:-------------:| ---------:|
|--server.port    | fiery 服务监听端口如9090| |
|--fiery.kafkaenable|true false|是否开启kafka的消费端|
|--fiery.kafkaserver|10.1.1.1:9191,10.1.1.2:9191|kafka的broker服务器ip:port列表|
|--fiery.kafkatopic|kafka topic|kafka topic名称|
|--fiery.kafkagroupid|kafka groupid|消费端groupid|


Kafka版本
默认集成的是0.9的kafka协议，如果需要更新的协议支持，直接修改pom.xml的kafka依赖包版本即可

## 联系我们
 * WeiboAD ADINF Team
 * QQ Group: 318051466
