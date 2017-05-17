<?php
namespace Adinf\RagnarSDK;

/**
 * 用于Ragnar的系统常量
 * @author: changlong1
 */

class RagnarConst
{
    /**
     * 分级日志 日志级别常量定义
     */
    //debug 信息，用于调试信息输出，默认不会输出，当在生产环境在线调试时使用
    const LOG_TYPE_DEBUG = 1;

    //trace
    const LOG_TYPE_TRACE = 2;

    //notice
    const LOG_TYPE_NOTICE = 3;

    //info 信息
    const LOG_TYPE_INFO = 4;

    //错误 信息
    const LOG_TYPE_ERROR = 5;

    //警报 信息
    const LOG_TYPE_EMEGENCY = 6;

    //异常 信息
    const LOG_TYPE_EXCEPTION = 7;

    /**
     * 日志特殊类型
     */
    //日志类型：xhprof性能日志
    const LOG_TYPE_XHPROF = 8;

    //日志类型：耗时性能日志
    const LOG_TYPE_PERFORMENCE = 9;

    /**
     * 运行环境定义
     * test 测试环境，使用ragnar_test_log ragnar_test_meta作为topic
     * dev 开发环境，使用ragnar_dev_log ragnar_dev_meta作为topic
     * prod 生产环境,使用ragnar_项目名_log ragnar_项目名_meta作为topic
     */
    //开发环境定义
    const MODE_DEV = "dev";

    //测试环境定义
    const MODE_TEST = "test";

    //线上环境定义
    const MODE_ONLINE = "prod";

    /**
     * 日志写入方式
     * file 写入指定目录文件，具体目录在nginx环境变量内指定
     * mc 写入日志到mc协议的logagent，topic取决于Ragnar工作模式
     * none 日志不做任何写入，直接返回
     */
    //所有日志写入文件
    const WRITE_MODE_FILE = "file";

    //所有日志写入MC
    const WRITE_MODE_MC = "mc";

    //不生成Ragnar日志
    const WRITE_MODE_NONE = "none";
}