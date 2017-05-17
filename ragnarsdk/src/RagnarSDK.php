<?php

namespace Adinf\RagnarSDK;

/**
 * Class RagnarSDK
 * Ragnar会产生两种日志，一种是meta日志用于ES做索引，一种是Log日志通过索引可查询
 * @package Adinf\RagnarSDK
 * @author changlong1
 */

class RagnarSDK
{
    //---------- Variable
    //是否启用，如果启用则Ragnar工作，不启用则不做任何动作和统计
    private static $_enable = true;

    //初始化标志，如果执行了就无需再执行
    private static $_isinit = false;

    //current request trace id 此次请求的唯一UUID
    private static $_traceid = "";

    //current rpc id 此次请求的调用累加rpcid
    private static $_rpcid = "0";

    //rpcid + seq is current rpcid
    private static $_seq = 0;

    //cacheed log 日志存储变量
    private static $_log = array();

    //xhprof open 性能日志 是否打开标志
    private static $_recordXhprof = "0";

    //xhprof record time 附加xhprof日志保存条件，如果0则都保存，如果非0 则运行大于这个值时保存
    private static $_xhproftime = 0;

    //request start
    private static $_starttime;

    //current idc and ip
    private static $_idc;
    private static $_ip;

    //project name 当前鲜明名称，会作为日志保存目录名
    private static $_project = "";

    //log level 默认分级日志级别，生产环境一般都是Error
    private static $_log_level = RagnarConst::LOG_TYPE_ERROR;

    //运行环境：
    //开发环境会自动推送到ragnar_dev_meta ragnar_dev_log
    //测试会推送到测试管理界面ragnar_test_meta ragnar_test_log
    //线上项目会推送到 项目名_log 项目名_meta 此为默认选项
    private static $_workmode = RagnarConst::MODE_ONLINE;

    //日志写入模式:
    //写入本地模式：所有Ragnar日志会写到本地
    //MC协议模式：所有Ragnar日志会通过Mc协议发送到Logagent
    //不写入模式:所有日志都会忽略不写入任何地方
    private static $_writemode = RagnarConst::WRITE_MODE_FILE;

    //如果url内带参数那么url会很难做汇总
    //用回调方式兼容多个方式
    private static $_urlruleCallback = null;

    //meta other parameter 附加在meta日志内，不建议存太多，会影响性能
    private static $_uid = 0;
    private static $_env = "";
    private static $_extra = array();

    //设置是否传输的数据进行压缩
    private static $_compress = false;

    //if it's dev mode on cli for test
    private static $_devmode = false;

    //if dump the log on mc this is timeout count
    private static $_mctimeout = 40; // 40ms

    //current SDK Version
    const VERSION = "v0.3.9f";

    /**
     * init 初始化系统状态,http请求使用这个初始化
     * @param string $projectname 当前项目名称建议小写英文字母和数字组成
     * @throws |Exception
     */
    public static function init($projectname)
    {
        //检测是否启用
        if (!self::isEnable()) {
            return;
        }

        //is init already
        if (self::$_isinit == false) {

            //idc decide
            if (isset($_SERVER["WEIBO_ADINF_IDC"]) && trim($_SERVER["WEIBO_ADINF_IDC"]) !== "" &&
                $_SERVER["WEIBO_ADINF_IDC"] >= 0 && $_SERVER["WEIBO_ADINF_IDC"] <= 3
            ) {
                self::$_idc = trim($_SERVER["WEIBO_ADINF_IDC"]) . "";
            } else {
                throw new \Exception("Ragnar:WEIBO_ADINF_IDC取值 0~3 ，请检查Nginx配置选项", 1000);
            }

            //workmode 工作模式 根据工作模式决定输出日志的topic名称
            if (isset($_SERVER["WEIBO_ADINF_ENV"]) && $_SERVER["WEIBO_ADINF_ENV"] != "") {
                self::$_workmode = $_SERVER["WEIBO_ADINF_ENV"];

                //project name by the work mode
                switch (self::$_workmode) {
                    case RagnarConst::MODE_ONLINE:
                        //线上项目会推送到 项目名_log 项目名_meta 此为默认选项
                        //project name
                        if (strlen($projectname) != 0) {
                            self::$_project = trim($projectname);
                        } else {
                            throw new \Exception("Ragnar:请指定init初始化的项目名称", 1001);
                        }
                        break;

                    case RagnarConst::MODE_DEV:
                        //开发环境会自动推送到ragnar_dev_meta ragnar_dev_log
                        self::$_project = "ragnar_dev";
                        //change the mc time out for test env
                        self::$_mctimeout = 200;
                        break;

                    case RagnarConst::MODE_TEST:
                        //测试会推送到测试管理界面ragnar_test_meta ragnar_test_log
                        self::$_project = "ragnar_dev";
                        break;
                    default:
                        throw new \Exception("Ragnar:请指定正确的Nginx配置项其中WEIBO_ADINF_ENV 目前只支持 dev prod test", 1002);
                }
            } else {
                throw new \Exception("Ragnar:请指定项目名称，请检查Nginx配置选项", 1003);
            }


            //get server ip
            if (isset($_SERVER["WEIBO_ADINF_SERVERIP"]) && $_SERVER["WEIBO_ADINF_SERVERIP"] != "" &&
                count(explode(".", $_SERVER["WEIBO_ADINF_SERVERIP"])) == 4
            ) {
                self::$_ip = $_SERVER["WEIBO_ADINF_SERVERIP"];
            } else {
                throw new \Exception("Ragnar:服务器IP配置错误，请检查Nginx配置选项", 1004);
            }

            //mark flag
            self::$_isinit = true;

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

            //if set the xhprof
            if (!empty($_SERVER["HTTP_X_RAGNAR_XHPROF"]) && $_SERVER["HTTP_X_RAGNAR_XHPROF"] == "1") {
                $xhproftime = 0;

                //if set the xhprof
                if (!empty($_SERVER["HTTP_X_RAGNAR_XHPROFTIME"])) {
                    $xhproftime = $_SERVER["HTTP_X_RAGNAR_XHPROFTIME"];
                }
                //start record xhprof
                self::startXhprof($xhproftime);
            }

            //check rand xhprof option
            //if the xhprof already opend will ignore
            if (isset($_SERVER["X-RAGNAR-XHPROF-CHANCE"]) && $_SERVER["X-RAGNAR-XHPROF-CHANCE"] != "") {
                $randmax = intval($_SERVER["X-RAGNAR-XHPROF-CHANCE"]);
                self::startRandXhprof($randmax);
            }

            //traceid rpcid
            header("X-RAGNAR-TRACEID: " . self::$_traceid);
            header("X-RAGNAR-RPCID: " . self::$_rpcid);

            //shutdown process
            register_shutdown_function(function () {
                self::shutdown();
            });
        }

    }

    /**
     * 此功能不是给任何开发使用的
     * 只是用来此SDK开发测试使用
     * 任何业务代码禁止调用
     */
    public static function devmode()
    {
        self::$_idc = "0";
        self::$_workmode = "dev";
        self::$_project = "ragnar_dev";
        self::$_writemode = "file";
        self::$_ip = "11.11.11.11";
        //self::$_compress = true;
        self::$_isinit = true;
        self::$_starttime = microtime(true);
        self::$_devmode = true;
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
     * 如果之前没有执行过startXHprof
     * 执行这个会随机决定是否启用xhprof
     * @param int $maxrand 一个大于2的整数，随机取值范围最大值
     */
    public static function startRandXhprof($maxrand)
    {
        //检测是否可用
        if (!self::isEnable() || $maxrand < 2) {
            return;
        }

        if (mt_rand(0, $maxrand) == 1) {
            self::startXhprof(0);
        }
    }

    /**
     * 如使用必须在init调用之前执行，用途是启动xhprof记录
     * 如需要记录则init后调用一次即可，多次调用只以第一次调用为准
     * @param int $time 指定超过多长时间才记录xhprof日志，若为0则全量记录
     */
    public static function startXhprof($time = 0)
    {
        //检测是否初始化并且未禁用
        if (!self::isEnable()) {
            return;
        }

        self::$_xhproftime = $time;

        //record performance log
        if (self::$_recordXhprof == "0" && function_exists("xhprof_enable")) {
            self::$_xhproftime = $time;
            self::$_recordXhprof = "1";
            xhprof_enable(XHPROF_FLAGS_NO_BUILTINS);

        }
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
     * 性能埋点开始，此函数会返回一组数据，这个数据是给digLogEnd函数使用的
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
     * 性能埋点结束，传入之前埋点函数返回的数据到这里即可产生日志
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
        //replace special char
        //$msg = str_replace(array("\n", "\r", "\x02"), "", $msg);

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
            "result" => strlen($result) > 15480 ? substr($result, 0, 15480) : $result,    //长度请勿超过20480,超过切断,使用者如果很在意数据可以去掉这个功能，但过长会有问题
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
            "X-RAGNAR-XHPROF" => self::$_recordXhprof,
            "X-RAGNAR-XHPROFTIME" => self::$_xhproftime,
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
            "X-RAGNAR-XHPROF: " . self::$_recordXhprof,
            "X-RAGNAR-XHPROFTIME: " . self::$_xhproftime,
        );

        if (isset($digpoint["rpcid"])) {
            $headers[] = "X-RAGNAR-RPCID: " . $digpoint["rpcid"];
        }else{
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
        //dump xhroflog
        self::DumpXhprofLog();
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
            "perf_on" => self::$_recordXhprof,
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

    private static function DumpXhprofLog()
    {
        if (self::$_recordXhprof == "1" && function_exists("xhprof_disable")) {

            //when the xhprof time setup less than threadhold will not record
            if (self::$_xhproftime != 0 && bcsub(microtime(true), self::$_starttime, 4) <= self::$_xhproftime) {
                return;
            }

            $content = array("t" => RagnarConst::LOG_TYPE_XHPROF, "m" => xhprof_disable());

            $data = array(
                array(
                    "key" => self::getTraceID(),
                    "rpcid" => self::getCurrentRPCID(),
                    "val" => array($content),
                    "timestamp" => time(),
                ),
            );

            $logstr = json_encode($data);

            //save the performance
            self::writeMsg(self::$_project, "log", $logstr);
        }
    }


    private static function getMsgDir($topic)
    {

        if (!isset($_SERVER["WEIBO_ADINF_DEFAULT_LOGPATH"])) {
            //$_rootPath = "/tmp";
            $_rootPath = '/tmp';

        } else {
            $_rootPath = $_SERVER["WEIBO_ADINF_DEFAULT_LOGPATH"];
        }

        if (!isset($_SERVER["WEIBO_ADINF_BRAGI_MSG_FOLDER"])) {
            $_msgPath = "bragimsg";
        } else {
            $_msgPath = $_SERVER["WEIBO_ADINF_BRAGI_MSG_FOLDER"];
        }

        return $_rootPath . "/" . $_msgPath . "/" . trim($topic) . "/" . date("Ym") . "/";
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
        if (self::$_writemode == RagnarConst::WRITE_MODE_FILE) {

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

        } else if (self::$_writemode == RagnarConst::WRITE_MODE_NONE) {
            //do nothing
            return false;
        }
        return false;
    }

}
