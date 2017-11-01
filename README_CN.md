Fiery
====== 

![logo](https://img.shields.io/badge/status-alpha-red.svg)
[![Build Status](https://travis-ci.org/weiboad/fiery.png)](https://travis-ci.org/weiboad/fiery)
[![License](https://img.shields.io/badge/license-apache2-blue.svg)](LICENSE)

Fiery 是一款为PHP提供服务的性能跟踪监控系统，可以方便的查看线上调用关系，响应性能，回放每次请求的具体执行过程、参数、异常。并且对系统异常及依赖数据性能做了去重统计，具有部署简单方便，开箱即用的优点。

部署在开发环境可以方便调试，部署在线上服务器可以快速定位线上故障及接口性能分析，有助于改善完善项目稳定性。

此项目还有C++11企业版本，但是由于依赖过多暂时并未对外开源。

使用Java开源一个针对PHP的服务只是为了方便用户部署，降低中小流量用户入门门槛。

集成此服务并不会影响原有接口性能
 
### 最低配置要求
 * PHP 5.3 or later with bcmath 扩展
 * 目前仅支持64位 UTF8编码PHP项目
 * Linux, MacOS、Windows
 * 内存: 4G+ （实际使用2G左右，预留2G应对突发峰值）
 * Java 8 Runtime

## 概念指引
### 模块及功能

![dataflow](https://github.com/weiboad/fiery/blob/master/docs/imgs/dataflow.jpg)

Ragnar Fiery主要由三个部分组成：

 * FierySDK：PHP埋点库（https://github.com/weiboad/fierysdk)
 * Log Pusher：日志收集及推送 在本项目内
 * Fiery Server：日志存储索引统计及管理界面（https://github.com/weiboad/fiery/server)

 
### FierySDK
FierySDK需要在Nginx引入一个环境变量文件(fierysdk/nginx/fiery_fastcgi_params)，通过这个文件可以获取一些环境变量，如FierySDK生成日志路径。当前服务器IP等信息。
  
FierySDK目前已打包成Composer，使用时使用Composer引入项目。按照集成指引在：

 * 框架入口
 * CURL基类
 * Mysql基类

以上类内埋点后即可工作，验证SDK是否工作可以查看Nginx变量内指定的路径内是否有日志产生，如果没有，需要检查目录是否有写入权限。

如仍旧不工作可以在FierySDK的src内RagnarSDK.php的代码打断点 进行原因排查。

> 后期若有精力可以对PHP项目的分级日志，以及一些性能关键函数内进行埋点，可以获得更多信息。

### LogPusher
LogPusher部署在每一台运行集成FierySDK项目的服务器内，主要职能是收集指定目录内日志内容，通过HTTP或Kafka推送所有日志到Fiery服务，通过这个服务我们才可以将日志传输到存储和分析服务器内。本服务支持多子目录内日志监控，能够定期清理过期日志（outtime参数）。

### Fiery Server
主要日志存储索引统计服务，所有日志都会存储索引在这个服务，这个服务目前服务于一个2000w QPS的项目（为了保证质量，接口请求返回都做了记录，每天40G左右的日志）限制服务使用4G内存，实际使用2G左右。Fiery Server会从Kafka或http推送获取Logpusher取来的FierySDK日志。通过对日志的统计、分析、存储、索引，完成分布式跟踪过程。

## 服务端安装步骤

 1. 下载并安装 [Java 8 Runtime](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
 2. 下载Fiery最新的 Fiery [Release page](https://github.com/weiboad/fiery/releases) jar包
 3. 在jar所在目录创建文件夹 mkdir logs index db
 4. 通过以下命令启动主服务:
 ```
 nohup java -XX:-MaxFDLimit -Xms3750m -Xmx3750m -XX:ReservedCodeCacheSize=240m -XX:+UseCompressedOops -jar ragnarserver-版本号.jar  --server.port=9090 &
 ```
 5. 服务启动后 浏览器访问地址： http://127.0.0.1:9090/ragnar/ 即可

### PHP项目埋点库埋点介绍
 * [埋点库相关介绍](https://github.com/weiboad/fiery/blob/master/README_CN.md)


### LogPusher 日志收集及推送服务
 日志推送服务，可以监控一个目录下所有日志是否有更新，并将内容推送到主服务
 ```
nohup java -XX:-MaxFDLimit -Xms128m -Xmx450m -XX:ReservedCodeCacheSize=240m -XX:+UseCompressedOops -jar logpusher-版本号.jar -c ./conf/logpusher.properties &
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
|kafkaTopic    | kafka内的topic如fiery_test|向那个topic推送日志，支持多topic订阅，如fiery_test1,fiery_test2|

Server(使用命令行参数)

|      命令行参数        |      选项      |   说明    |
| --------------- |:-------------:| ---------:|
|--server.port    | fiery 服务监听端口如9090| |
|--fiery.kafkaenable|true false|是否开启kafka的消费端|
|--fiery.kafkaserver|10.1.1.1:9191,10.1.1.2:9191|kafka的broker服务器ip:port列表|
|--fiery.kafkatopic|kafka topic|kafka topic名称可以订阅多个，使用英文逗号隔开即可|
|--fiery.kafkagroupid|kafka groupid|消费端groupid|
|--fiery.keepdataday|日志及数据保留时间（天）默认五天|统计，日志，排行信息等保存天数|


Kafka版本
默认集成的是0.9的kafka协议，如果需要更新的协议支持，直接修改pom.xml的kafka依赖包版本即可

------

### 功能界面介绍
##### 调用回放
![showtrace](https://github.com/weiboad/fiery/blob/master/docs/imgs/showtrace.png)
> * 展示多级API的调用关系，接口性能，响应状态，调用层级，服务器IP，用于查看此次服务质量，响应结果，以及相关参数及日志
> * 通过这个功能可以对线上所有请求进行回放调用过程方便快速找到接口故障原因（性能问题，逻辑问题等）
> * 线下线上运行或测试 时出现故障后可直接提供Traceid给研发直接查看原因
> * 线上故障可在错误提示附带Traceid方便快速查找问题

##### 最近请求
![recentrequest](https://github.com/weiboad/fiery/blob/master/docs/imgs/recent.png)
> * 查看最近的请求列表，一般会有5秒延迟，通过列表可以直接查看当前最新请求的信息且可以跳转到调用回放界面进行查看

##### 性能排行
![apitop](https://github.com/weiboad/fiery/blob/master/docs/imgs/apitop.png)
> * 查看每天埋点的服务性能排行，记录平均响应时间、响应时间比例、调用次数、httpcode比例，可方便的发现线上接口性能情况
> * 可查看按响应时间排序的请求列表

##### 依赖服务排行
> * 可查看同性能排行一样的数据指标，用于统计第三方API无埋点库的性能
> * 可查看不同时段的性能统计信息

##### SQL性能统计
> * 可查看所有SQL的性能情况，SQL自动去重合并统计
> * 可查看不同时段的SQL性能情况，SQL自动去重合并统计

##### 线上故障去重
> * 线上所有通过埋点库产生的警报日志、异常日志、通知日志都会在这里汇总，去重
> * 可查看第一次产生故障调用回放以及最后一次调用回放 

## 联系我们
 * WeiboAD ADINF Team
 * QQ Group: 318051466

