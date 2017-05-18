<?php
namespace Adinf\RagnarSDK;

/**
 * Ragnar Const Define
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

}