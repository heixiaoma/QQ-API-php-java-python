<?php
/**
 * Created by PhpStorm.
 * User: 黑小马
 * Date: 2017/11/15
 * Time: 22:35
 * 首先：1，保证你的环境有Curl，2，保证php环境关闭了安全模式，vps可以去php.ini设置safe_mode=OFF
 */
class Qzone{

    private $cookie;
    private $qq;
    private $url;
    private $temp=array();
    private $curkey=array();
    private $unikey=array();
    private $uin=array();
    private $tid=array();
    private $g_tk;
    private $qzone_token;
    //构造初始化
    function Qzone($cookie,$qq)
    {
        $this->url="https://user.qzone.qq.com/".$qq."/infocenter?via=toolbar";;
        $this->cookie=$cookie;
        $this->qq=$qq;
        $this->g_tk=$this->get_GTK($cookie);
    }
    //网络请求方法
    private  function curl_request($url,$post='',$cookie='', $returnCookie=0)
    {

        $curl = curl_init();
        curl_setopt($curl, CURLOPT_URL, $url);
        curl_setopt($curl, CURLOPT_USERAGENT, 'Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)');
        if (ini_get('open_basedir') == '' && ini_get('safe_mode' == 'Off')) {

            curl_setopt($curl, CURLOPT_FOLLOWLOCATION, 1);

        }
        curl_setopt($curl, CURLOPT_AUTOREFERER, 1);
        curl_setopt($curl, CURLOPT_HEADER, 0);  //0表示不输出Header，1表示输出
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);    //设定是否显示头信息,1显示，0不显示。
        //如果成功只将结果返回，不自动输出任何内容。如果失败返回FALSE
        curl_setopt($curl, CURLOPT_REFERER, "http://XXX");
        curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, false);
        if ($post) {
            curl_setopt($curl, CURLOPT_POST, 1);
            curl_setopt($curl, CURLOPT_POSTFIELDS, $post);
        }
        if ($cookie) {
            curl_setopt($curl, CURLOPT_COOKIE, $cookie);
        }
        curl_setopt($curl, CURLOPT_HEADER, $returnCookie);
        curl_setopt($curl, CURLOPT_TIMEOUT, 10);
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
        $data = curl_exec($curl);
        if (curl_errno($curl)) {
            return curl_error($curl);
        }
        curl_close($curl);
        if ($returnCookie) {
            list($header, $body) = explode("\r\n\r\n", $data, 2);
            preg_match_all("/Set\-Cookie:([^;]*);/", $header, $matches);
            $info['cookie'] = substr($matches[1][0], 1);
            $info['content'] = $body;
            return $info;
        } else {
            return $data;
        }
    }
    //获取最新说说
    public function get_Msg()
    {
        $str=$this->curl_request($this->url,"",$this->cookie,0);
        preg_match_all("/<div class=\"f-info\">(.*?)<\/div>/",$str,$regs);
        return $this->get_Array($regs);
    }
    //点赞方法,并评论
    public function get_zan($con=0)
    {
        $res=array();
        $this->get_no_dianzan();
        for($i=0;$i<count($this->unikey);$i++){
            preg_match("/.{24}$/",$this->curkey[$i],$fid);
            $post="qzreferrer=https%3A%2F%2Fuser.qzone.qq.com%2F".$this->qq."&opuin=".$this->qq."&unikey=".$this->unikey[$i]."&curkey=".$this->curkey[$i]."&from=1&appid=311&typeid=0&fid=".$fid[0]."&active=0&fupdate=1";
            $dianzan_url="https://user.qzone.qq.com/proxy/domain/w.qzone.qq.com/cgi-bin/likes/internal_dolike_app?g_tk=".$this->g_tk;
            $re=$this->curl_request($dianzan_url,$post,$this->cookie,0);
            if($con){
            $this->send_discuss($con,$this->uin[$i],$this->tid[$i]);
            }
            $res[]=array("点赞QQ：".$this->uin[$i],"点赞结果".$re);
        }
        return $res;

    }
    //发表说说方法
    public function send_Msg($str){
        $post="syn_tweet_verson=1&paramstr=1&pic_template=&richtype=&richval=&special_url=&subrichtype=&who=1&con=".$str."&feedversion=1&ver=1&ugc_right=1&to_sign=0&hostuin=".$this->qq."&code_version=1&format=fs&qzreferrer=https%3A%2F%2Fuser.qzone.qq.com%2F".$this->qq."%2Finfocenter%3Fvia%3Dtoolbar";
        $qzonetoken=$this->get_qzonetoken();
        $send_url="https://user.qzone.qq.com/proxy/domain/taotao.qzone.qq.com/cgi-bin/emotion_cgi_publish_v6?qzonetoken=".$qzonetoken."&g_tk=".$this->g_tk;
        $re=$this->curl_request($send_url,$post,$this->cookie,0);
        return $re;
    }
    //发表评论
    private function send_discuss($msg,$uina,$tid){
        $post="topicId=".$uina."_".$tid."__1&feedsType=100&inCharset=utf-8&outCharset=utf-8&plat=qzone&source=ic&hostUin=".$uina."&isSignIn=&platformid=52&uin=".$this->qq."&format=fs&ref=feeds&content=".$msg."&richval=&richtype=&private=0&paramstr=1&qzreferrer=https%3A%2F%2Fuser.qzone.qq.com%2F".$this->qq."%2Finfocenter%3Fvia%3Dtoolbar";
        $ur="https://user.qzone.qq.com/proxy/domain/taotao.qzone.qq.com/cgi-bin/emotion_cgi_re_feeds?qzonetoken=".$this->qzone_token."&g_tk=".$this->g_tk;
        $this->curl_request($ur,$post,$this->cookie,0);
    }
    //获取Qzone_Token
    private function get_qzonetoken(){
        $str=$this->curl_request($this->url,"",$this->cookie,0);
        preg_match("/try\{return \".{82}/",$str,$reg);
        return substr($reg[0],12,80);
    }
    //获取，未点赞的列表curkey，unikey，uin，tid
    private function get_no_dianzan()
    {

        //请求
        $str=$this->curl_request($this->url,"",$this->cookie,0);
        //获取token
        preg_match("/try\{return \".{82}/",$str,$reg);
        $this->qzone_token=substr($reg[0],12,80);
        preg_match_all("/<a class=\"item qz_like_btn_v3 \" data-islike=\"0\" (.*?)<\/a>/",$str,$regsq);
        unset($this->unikey);
        unset($this->curkey);
        unset($this->uin);
        unset($this->tid);
        $ar= $this->get_Array($regsq);
        foreach($ar as $value)
        {
            preg_match("/data-unikey=\"(.*?)\"/",$value,$regs);
            $this->unikey[]=$regs[1];
            preg_match("/data-curkey=\"(.*?)\"/",$value,$reg);
            $this->curkey[]=$reg[1];
            preg_match("/http:\/\/user.qzone.qq.com\/[0-9]*?\//",$reg[1],$req);
            $this->uin[]=substr($req[0],25,-1);
            $days_array=explode('/',$reg[1]);
            $this->tid[]=$days_array[count($days_array)-1];

        }

    }
    //计算_GTK值
    private function get_GTK($cookie){
        preg_match("/p_skey=([^;^\']*)/",$cookie,$re);
        $skey=$re[1];
        $hash = 5381;
        for($i=0;$i<strlen($skey);++$i){
            $hash += ($hash << 5) + $this->utf8_unicode($skey[$i]);
        }
        return $hash & 0x7fffffff;
    }
    //编码转化
    private function utf8_unicode($c) {
        switch(strlen($c)) {
            case 1:
                return ord($c);
            case 2:
                $n = (ord($c[0]) & 0x3f) << 6;
                $n += ord($c[1]) & 0x3f;
                return $n;
            case 3:
                $n = (ord($c[0]) & 0x1f) << 12;
                $n += (ord($c[1]) & 0x3f) << 6;
                $n += ord($c[2]) & 0x3f;
                return $n;
            case 4:
                $n = (ord($c[0]) & 0x0f) << 18;
                $n += (ord($c[1]) & 0x3f) << 12;
                $n += (ord($c[2]) & 0x3f) << 6;
                $n += ord($c[3]) & 0x3f;
                return $n;
        }
    }
    //多维数组转一维
    private function set_Array($arr)
    {

        for($i=0; $i<count($arr); $i++)
        {
            if(is_array($arr[$i]))
            {
                $this->set_Array($arr[$i]);
            }else{
                $this->temp[]=$arr[$i];
            }
        }
    }
    private function get_Array($arr)
    {
        unset($this->temp);
        $this->set_Array($arr);
        return $this->temp;

    }
}