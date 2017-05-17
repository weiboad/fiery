### Ragnar Fiery分布式性能跟踪系统

### 项目简介
 * Fiery跟踪系统是基于内部Ragnar分布式跟踪设计做的嵌入式精简版
 * 此系统针对小容量的PHP系统提供分布式调用跟踪及性能监控服务(APM-Application Performance Management)
 * 提供分布式系统性能跟踪，依赖接口性能跟踪，系统故障去重合并等功能

---------------------------------------

### 功能组成
 * 系统埋点库(ragnarsdk)用于植入系统埋点库产生调用日志
 * 日志收集端(logpusher收集埋点库日志推送到服务端)
 * 服务端（日志存储，索引，web管理界面服务）

---------------------------------------

### 开源协议 
 * Apache 2.0 
 * 附加声明:经营此系统衍生分支版本请联系作者授权方可使用

---------------------------------------

### 系统安装

---------------------------------------

#### RagnarSDK埋点库
埋点库使用PHP制作,在ragnarsdk目录内 INSTALL.md 及 README.md 有详细介绍，简单的说就是系统埋点RagnarSDK，Nginx配置一些变量。

##### RagnarSDK埋点库-环境依赖：
 * PHP 5.5+
 * bcmath

---------------------------------------

#### 服务端
 * 此服务是主要服务，用于记录、索引、展示所有相关日志及性能信息

##### 服务端-系统要求
 * 16G 内存，闲置 8G 以上。
 * CPU 四线程以上限制
 * 存储空间取决于日志量大小及存储天数
 * Java 8 Runtime http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

##### 服务端-安装
 * 安装 Java 8 Runtime 
 * 拷贝 ragnarserver-0.5.1-SNAPSHOT.jar 包到服务器
 * 创建系统日志目录：mkdir logs 
 * 创建索引存储目录：mkdir index
 * 创建日志存储目录：mkdir db
 * 启动：nohup java -Xms3750m -Xmx3750m -XX:ReservedCodeCacheSize=240m -XX:+UseCompressedOops -jar ragnarserver-0.5.1-SNAPSHOT.jar -type server --server.port=9090 &
 * 启动成功后浏览器访问 http://服务器IP:9090/ragnar/ 即可看到相关界面
 * 注意：当前版本对性能统计数据没有做持久化, 重启会清零

---------------------------------------

#### 日志收集服务
 * 安装 JDK8
 * 拷贝 ragnarserver-0.5.1-SNAPSHOT.jar 包到服务器
 * 启动：nohup java -Xms128m -Xmx750m -XX:ReservedCodeCacheSize=240m -XX:+UseCompressedOops -jar target/ragnarserver-0.5.1-SNAPSHOT.jar -type logpush -path 这里写ragnarsdk产生日志的目录地址 -host 主服务IP:主服务端口（9090） -outtime 7 &
 * 其他方式 nohup php logcollector/tail.php ragnarsdk日志目录 0 & （仅用于测试，服务器地址等信息需要在文件内更改）

---------------------------------------

#### todo
 * 增加bash脚本(start/restart/stop等)
 * 对性能统计数据做持久化
 * 增加对statsd的支持

---------------------------------------

## 联系我们
 * 微博广告 ADINF 团队 出品
 * QQ群: 318051466
