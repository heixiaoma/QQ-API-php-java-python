package 空间;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
	private String qq;
	private String cookie;
	private String g_tk;
	private String hostUin;
	private String unikey;
	private String curkey;
	private String qzoneToken;
	
	public Util(String qq, String cookie) {
		this.qq = qq;
		this.cookie = cookie;
		this.g_tk = GetG_TK();
		this.hostUin = "";
		this.unikey = "";
		this.curkey = "";
		//更新qzonetoken
		updateQzoneToken();
	}
	
	/**
	 * 点赞
	 */
	public void func() {
		func("");
	}
	
	/**
	 * 点赞+评论
	 * @param plTxt 评论内容
	 */
	public void func(String plTxt) {
		//获取未点赞列表  匹配标签属性为data-islike="0"的就是没有点过赞的
		List<String> regExpResult = getRegExpResult(getQzone("https://user.qzone.qq.com/" + qq, ""), "data-islike=\"0\"\\sdata-likecnt=.+?\\sdata-showcount=.+?\\sdata-unikey=\".+?\"\\sdata-curkey=\".+?\"\\sdata-clicklog=\"like\".+?</a>");
		//由于正则匹配出点赞按钮里面有一个i标签（展示点赞的图片）与匹配的data-islike属性一样，所以每个说说就获取了两次，所以下面+=2
		for (int i = 0; i < regExpResult.size(); i+=2) {
			int a = regExpResult.get(i).indexOf("data-unikey");
			int b = regExpResult.get(i).indexOf("\" data-curkey");
			this.unikey = regExpResult.get(i).substring(a+13,b);
			int c = regExpResult.get(i).indexOf(" data-clicklog");
			this.curkey = regExpResult.get(i).substring(b+15,c-1);
			
			//获取当前时间戳（虽然可以写死，这并不是必须动态获取的）
			String abstime = Long.toString(new Date().getTime()).substring(0,10);
			String param="qzreferrer=https%3A%2F%2Fuser.qzone.qq.com%2F"+qq+"&opuin="+qq+"&unikey="+unikey+"&curkey="+curkey+"&from=1&appid=311&typeid=0&abstime="+abstime+"&fid=2430955f96190d5a984e0b00&active=0&fupdate=1";
			String html= getQzone("https://user.qzone.qq.com/proxy/domain/w.qzone.qq.com/cgi-bin/likes/internal_dolike_app?g_tk="+g_tk, param);

			String[] unikeys = unikey.split("/");
			hostUin = unikeys[3];
			//评论
			if (html.indexOf("succ") != -1 && !plTxt.equals("")) {
				pinglun(unikeys[unikeys.length-1], plTxt);
				System.out.println("QQ:" + hostUin + "已点赞和评论");
			}else if (html.indexOf("succ") != -1) {
				System.out.println("QQ:" + hostUin + "已点赞");
			}else {
				System.out.println("QQ:" + hostUin + "点赞或评论失败");
			}
		}
	}
	
	// 
	private int tempI = -1;
	/**
	 * 评论
	 * @param tid 说说的tid
	 * @param plTxt 评论内容
	 */
	private void pinglun(String tid, String plTxt) {
		tempI++;
		if (tempI % 2 != 0) {
			return;
		}
		String param;
		try {
			param = "topicId="+qq+"_"+tid+"__1&feedsType=100&inCharset=utf-8&outCharset=utf-8&plat=qzone&source=ic&hostUin="+hostUin+"&isSignIn=&platformid=52&uin="+qq+"&format=fs&ref=feeds&content="+URLEncoder.encode(plTxt, "UTF-8")+"&richval=&richtype=&private=0&paramstr=1&qzreferrer=https%3A%2F%2Fuser.qzone.qq.com%2F"+qq;
			String qzone = getQzone("https://user.qzone.qq.com/proxy/domain/taotao.qzone.qq.com/cgi-bin/emotion_cgi_re_feeds?qzonetoken="+qzoneToken+"&g_tk="+g_tk, param);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发说说
	 * @param txt 说说内容
	 */
	public void sendTalk(String txt) {
		updateQzoneToken();
		String param;
		try {
			param = "syn_tweet_verson=1&paramstr=1&pic_template=&richtype=&richval=&special_url=&subrichtype=&who=1&con="+URLEncoder.encode(txt, "UTF-8")+"&feedversion=1&ver=1&ugc_right=1&to_sign=1&hostuin="+qq+"&code_version=1&format=fs&qzreferrer=https%3A%2F%2Fuser.qzone.qq.com%2F"+qq;
			getQzone("https://user.qzone.qq.com/proxy/domain/taotao.qzone.qq.com/cgi-bin/emotion_cgi_publish_v6?qzonetoken="+qzoneToken+"&g_tk="+g_tk, param);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 更新qzonetoken
	 */
	private void updateQzoneToken() {
		qzoneToken = getRegExpResult(getQzone("https://user.qzone.qq.com/"+qq, ""), "try\\{return \".{82}").get(0);
		int a = qzoneToken.indexOf("\"");
		int b = qzoneToken.lastIndexOf("\"");
		qzoneToken = qzoneToken.substring(a+1, b);
	}
	
	/**
	 * 获取g_tk
	 * @return 返回g_tk
	 */
	private String GetG_TK() {
		//取得p_skey的值
		List<String> ls = getRegExpResult(cookie, "p_skey=.{44}");
		String str = ls.get(ls.size() - 1).substring(7);
		
		int hash = 5381;
		for (int i = 0, len = str.length(); i < len; ++i) {
			hash += (hash << 5) + (int) (char) str.charAt(i);
		}
		return (hash & 0x7fffffff) + "";
	}
	
	/**
	 * 返回匹配正则表达式的字符串
	 * @param str 需要匹配的内容
	 * @param reg 正则表达式
	 * @return 返回一个符合正则的List集合
	 */
	private List<String> getRegExpResult(String str, String reg) {
		List<String> ls = new ArrayList<String>();
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(str);
		while (matcher.find())
			ls.add(matcher.group());
		return ls;
	}
	
	/**
	 * 提交参数，得到返回网页数据
	 * @param path 需要提交数据的网址
	 * @param post 参数列表
	 * @return 返回相对应的网页
	 */
	private String getQzone(String path, String post) {
		URL url = null;
		try {
			url = new URL(path);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("POST");// 提交模式
			httpURLConnection.setRequestProperty("accept", "*/*");
			httpURLConnection.setRequestProperty("connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
			httpURLConnection.setRequestProperty("Cookie", this.cookie);
			// conn.setConnectTimeout(10000);//连接超时 单位毫秒
			// conn.setReadTimeout(2000);//读取超时 单位毫秒
			// 发送POST请求必须设置如下两行
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
			// 发送请求参数
			printWriter.write(post);// post的参数 xx=xx&yy=yy
			// flush输出流的缓冲
			printWriter.flush();
			// 开始获取数据
			BufferedInputStream bis = new BufferedInputStream(httpURLConnection.getInputStream());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int len;
			byte[] arr = new byte[1024];
			while ((len = bis.read(arr)) != -1) {
				bos.write(arr, 0, len);
				bos.flush();
			}
			bos.close();
			return bos.toString("utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
