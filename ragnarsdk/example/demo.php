<?php
require_once "../src/MidTool.php";
require_once "../src/RagnarConst.php";
require_once "../src/RagnarSDK.php";
require_once "../src/Traceid.php";
require_once "../src/Util.php";

use \Adinf\RagnarSDK\RagnarSDK as RagnarSDK;
use \Adinf\RagnarSDK\RagnarConst as RagnarConst;

error_reporting(E_ALL);
ini_set("display_errors", "On");


//这俩必须在init之前
//设置业务日志等级
RagnarSDK::setLogLevel(RagnarConst::LOG_TYPE_INFO);

//初始化ragnar项目 实际生产环境用这个初始化,仅限FPM工作
//\Adinf\RagnarSDK\RagnarSDK::init("ragnar_projectname");

//命令行测试使用，生产环境不适用
RagnarSDK::devmode("ragnar_projectname");

//设置要索引的日志附加数据，在ES搜索内能看到，不建议加太多
RagnarSDK::setMeta(123, "", array("extrakey" => "extraval"));

RagnarSDK::RecordLog(RagnarConst::LOG_TYPE_INFO, __FILE__, __LINE__, "module1_msg", "i wish i can fly!");
RagnarSDK::RecordLog(RagnarConst::LOG_TYPE_DEBUG, __FILE__, __LINE__, "module2_msg", "i wish i'm rich!");

$digpooint = RagnarSDK::digLogStart(__FILE__, __LINE__, "ragnar_test");
RagnarSDK::digLogEnd($digpooint, "happy");

$a = RagnarSDK::getChildCallParam();

//url 内包含变量替换注册函数演示
$url = "http://dev.weibo.c1om/v1/log/12312312/lists.json?a=1";

$filterURL = function ($url, $hashquery) {
    return $url;
};

RagnarSDK::setUrlFilterCallback($filterURL);

var_dump(RagnarSDK::getTraceID());
var_dump(RagnarSDK::decodeTraceID(RagnarSDK::getTraceID()));
