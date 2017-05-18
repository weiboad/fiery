<?php

namespace Adinf\RagnarSDK;

/**
 * Class RagnarSDK
 * @package Adinf\RagnarSDK
 * @author changlong1
 */

class RagnarSDK
{
    //is disable the work
    private static $_enable = true;

    //is init flag
    private static $_isinit = false;

    //current request trace id 此次请求的唯一UUID
    private static $_traceid = "";

    //current rpc id 此次请求的调用累加rpcid
    private static $_rpcid = "0";

    //rpcid + seq is current rpcid
    private static $_seq = 0;

    //cacheed log 日志存储变量
    private static $_log = array();

    //request start
    private static $_starttime;

    //current idc and ip
    private static $_idc;
    private static $_ip;

    //project name 当前鲜明名称，会作为日志保存目录名
    private static $_project = "";

    //log level 默认分级日志级别，生产环境一般都是Error
    private static $_log_level = RagnarConst::LOG_TYPE_ERROR;

    //如果url内带参数那么url会很难做汇总
    //用回调方式兼容多个方式
    private static $_urlruleCallback = null;

    //meta other parameter 附加在meta日志内，不建议存太多，会影响性能
    private static $_uid = 0;
    private static $_env = "";
    private static $_extra = array();

    private static $_devmode = false;

    //current SDK Version
    const VERSION = "v0.3.9f";

    /**
     * init 初始化系统状态
     * @param string $projectName 当前项目名称建议小写英文字母下划线
     * @throws |Exception
     */
    public static function init($projectName)
    {
        //检测是否启用
        if (!self::isEnable()) {
            return;
        }

        //is init already
        if (self::$_isinit) {
            return;
        }

        //init flag
        self::$_isinit = true;

        //idc
        if (isset($_SERVER["RAGNAR_IDC"]) && trim($_SERVER["RAGNAR_IDC"]) !== "" &&
            $_SERVER["RAGNAR_IDC"] >= 0 && $_SERVER["RAGNAR_IDC"] <= 3
        ) {
            self::$_idc = trim($_SERVER["RAGNAR_IDC"]) . "";
        } else {
            throw new \Exception("Ragnar:RAGNAR_IDC取值 0~3 ，请检查Nginx配置选项", 1000);
        }

        self::$_project = trim($projectName);
        if (strlen(self::$_project) == 0) {
            throw new \Exception("Ragnar:请指定初始化的项目名称参数", 1001);
        }

        //get server ip
        if (isset($_SERVER["RAGNAR_IP"]) && $_SERVER["RAGNAR_IP"] != "" &&
            count(explode(".", $_SERVER["RAGNAR_IP"])) == 4
        ) {
            self::$_ip = $_SERVER["RAGNAR_IP"];
        } else {
            throw new \Exception("Ragnar:服务器IP配置错误，请检查Nginx配置选项", 1004);
        }


        //record start time
        self::$_starttime = microtime(true);

        //if set the rpcid header
        if (!empty($_SERVER["HTTP_X_RAGNAR_RPCID"])) {
            self::$_rpcid = trim($_SERVER["HTTP_X_RAGNAR_RPCID"]);
        }

        //if set the traceid header
        if (!empty($_SERVER["HTTP_X_RAGNAR_TRACEID"])) {
            self::$_traceid = trim($_SERVER["HTTP_X_RAGNAR_TRACEID"]);
        } else {
            //trace id not exist general one
            self::getTraceID();
        }

        //set log level
        if (!empty($_SERVER["HTTP_X_RAGNAR_LOGLEVEL"])) {
            self::$_log_level = trim($_SERVER["HTTP_X_RAGNAR_LOGLEVEL"]);
        }

        //traceid rpcid
        header("X-RAGNAR-TRACEID: " . self::$_traceid);
        header("X-RAGNAR-RPCID: " . self::$_rpcid);

        //shutdown process
        register_shutdown_function(function () {
            self::shutdown();
        });

    }

    /**
     * 此功能不是给任何开发使用的
     * 只是用来此SDK开发测试使用
     * 任何业务代码禁止调用
     */
    public static function devmode()
    {
        self::$_devmode = true;
        self::$_idc = "0";
        self::$_project = "ragnar_dev";
        self::$_ip = "11.11.11.11";
        self::$_isinit = true;
        self::$_starttime = microtime(true);
        //shutdown process
        register_shutdown_function(function () {
            self::shutdown();
        });
    }

    /**
     * 用于检测当前是否启用状态
     * 由于启用状态检测涉及多个选项统一用一个函数处理了
     */
    public static function isEnable()
    {

        //仅供本SDK开发人员自行调试使用
        if (self::$_devmode) {
            return true;
        }

        //被禁用不工作
        if (!self::$_enable) {
            return false;
        }

        //命令行启用的脚本，不工作
        if (php_sapi_name() == "cli") {
            return false;
        }

        return true;
    }

    /**
     * 如果url内包含请求参数，会导致url做聚合和统计很难唯一
     * 通过这个函数将一些url进行唯一化
     * 如xxx.weibo.com/v1/log/12312312/lists.json xxx.weibo.com/v1/log/4567/lists.json
     * 过滤成xxx.weibo.com/v1/log/filteredparam/lists.json
     * 使用之前请setUrlFilterCallback指定规则
     * @param string $url
     * @param bool $hasquery result with query parameter
     * @return string $url
     */
    public static function urlFilter($url, $hasquery = false)
    {
        if (self::$_urlruleCallback != null) {
            return call_user_func(self::$_urlruleCallback, $url, $hasquery);
        } else {
            return $url;
        }
    }

    /**
     * url过滤回调注册，如果不指定请指定为null
     * 每个网站的url规则多变，单纯的模板是无法通用的，只好这么做了
     * @param callback $callback url处理回调函数可以为null
     */
    public static function setUrlFilterCallback($callback)
    {
        self::$_urlruleCallback = $callback;
    }

    /**
     * 设置附加metalog信息,不建议放太多东西
     * @param string $uid 用户uid
     * @param array $extra 附加记录信息数组 不建议放不规则的数据结构
     */
    public static function setMeta($uid, $env, $extra)
    {
        self::$_uid = $uid;
        self::$_env = $env;
        self::$_extra = json_encode($extra);
    }

    /**
     * 如使用此功能必须执行在init之前
     * 设置日志记录级别，低于这个级别的日志不会记录到日志
     * @param $level
     * @throws |Exception
     */
    public static function setLogLevel($level)
    {
        if (!is_numeric($level) || $level < 1 || $level > 7) {
            throw new \Exception("日志等级取值范围:1-7", 1008);
        }
        self::$_log_level = trim($level);
    }

    /**
     * 记录业务日志
     * @param string $type 日志类型本地LOG_TYPE常量,代表日志等级
     * @param string $file 文件路径
     * @param int $line 写此日志的文件行数
     * @param string $tag 用户自定义tag，用来区分日志类型的
     * @param string $content 日志内容
     */
    public static function RecordLog($type, $file, $line, $tag, $content)
    {
        //检测是否可用
        if (!self::$_enable) {
            return;
        }

        if ($type < self::$_log_level) {
            //ignore the low level log
            return;
        }

        //record on var
        //t type ,p path,l line, m msg,g tag,e time,c cost

        self::$_log[] = array("r" => self::getChildRPCID(), "t" => $type, "e" => microtime(true), "g" => $tag, "p" => $file, "l" => $line, "m" => $content);

    }

    /**
     * 手动性能埋点开始，此函数会返回一组数据，这个数据是给digLogEnd函数使用的
     * @param string $file 当前埋点文件路径
     * @param int $line 当前行
     * @param string $tag 性能标签比如:模块名_函数名_xxx
     * @return array
     */
    public static function digLogStart($file, $line, $tag)
    {
        return array(
            "file" => $file,
            "line" => $line,
            "tag" => $tag,
            "start" => microtime(true),
            "rpcid" => self::getChildNextRPCID(),
        );
    }

    /**
     * 手动性能埋点结束，传入之前埋点函数返回的数据到这里即可产生日志
     * @param array $config 配置信息
     * @param string $msg 附加文字信息
     */
    public static function digLogEnd($config, $msg)
    {
        //检测是否初始化并且未禁用
        if (!self::isEnable()) {
            return;
        }

        //replace the special url
        if (is_array($msg) && isset($msg["url"])) {
            $msg["url"] = self::urlFilter($msg["url"]);
        }

        //record on var
        //t type ,p path,l line, m msg,g tag,e time,c cost
        self::$_log[] =
            array(
                "t" => RagnarConst::LOG_TYPE_PERFORMENCE,
                "e" => microtime(true),
                "g" => $config["tag"],
                "p" => $config["file"],
                "l" => $config["line"],
                "c" => bcsub(microtime(true), $config["start"], 4),
                "m" => $msg,
                "r" => $config["rpcid"],
            );
    }

    /**
     * mysql性能埋点结束，使用digLogStart作为开始
     * 此函数只是digLogEnd的封装
     * @param array $digPoint 埋点digLogStart返回的值
     * @param string $sql 此次执行的sql
     * @param array $data 此次执行的sql配套的data没有直接传入array()
     * @param string $op 此次操作类型如select update delete insert
     * @param string $fun 此次埋点相关函数名，仅供备注
     */
    public static function digMysqlEnd($digPoint, $sql, $data, $op, $fun)
    {
        self::digLogEnd($digPoint, array("sql" => $sql, "data" => $data, "op" => $op, "fun" => $fun));
    }

    /**
     * curl性能埋点结束时调用，使用digLogStart作为开始
     * @param array $digPoint 埋点digLogStart返回的值
     * @param string $url 请求的url
     * @param string $method 请求的动作post get delete put 等
     * @param array $postParam post的参数
     * @param array $getParam get的参数
     * @param array $curlInfo curl_getinfo(handle)返回的内容
     * @param string $errCode 错误时产生的code curl_errno(handle)函数
     * @param string $errMsg 错误时获取到的msg curl_error(handle)函数
     * @param string $result
     */
    public static function digCurlEnd($digPoint, $url, $method, $postParam, $getParam, $curlInfo, $errCode, $errMsg, $result)
    {
        self::digLogEnd($digPoint, array(
            "url" => self::urlFilter($url, true),
            "orgurl" => $url,
            "method" => $method,
            "param" => array("post" => $postParam, "get" => $getParam),
            "info" => $curlInfo,
            "error" => array("errorno" => $errCode, "error" => $errMsg),
            "result" => $result,
        ));
    }

    /**
     * 获取当前请求的traceid，如果没有设置自动生成一个
     * @return string|boolean
     */
    public static function getTraceID()
    {
        if (trim(self::$_traceid) == "") {
            //prepare parameter
            $idc = self::$_idc;//2bit

            $ip = self::$_ip;//16bit
            $ip = explode(".", $ip);
            $ip = $ip[2] * 256 + $ip[3];

            $time = microtime();//28bit + 10bit
            $time = explode(" ", $time);

            $ms = intval($time[0] * 1000);
            $time = $time[1] - strtotime("2017-1-1");

            $rand = mt_rand(0, 255);//4

            $key = Traceid::encode($idc, $ip, $time, $ms, $rand);
            $key = MidTool::encode($key);

            self::$_traceid = $key;

        }

        return self::$_traceid;
    }

    public static function decodeTraceID($traceid)
    {
        $traceid = MidTool::decode($traceid);

        $result = Traceid::decode($traceid);
        $result["time"] = strtotime("2017-01-01") + $result["time"];

        $ip1 = (int)($result["ip"] / 256);
        $ip2 = (int)($result["ip"] % 256);

        $result["ip"] = $ip1 . "." . $ip2;
        return $result;
    }

    /**
     * 获取当前请求的RPCid
     * @return string
     */
    public static function getCurrentRPCID()
    {
        return self::$_rpcid;
    }

    /**
     * 获取当前子请求的RPCID，发送请求用，请不要使用这个
     * @return string
     */
    public static function getChildRPCID()
    {
        return self::$_rpcid . "." . self::$_seq;
    }

    /**
     * 获取下一个子请求的RPCID，getChildRPCID也会跟随变化，发送请求的时候用这个
     * @return string
     */
    public static function getChildNextRPCID()
    {
        self::$_seq++;
        return self::$_rpcid . "." . self::$_seq;
    }

    /**
     * 获取子请求的header参数，已经包含了getChildNextRPCID
     * @return array
     */
    public static function getChildCallParam()
    {
        //检测是否初始化并且未禁用
        if (!self::isEnable()) {
            return array();
        }

        $headers = array(
            "X-RAGNAR-RPCID" => self::getChildNextRPCID(),
            "X-RAGNAR-TRACEID" => self::getTraceID(),
            "X-RAGNAR-LOGLEVEL" => self::$_log_level,
        );
        return $headers;
    }

    /**
     * 获取子请求的curl header参数，已经包含了getChildNextRPCID
     * 通过这个函数获取下一次Curl请求所需的Header值
     * 如果指定了digstart返回的数组会使用当前rpciid
     * @param $digpoint array digpoint埋点
     * @return array
     */
    public static function getCurlChildCallParam($digpoint = array())
    {
        //检测是否初始化并且未禁用
        if (!self::isEnable()) {
            return array();
        }

        $headers = array(
            "X-RAGNAR-TRACEID: " . self::getTraceID(),
            "X-RAGNAR-LOGLEVEL: " . self::$_log_level,
        );

        if (isset($digpoint["rpcid"])) {
            $headers[] = "X-RAGNAR-RPCID: " . $digpoint["rpcid"];
        } else {
            $headers[] = "X-RAGNAR-RPCID: " . self::getChildNextRPCID();
        }

        return $headers;
    }

    /**
     * 当所有处理完毕后会触发这个函数进行收尾
     * 注意这里会关闭用户请求连接异步做一些事情，这个函数必须在最后执行
     * shutdow注册有顺序，请关注
     */
    public static function shutdown()
    {
        //检测是否初始化并且未禁用
        if (!self::isEnable()) {
            return;
        }

        //release the session handle
        //注意这里会关闭链接异步做一些事情，所以这个函数必须在最后执行
        \session_write_close();

        //finished the request
        if (function_exists("fastcgi_finish_request")) {
            \fastcgi_finish_request();
        }

        //记录此次请求的一些附加信息到日志
        self::RecordLog(RagnarConst::LOG_TYPE_INFO, __FILE__, __LINE__, "end",
            array(
                "from" => Util::getReferer(),
                "clientip" => Util::getClientIp(),
                "localip" => self::$_ip,
                "ua" => Util::getUserAgent(),
                "url" => Util::getCurrentUrl(),
            )
        );

        //获取最后一次产生的错误
        $error = error_get_last();
        if ($error) {
            self::RecordLog(RagnarConst::LOG_TYPE_ERROR, $error["file"], $error["line"], "end",
                array(
                    "type" => $error['type'],
                    "msg" => $error['message'],
                )
            );
        }

        //dump meta log
        self::DumpMetaLog();
        //dump common log
        self::DumpCommonLog();
    }

    /**
     * 禁用当前功能，不会产生日志
     * 用于不希望产生日志的请求
     */
    public static function disable()
    {
        self::$_enable = false;
    }


    private static function DumpMetaLog()
    {
        $time = self::$_starttime - 8 * 60 * 60;
        $log = array(
            "version" => self::VERSION,
            "rpcid" => self::getCurrentRPCID(),
            "traceid" => self::getTraceID(),
            "time" => self::$_starttime,
            "@timestamp" => date("Y-m-d", $time) . "T" . date("H:i:s", $time) . "Z",
            "elapsed_ms" => bcsub(microtime(true), self::$_starttime, 4),
            "perf_on" => 0,
            "ip" => self::$_ip,
            "rt_type" => php_sapi_name(),
            "uid" => self::$_uid . "",
        );

        if (isset($_SERVER["REQUEST_URI"]) && strlen($_SERVER["REQUEST_URI"]) > 0) {
            $url = $_SERVER["REQUEST_URI"];
            $log["url"] = self::urlFilter("http://" . $_SERVER['HTTP_HOST'] . $url);
            $log["param"] = json_encode(array("get" => $_GET, "post" => $_POST));
        } else {
            $log["url"] = $_SERVER["PHP_SELF"];
        }

        //record the httpcode
        if (function_exists("http_response_code") && http_response_code() > 0) {
            $log["httpcode"] = http_response_code();
        } else {
            $log["httpcode"] = -1;
        }

        $log["project"] = self::$_project;

        $log["extra"] = self::$_extra;
        $logstr = json_encode($log);
        $logstr = '{"create":{"_index":"' . self::$_project . '-' . date("Ymd") . '","_type":"' . self::$_project . '"}}' . "\n" . $logstr . "\n";

        //base 64 for encode the \n
        $logstr = base64_encode($logstr);

        //save to syslog
        self::writeMsg(self::$_project, "meta", $logstr);
    }


    private static function DumpCommonLog()
    {
        //if there is no log
        if (count(self::$_log) == 0) {
            return;
        }

        //filter the large msg
        foreach (self::$_log as $k => $v) {
            $msg = json_encode($v["m"]);
            if (strlen($msg) > 20480) {
                self::$_log[$k]["m"] = substr($msg, 0, 20480);
            }
        }

        //if the count too much
        if (count(self::$_log) > 30) {
            $list = array_chunk(self::$_log, 30);

            $result = array(
                array(
                    "key" => self::getTraceID(),
                    "rpcid" => self::getCurrentRPCID(),
                    "val" => "",
                    "timestamp" => time(),
                ),
            );

            foreach ($list as $logitem) {
                $result[0]["val"] = $logitem;
                $logstr = json_encode($result);

                self::writeMsg(self::$_project, "log", $logstr);
            }
        } else {
            $result = array(
                array(
                    "key" => self::getTraceID(),
                    "rpcid" => self::getCurrentRPCID(),
                    "val" => self::$_log,
                    "timestamp" => time(),
                ),
            );

            $logstr = json_encode($result);

            self::writeMsg(self::$_project, "log", $logstr);
        }

        //clean up
        self::$_log = array();
    }


    private static function getMsgDir($topic)
    {

        if (!isset($_SERVER["RAGNAR_LOGPATH"])) {
            //$_rootPath = "/tmp";
            $_rootPath = '/tmp';

        } else {
            $_rootPath = $_SERVER["RAGNAR_LOGPATH"];
        }

        return $_rootPath . "/" . trim($topic) . "/" . date("Ym") . "/";
    }


    private static function checkdir($path)
    {
        //must absolute path
        if (substr($path, 0, 1) != "/") {
            return false;
        }

        if (!is_dir($path)) {
            return mkdir($path, 0777, true);
        }

        return true;
    }


    private static function writeMsg($topic, $type, $data)
    {

        //write log for logagent
        $logpath = self::getMsgDir($topic . "_" . $type);

        //check the dir and create it
        $ret = self::checkdir($logpath);

        //create dir fail
        if (!$ret) {
            return false;
        }

        //put data to log
        $fsret = file_put_contents($logpath . date("d") . "-" . getmypid() . ".log", trim($data) . "\n", FILE_APPEND);
        //if fail write
        if (!$fsret) {
            return false;
        } else {
            return true;
        }

    }

}
