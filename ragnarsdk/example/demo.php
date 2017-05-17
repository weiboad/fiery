<?php

error_reporting(E_ALL);
ini_set("display_errors","On");

//任何人禁止调用此函数
\Adinf\RagnarSDK\RagnarSDK::devmode();

//这俩必须在init之前
//设置业务日志等级
\Adinf\RagnarSDK\RagnarSDK::setLogLevel(\Adinf\RagnarSDK\RagnarConst::LOG_TYPE_INFO);

//是否开启xhprof日志统计，如果开启会影响性能，开发时可以使用
//参数为指定超过多长时间才记录xhprof日志，若为0则全量记录
\Adinf\RagnarSDK\RagnarSDK::startXhprof(0);

//初始化ragnar项目
\Adinf\RagnarSDK\RagnarSDK::init("ragnar_projectname");

//设置要索引的日志附加数据，在ES搜索内能看到，不建议加太多
\Adinf\RagnarSDK\RagnarSDK::setMeta(123, "", array("extrakey" => "extraval"));

\Adinf\RagnarSDK\RagnarSDK::RecordLog(\Adinf\RagnarSDK\RagnarConst::LOG_TYPE_INFO, __FILE__, __LINE__, "module1_msg", "i wish i can fly!");
\Adinf\RagnarSDK\RagnarSDK::RecordLog(\Adinf\RagnarSDK\RagnarConst::LOG_TYPE_DEBUG, __FILE__, __LINE__, "module2_msg", "i wish i'm rich!");

$digpooint = \Adinf\RagnarSDK\RagnarSDK::digLogStart(__FILE__, __LINE__, "ragnar_test");
\Adinf\RagnarSDK\RagnarSDK::digLogEnd($digpooint, "happy");

$a = \Adinf\RagnarSDK\RagnarSDK::getChildCallParam();

//url 内包含变量替换注册函数演示
$url = "http://dev.weibo.c1om/v1/log/12312312/lists.json?a=1";


\Adinf\RagnarSDK\RagnarSDK::setUrlFilterCallback(function ($url, $hashquery) {
    if (trim($url) == "") {
        return "";
    }
    if (stripos($url, 'http') !== 0) {
        $url = "http://" . $url;
    }

    $urlinfo = parse_url($url);

    if(!$urlinfo){
        return $url."#PARSERERROR";
    }

    if (!isset($urlinfo["scheme"])) {
        $urlinfo["scheme"] = "http";
    }

    if (!isset($urlinfo["path"])) {
        $urlinfo["path"] = "/";
    }

    if (!isset($urlinfo["query"])) {
        $urlinfo["query"] = "";
    }

    if (isset($urlinfo["host"]) && ($urlinfo["host"] == "dev.weibo.com" || $urlinfo["host"] == "biz.weibo.com")) {
        $pathinfo = explode("/", $urlinfo["path"]);
        if (count($pathinfo) == 5) {
            $pathinfo[3] = "filted";//统一更换成固定字符
            $pathinfo    = implode("/", $pathinfo);
            $url         = $urlinfo["scheme"] . "://" . $urlinfo["host"] . $pathinfo;
            if ($hashquery) {
                $url .= "?" . $urlinfo["query"];
            }

            return $url;
        }
    }

    if (isset($urlinfo["host"]) && $urlinfo["host"] == "10.1.1.1" ) {
        if(stripos($urlinfo["path"],"/mid=")===0){
            $mid = substr($urlinfo["path"],6);
            $urlinfo["path"] = "/mid/";
            $urlinfo["query"] = "mid=".$mid;
        }
    }

    if ($hashquery) {
        return $urlinfo["scheme"] . "://" . $urlinfo["host"] . $urlinfo["path"] . "?" . $urlinfo["query"];
    } else {
        return $urlinfo["scheme"] . "://" . $urlinfo["host"] . $urlinfo["path"];
    }
});
var_dump(\Adinf\RagnarSDK\RagnarSDK::getTraceID());
var_dump(\Adinf\RagnarSDK\RagnarSDK::decodeTraceID(\Adinf\RagnarSDK\RagnarSDK::getTraceID()));
