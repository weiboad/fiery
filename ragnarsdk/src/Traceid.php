<?php
namespace Adinf\RagnarSDK;

/**
 * Class Traceid
 * @package Adinf\RagnarSDK
 * @author zhongxiu
 * Traceid UUID生成算法
 */

class Traceid
{

    const BIT_B64 = 'N2';
    const BIT_B32 = 'N';
    const BIT_B16 = 'n';
    const BIT_B16_SIGNED = 's';
    const BIT_B8 = 'C';

    public static function encode($idc, $ip, $time, $ms, $rand)
    {
        $init = self::pack(self::BIT_B64, 0x0000000000000000);
        $idc = self::pack(self::BIT_B64, $idc << 62);
        $ip = self::pack(self::BIT_B64, $ip << 46);
        $time = self::pack(self::BIT_B64, $time << 18);
        $ms = self::pack(self::BIT_B64, $ms << 8);
        $rand = self::pack(self::BIT_B64, $rand);
        $init |= $idc;
        $init |= $ip;
        $init |= $time;
        $init |= $ms;
        $init |= $rand;

        //var_dump(bin2hex($init));
        $traceid = self::unpack(self::BIT_B64, $init);
        return $traceid;
    }


    /**
     * @param $traceid
     * @return array
     */
    public static function decode($traceid)
    {
        // 4 => 8
        $rand = self::pack(self::BIT_B64, $traceid);
        $mask = self::pack(self::BIT_B64, 0x00000000000000ff);
        $rand = self::unpack(self::BIT_B64, $mask & $rand);

        // 14 => 10
        $ms = self::pack(self::BIT_B64, $traceid >> 8);
        $mask = self::pack(self::BIT_B64, 0x00000000000003ff);
        $ms = self::unpack(self::BIT_B64, $mask & $ms);

        // time 28
        $time = self::pack(self::BIT_B64, $traceid >> 18);
        $mask = self::pack(self::BIT_B64, 0x000000000fffffff);
        $time = self::unpack(self::BIT_B64, $mask & $time);

        // ip 16
        $ip = self::pack(self::BIT_B64, $traceid >> 46);
        $mask = self::pack(self::BIT_B64, 0x000000000000ffff);
        $ip = self::unpack(self::BIT_B64, $mask & $ip);

        // idc 2
        $idc = self::pack(self::BIT_B64, $traceid >> 62);
        $mask = self::pack(self::BIT_B64, 0x0000000000000003);
        $idc = self::unpack(self::BIT_B64, $mask & $idc);

        return array(
            'rand' => $rand,
            'ms' => $ms,
            'time' => $time,
            'ip' => $ip,
            'idc' => $idc,
        );
    }

    public static function Khex2bin($string)
    {
        if (function_exists('\hex2bin')) {
            return \hex2bin($string);
        } else {
            $bin = '';
            $len = strlen($string);
            for ($i = 0; $i < $len; $i += 2) {
                $bin .= pack('H*', substr($string, $i, 2));
            }
            return $bin;
        }
    }

    public static function unpack($type, $bytes)
    {
        if ($type == self::BIT_B64) {
            $set = unpack($type, $bytes);
            $original = ($set[1] & 0xFFFFFFFF) << 32 | ($set[2] & 0xFFFFFFFF);
            return $original;
        } else {
            return unpack($type, $bytes);
        }
    }

    public static function pack($type, $data)
    {
        if ($type == self::BIT_B64) {
            if ($data == -1) { // -1L
                $data = self::Khex2bin('ffffffffffffffff');
            } elseif ($data == -2) { // -2L
                $data = self::Khex2bin('fffffffffffffffe');
            } else {
                $left = 0xffffffff00000000;
                $right = 0x00000000ffffffff;
                $l = ($data & $left) >> 32;
                $r = $data & $right;
                $data = pack($type, $l, $r);
            }
        } else {
            $data = pack($type, $data);
        }
        return $data;
    }
}
