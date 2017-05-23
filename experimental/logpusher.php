<?php
/*
	experimental logpusher
	only for test
*/
ini_set("display_error", "On");
error_reporting(E_ALL);
ini_set('memory_limit', '2048M');

$fileInfo = array();
$sendBizLogQueue = array();
$sendMetaLogQueue = array();

/**
 * get the file new append content
 * @param $file
 * @param $modifyTime
 */
function fetchFileAppendContent($file, $modifyTime)
{
    global $fileInfo;
    global $sendBizLogQueue;
    global $sendMetaLogQueue;

    clearstatcache();

    //not found
    if (!file_exists($file)) {
        unset($fileInfo[$file]);
        echo "=== File Not Found ... " . PHP_EOL;
        return;
    }

    //get the file size
    $fileSize = filesize($file);

    //have record
    if (isset($fileInfo[$file])) {
        //old file
        $offset = $fileInfo[$file]["offset"];
        $fileInfo[$file]["lastupdate"] = $modifyTime;
    } else {
        //new file
        $fileInfo[$file] = array(
            "offset" => 0,
            "lastupdate" => $modifyTime,
            "size" => $fileSize,
        );
        $offset = 0;
    }

    //file have been move or empty
    if ($offset > $fileSize) {
        $offset = 0;
    }

    $processedCount = 0;
    $fh = fopen($file, "r");
    fseek($fh, $offset);

    while ($d = fgets($fh)) {
        $processedCount++;
        if ($processedCount > 100) {
            break;
        }
        $offset = bcadd($offset, strlen($d), 0);
        //if it's json(must biz log)
        if ($d[0] != "[") {
            $sendMetaLogQueue[] = $d;
        } else {
            //put to the queue
            $sendBizLogQueue[] = $d;
        }
    }

    $fileInfo[$file]["offset"] = $offset;
    fclose($fh);
}

/**
 * scan the folder the new update file
 * @param $path
 */
function scanRecentUpdateFile($path)
{
    clearstatcache();

    $filelist = glob($path . "/*");

    foreach ($filelist as $filepath) {

        $filepath = realpath($filepath);

        if (is_dir($filepath)) {
            scanRecentUpdateFile($filepath);
        } else {
            $modifyTime = filemtime($filepath);

            //20 second have been modify
            if ($modifyTime > time() - 3600) {
                //echo "==== Found File:" . $filepath . PHP_EOL;
                fetchFileAppendContent($filepath, $modifyTime);
            }
        }
    }
}

function pushBizLogToServer($url)
{
    global $sendBizLogQueue;

    $sendList = array();

    $pushCount = 0;

    foreach ($sendBizLogQueue as $key => $log) {
        $sendList[] = $log;
        unset($sendBizLogQueue[$key]);
        $pushCount++;
        if ($pushCount > 100) {
            break;
        }
    }

    if (count($sendList) > 0) {
        $postdata = implode("", $sendList);

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_HTTPHEADER, array('Expect:'));
        curl_setopt($ch, CURLOPT_POSTFIELDS, array("contents" => ($postdata)));

        $output = curl_exec($ch);
        curl_close($ch);

        var_dump($output);
    }
}

function pushMetaLogToServer($url)
{
    global $sendMetaLogQueue;

    $sendList = array();

    $pushCount = 0;

    foreach ($sendMetaLogQueue as $key => $log) {
        $sendList[] = $log;
        unset($sendMetaLogQueue[$key]);
        $pushCount++;
        if ($pushCount > 100) {
            break;
        }
    }

    if (count($sendList) > 0) {
        $postdata = implode("", $sendList);

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_HTTPHEADER, array('Expect:'));
        curl_setopt($ch, CURLOPT_POSTFIELDS, array("contents" => $postdata));

        $output = curl_exec($ch);
        curl_close($ch);

        var_dump($output);
    }
}

/**
 * remove the old file on the list
 */
function cleanupOldFileInfoList()
{
    global $fileInfo;

    clearstatcache();
    foreach ($fileInfo as $filepath => $info) {
        if (!file_exists($filepath)) {
            unset($fileInfo[$filepath]);
            echo "=== File Was Delete:" . $filepath . PHP_EOL;
        }

        // there is no update 60 minute
        //if (filemtime($filepath) < time() - 3600) {
        //unset($fileInfo[$filepath]);
        //}
    }
}

/**
 * main
 */

if ($argc == 3) {
	$path = trim($argv[1]);
	$posthost = trim($argv[2]);
    while (true) {
        scanRecentUpdateFile($path);
        pushBizLogToServer("http://" . $posthost . "/ragnar/log/bizlog/put");
        pushMetaLogToServer("http://" . $posthost . "/ragnar/log/metalog/put");
        cleanupOldFileInfoList();
        usleep(200);
    }
} else {
    echo "cmd: php logpusher.php path posturl";
    exit;
}
