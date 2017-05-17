<?php
function postcurl($url,$content){

	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, $url);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	// post数据
	curl_setopt($ch, CURLOPT_POST, 1);
	// post的变量
	curl_setopt($ch, CURLOPT_POSTFIELDS, $content);
	$output = curl_exec($ch);
	curl_close($ch);
	return $output;
}
$errorcount = 0;
for($i=0;$i< 10000 ; $i++)
{
	$teststring = '{"version":"v0.1","rpcid":"0","traceid":"'.md5(microtime(true).mt_rand(0,10000000)).'","time":'.microtime(true).',"@timestamp":"2016-12-26T16:20:15Z","elapsed_ms":"'.mt_rand(0,100).'.01","perf_on":"0","ip":"10.75.26.95","rt_type":"fpm-fcgi","uid":"0","url":"promote.biz.weibo.cn\/members\/promotenew'.mt_rand(0,100).'","param":"{\"get\":{\"members\\\/promotenew\":\"\",\"mid\":\"'.mt_rand(100000,9999999).'\",\"members\\\/promote\":\"\",\"gsid\":\"_2A251WwYfDeRxGeRK6VQY-C_EzTyIHXVUMR7XrDV6PUJbkdAKLVfhkWpJot2bCqdjcYF-bUoazEyegi_ekg..\",\"wm\":\"3333_2001\",\"FP\":\"read_profile_01\",\"i\":\"b9f6cfa\",\"b\":\"1\",\"from\":\"106C293010\",\"c\":\"iphone\",\"v_p\":\"41\",\"skin\":\"default\",\"disable_sinaurl\":\"1\",\"lang\":\"zh_CN\",\"ua\":\"iPhone7,2__weibo__6.12.2__iphone__os10.2\"},\"post\":[]}","httpcode":302,"project":"ragnar_fentiao","extra":[]}';

var_dump($teststring);
	$data = array(
			"contents" => urlencode($teststring),
			);
	$ret = postcurl("http://127.0.0.1:8888/ragnar/appendindex",$data);
	if(!$ret){
		$errorcount ++;
	}
}
echo "errorcount:".$errorcount."\r\n";
