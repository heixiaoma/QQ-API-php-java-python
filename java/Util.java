package �ռ�;

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
		//����qzonetoken
		updateQzoneToken();
	}
	
	/**
	 * ����
	 */
	public void func() {
		func("");
	}
	
	/**
	 * ����+����
	 * @param plTxt ��������
	 */
	public void func(String plTxt) {
		//��ȡδ�����б�  ƥ���ǩ����Ϊdata-islike="0"�ľ���û�е���޵�
		List<String> regExpResult = getRegExpResult(getQzone("https://user.qzone.qq.com/" + qq, ""), "data-islike=\"0\"\\sdata-likecnt=.+?\\sdata-showcount=.+?\\sdata-unikey=\".+?\"\\sdata-curkey=\".+?\"\\sdata-clicklog=\"like\".+?</a>");
		//��������ƥ������ް�ť������һ��i��ǩ��չʾ���޵�ͼƬ����ƥ���data-islike����һ��������ÿ��˵˵�ͻ�ȡ�����Σ���������+=2
		for (int i = 0; i < regExpResult.size(); i+=2) {
			int a = regExpResult.get(i).indexOf("data-unikey");
			int b = regExpResult.get(i).indexOf("\" data-curkey");
			this.unikey = regExpResult.get(i).substring(a+13,b);
			int c = regExpResult.get(i).indexOf(" data-clicklog");
			this.curkey = regExpResult.get(i).substring(b+15,c-1);
			
			//��ȡ��ǰʱ�������Ȼ����д�����Ⲣ���Ǳ��붯̬��ȡ�ģ�
			String abstime = Long.toString(new Date().getTime()).substring(0,10);
			String param="qzreferrer=https%3A%2F%2Fuser.qzone.qq.com%2F"+qq+"&opuin="+qq+"&unikey="+unikey+"&curkey="+curkey+"&from=1&appid=311&typeid=0&abstime="+abstime+"&fid=2430955f96190d5a984e0b00&active=0&fupdate=1";
			String html= getQzone("https://user.qzone.qq.com/proxy/domain/w.qzone.qq.com/cgi-bin/likes/internal_dolike_app?g_tk="+g_tk, param);

			String[] unikeys = unikey.split("/");
			hostUin = unikeys[3];
			//����
			if (html.indexOf("succ") != -1 && !plTxt.equals("")) {
				pinglun(unikeys[unikeys.length-1], plTxt);
				System.out.println("QQ:" + hostUin + "�ѵ��޺�����");
			}else if (html.indexOf("succ") != -1) {
				System.out.println("QQ:" + hostUin + "�ѵ���");
			}else {
				System.out.println("QQ:" + hostUin + "���޻�����ʧ��");
			}
		}
	}
	
	// 
	private int tempI = -1;
	/**
	 * ����
	 * @param tid ˵˵��tid
	 * @param plTxt ��������
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
	 * ��˵˵
	 * @param txt ˵˵����
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
	 * ����qzonetoken
	 */
	private void updateQzoneToken() {
		qzoneToken = getRegExpResult(getQzone("https://user.qzone.qq.com/"+qq, ""), "try\\{return \".{82}").get(0);
		int a = qzoneToken.indexOf("\"");
		int b = qzoneToken.lastIndexOf("\"");
		qzoneToken = qzoneToken.substring(a+1, b);
	}
	
	/**
	 * ��ȡg_tk
	 * @return ����g_tk
	 */
	private String GetG_TK() {
		//ȡ��p_skey��ֵ
		List<String> ls = getRegExpResult(cookie, "p_skey=.{44}");
		String str = ls.get(ls.size() - 1).substring(7);
		
		int hash = 5381;
		for (int i = 0, len = str.length(); i < len; ++i) {
			hash += (hash << 5) + (int) (char) str.charAt(i);
		}
		return (hash & 0x7fffffff) + "";
	}
	
	/**
	 * ����ƥ��������ʽ���ַ���
	 * @param str ��Ҫƥ�������
	 * @param reg ������ʽ
	 * @return ����һ�����������List����
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
	 * �ύ�������õ�������ҳ����
	 * @param path ��Ҫ�ύ���ݵ���ַ
	 * @param post �����б�
	 * @return �������Ӧ����ҳ
	 */
	private String getQzone(String path, String post) {
		URL url = null;
		try {
			url = new URL(path);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("POST");// �ύģʽ
			httpURLConnection.setRequestProperty("accept", "*/*");
			httpURLConnection.setRequestProperty("connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
			httpURLConnection.setRequestProperty("Cookie", this.cookie);
			// conn.setConnectTimeout(10000);//���ӳ�ʱ ��λ����
			// conn.setReadTimeout(2000);//��ȡ��ʱ ��λ����
			// ����POST�������������������
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
			// ��ȡURLConnection�����Ӧ�������
			PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
			// �����������
			printWriter.write(post);// post�Ĳ��� xx=xx&yy=yy
			// flush������Ļ���
			printWriter.flush();
			// ��ʼ��ȡ����
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
