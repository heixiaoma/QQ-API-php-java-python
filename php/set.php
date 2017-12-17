<?php
include "Qzone.php";
header("Content-type: text/html; charset=utf-8");
$adder = "qq_msg.json";

$myfile = fopen($adder, "r") or die("无相关信息");
$res = fread($myfile, filesize($adder));
$msg = json_decode($res, true);
while(true) {
    $qzone = new Qzone($msg["cookie"], $msg["qq"]);
//调用点赞，传入评论内容，不传代表不评论
    $r = $qzone->get_zan($msg["content"]);
    print_r($r);
}
?>