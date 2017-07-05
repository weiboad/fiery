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
 日志推送服务，可以监控一个目录下所有日志是否有更新，并将内容推送到主服务,安装步骤如上
 ```
 nohup java -XX:-MaxFDLimit -Xms128m -Xmx450m -XX:ReservedCodeCacheSize=240m -XX:+UseCompressedOops -jar logpusher-0.5.3-SNAPSHOT.jar  -path [ragnarsdklogpath] -host [ip:port] -outtime 7 -threadcount 10 &
 ```

### 参数说明
 Logpusher及Server各是一个jar包

|      参数        |      选项      |   说明    |
| --------------- |:-------------:| ---------:|
|-type            | logpusher server| 启动 日志推送服务或网页服务|
|-path            | 要监控的日志路径 | 此选项用于logpusher 指向ragnarsdk产生日志目录|
|-host            | 127.0.0.1:9090 | 日志收集到后会推送到fiery server|
|-outtime         | 本地日志保存时间| 超时日志会被自动清理 |
|-threadcount     | 推送日志线程数  |越大越快，但是CPU使用会增加|
|--server.port    | fiery 服务监听端口如9090|用于fiery服务器|

## 联系我们
 * WeiboAD ADINF Team
 * QQ Group: 318051466
