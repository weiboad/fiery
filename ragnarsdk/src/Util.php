<?php

namespace Adinf\RagnarSDK;

/**
 * Class        Util
 *
 * @package     Adinf\RagnarSDK
 * @author      huangchen
 * @version     0.1
 */
class Util
{

    /**
     * 修正过的ip2long
     *
     * 可去除ip地址中的前导0。32位php兼容，若超出127.255.255.255，则会返回一个float
     *
     * for example: 02.168.010.010 => 2.168.10.10
     *
     * 处理方法有很多种，目前先采用这种分段取绝对值取整的方法吧……
     * @param string $ip
     * @return float 使用unsigned int表示的ip。如果ip地址转换失败，则会返回0
     */
    public static function ip2long($ip)
    {
        $ip_chunks = explode('.', $ip, 4);
        foreach ($ip_chunks as $i => $v) {
            $ip_chunks[$i] = abs(intval($v));
        }
        return sprintf('%u', ip2long(implode('.', $ip_chunks)));
    }

    /**
     * 判断是否是内网ip
     *
     * @param string $ip
     *
     * @return boolean
     */
    public static function isPrivateIp($ip)
    {
        $ip_value = self::ip2long($ip);
        return ($ip_value & 0xFF000000) === 0x0A000000 || //10.0.0.0-10.255.255.255
            ($ip_value & 0xFFF00000) === 0xAC100000 || //172.16.0.0-172.31.255.255
            ($ip_value & 0xFFFF0000) === 0xC0A80000; //192.168.0.0-192.168.255.255
    }

    /**
     * 获取真实的客户端ip地址
     *
     * This function is copied from login.sina.com.cn/module/libmisc.php/get_ip()
     *
     * @param boolean $to_long 可选。是否返回一个unsigned int表示的ip地址
     *
     * @return mixed string or float 客户端ip。如果to_long为真，则返回一个unsigned int表示的ip地址；否则，返回字符串表示。
     */
    public static function getClientIp($to_long = false)
    {
        $forwarded = self::getServer('HTTP_X_FORWARDED_FOR');
        if ($forwarded) {
            $ip_chains = explode(',', $forwarded);
            $proxied_client_ip = $ip_chains ? trim(array_pop($ip_chains)) : '';
        }

        if (self::isPrivateIp(self::getServer('REMOTE_ADDR')) && isset($proxied_client_ip)) {
            $real_ip = $proxied_client_ip;
        } else {
            $real_ip = self::getServer('REMOTE_ADDR');
        }

        return $to_long ? self::ip2long($real_ip) : $real_ip;
    }

    /**
     * 获取当前Referer
     *
     * @return string
     */
    public static function getReferer()
    {
        return self::getServer('HTTP_REFERER');
    }

    /**
     * 获取当前域名
     *
     * @return string
     */
    public static function getDomain()
    {
        return self::getServer('SERVER_NAME');
    }

    /**
     * 得到当前请求的环境变量
     *
     * @param string $name
     *
     * @return mixed string or null 当$name指定的环境变量不存在时，返回null
     */
    public static function getServer($name)
    {
        return isset($_SERVER[$name]) ? $_SERVER[$name] : null;
    }

    /**
     * 获取当前ua
     * Method  getUserAgent
     *
     * @return string
     */
    public static function getUserAgent()
    {
        return self::getServer('HTTP_USER_AGENT');
    }

    /**
     * 返回当前url
     *
     * @param boolean $urlencode 是否urlencode后返回，默认true
     *
     * @return string
     */
    public static function getCurrentUrl($urlencode = true)
    {
        $req_uri = self::getServer('REQUEST_URI');
        if (null === $req_uri) {
            $req_uri = self::getServer('PHP_SELF');
        }

        $https = self::getServer('HTTPS');
        $s = null === $https ? '' : ('on' == $https ? 's' : '');

        $protocol = self::getServer('SERVER_PROTOCOL');
        $protocol = strtolower(substr($protocol, 0, strpos($protocol, '/'))) . $s;

        $port = self::getServer('SERVER_PORT');
        $port = ($port == '80') ? '' : (':' . $port);

        $server_name = self::getServer('SERVER_NAME');
        $current_url = $protocol . '://' . $server_name . $port . $req_uri;

        return $urlencode ? rawurlencode($current_url) : $current_url;
    }

    /**
     * 返回当前uri
     *
     * @return string
     */
    public static function getCurrentUri()
    {
        $req_uri = self::getServer('REQUEST_URI');
        if (null === $req_uri) {
            $req_uri = self::getServer('PHP_SELF');
        }

        return $req_uri;
    }

    /**
     * 获得服务器本地ip
     *
     * @return string
     */
    public static function getServerIp()
    {
        $exec = "/sbin/ifconfig | grep 'inet addr' | awk '{ print $2 }' | awk -F ':' '{ print $2}' | head -1";
        $fp = @popen($exec, 'r');
        $ip = trim(@fread($fp, 2096));
        @pclose($fp);
        if (preg_match('/^[0-9\.]+$/', $ip)) {
            return $ip;
        } else {
            return '1.1.1.1';
        }
    }

    /**
     * Method  getSapi
     *
     * @author huangchen
     * @static
     * @return string
     */
    public static function getSapi()
    {
        if (function_exists('php_sapi_name')) {
            return php_sapi_name();
        } else if (defined(PHP_SAPI)) {
            return PHP_SAPI;
        } else {
            return 'unknown';
        }
    }

}
