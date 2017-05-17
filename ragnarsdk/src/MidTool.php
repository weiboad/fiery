<?php

namespace Adinf\RagnarSDK;

/**
 * Class MidTool
 * 将数值转成Mid
 */
class MidTool
{
    private static $string = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    private static $encodeBlockSize = 7;
    private static $decodeBlockSize = 4;


    /**
     * 将 mid 转化成 62 进制字符串
     *
     * @param mixed $mid
     * @access public
     * @return string
     */
    public static function encode($mid)
    {
        $str = '';
        $midlen = strlen($mid);
        $segments = ceil($midlen / self::$encodeBlockSize);
        $start = $midlen;
        for ($i = 1; $i < $segments; $i += 1) {
            $start -= self::$encodeBlockSize;
            $seg = substr($mid, $start, self::$encodeBlockSize);
            $seg = self::encodeSegment($seg);
            $str = str_pad($seg, self::$decodeBlockSize, '0', STR_PAD_LEFT) . $str;
        }
        $str = self::encodeSegment(substr($mid, 0, $start)) . $str;
        return $str;
    }

    /**
     * 将62进制字符串转成10进制mid
     *
     * @param mixed $str
     * @param mixed $compat
     * @param mixed $forMid
     * @access public
     * @return string
     */
    public static function decode($str, $compat = false, $forMid = true)
    {
        $mid = '';
        $strlen = strlen($str);
        $segments = ceil($strlen / self::$decodeBlockSize);
        $start = $strlen;
        for ($i = 1; $i < $segments; $i += 1) {
            $start -= self::$decodeBlockSize;
            $seg = substr($str, $start, self::$decodeBlockSize);
            $seg = self::decodeSegment($seg);
            $mid = str_pad($seg, self::$encodeBlockSize, '0', STR_PAD_LEFT) . $mid;
        }
        $mid = self::decodeSegment(substr($str, 0, $start)) . $mid;
        if ($compat && !in_array(substr($mid, 0, 3), array('109', '110', '201', '211', '221', '231', '241'))) {
            $mid = self::decodeSegment(substr($str, 0, 4)) . self::decodeSegment(substr($str, 4));
        }
        if ($forMid) {
            if (substr($mid, 0, 1) == '1' && substr($mid, 7, 1) == '0') {
                $mid = substr($mid, 0, 7) . substr($mid, 8);
            }
        }
        return $mid;
    }

    /**
     * 将10进制转换成62进制
     *
     * @param mixed $str
     * @static
     * @access private
     * @return string
     */
    private static function encodeSegment($str)
    {
        $out = '';
        while ($str > 0) {
            $idx = $str % 62;
            $out = substr(self::$string, $idx, 1) . $out;
            $str = floor($str / 62);
        }
        return $out;
    }

    /**
     * 将62进制转换成10进制
     *
     * @param mixed $str
     * @access private
     * @return string
     */
    private static function decodeSegment($str)
    {
        $out = 0;
        $base = 1;
        for ($t = strlen($str) - 1; $t >= 0; $t -= 1) {
            $out = $out + $base * strpos(self::$string, substr($str, $t, 1));
            $base *= 62;
        }
        return $out . "";
    }

}