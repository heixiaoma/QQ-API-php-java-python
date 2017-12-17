<?php
/**
 * Created by PhpStorm.
 * User: asus
 * Date: 2017/11/16
 * Time: 16:50
 */
$qq=$_POST["qq"];
$con=$_POST["content"];
$cookie=$_POST["cookie"];
if($qq!=""&$cookie!=""){


//处理json文件
    $str="{\"qq\": \"".$qq."\",\"cookie\": \"".$cookie."\",\"content\": \"".$con."\"}";
    $fopen=fopen("qq_msg.json","wb")or die('文件不在');
    fwrite($fopen,$str);
    fclose($fopen);
    header("Location: index.html");


}else{

}