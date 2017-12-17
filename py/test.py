#!/usr/bin/python3
#已经测试了，相对来说比较稳定，可以进行，点赞和评论，评论内容自己修改，------By 黑小马
# encoding=utf-8
import urllib.request, json, time, http.cookiejar,re
from http import client
from urllib import parse
unikeys=[]
curkeys=[]
uins=[]
tids=[]
def make_g_tk(cookie):
	'''
	通过Cookie生成g_tk
	'''
	ss=re.search('p_skey=([^;^\']*)',cookie).group(1)
	hash = 5381
	for i in ss:
		hash+=(hash<<5)+ord(i)
	return (hash & 0x7fffffff)

def get_msg():
	'''
	获取说说列表
	'''
	fs = open('testssss.html', 'r')
	lines = fs.read()
	reg=re.compile('<div class=\"f-info\">.*?</div>')
	match = reg.findall(lines)
	fs.close()
	fss = open("ts.html","wb")
	for colour in match:
		print (colour[20:-6]+"\n")
		fss.write( bytes(colour[20:-6], 'UTF-8'))
		fss.flush()
	fss.close()
def get_qzone_msg():
	'''
	请求网页输出到文本
	'''
	req = urllib.request.Request(url, headers={'Cookie': qzone_cookie, 'User-Agent': UA})
	html=urllib.request.urlopen(req)
	str=html.read()
	f = open("testssss.html","wb")
	f.write(str)
	f.close()
def get_dian():
	'''
	获取未点赞列表
	'''
	print("获取未点赞列表")
	fs = open('testssss.html', 'r')
	lines = fs.read()
	reg=re.compile('<a class=\"item qz_like_btn_v3 \" data-islike=\"0\" .*?</a>')
	match = reg.findall(lines)
	fs.close()
	for colour in match:
		if '/mood' in colour:
			unikey=re.findall("data-unikey=\".*?\"",colour)[0][13:-1]
			curkey=re.findall("data-curkey=\".*?\"",colour)[0][13:-1]
			tid=re.findall("mood/.{0,}",unikey)[0][5:]
			uin=re.findall("http://user.qzone.qq.com/.*?/mood",unikey)[0][25:-5]
			unikeys.append(unikey)
			curkeys.append(curkey)
			tids.append(tid)
			uins.append(uin)
		else:
			print("发现一个上传图片说说，不为他点赞")
		
		

def get_zan():
	'''
	开始点赞
	'''	
	for i in range(len(unikeys)):
		body={'qzreferrer':url,  
		  'opuin':str(qq),  
		  'unikey':unikeys[i],  
		  'curkey':curkeys[i],  
		  'from':1,  
		  'appid':311,  
		  'typeid':0,  
		  'active':0,  
		  'fupdate':1  
		}
		headers = {
		'Cookie': qzone_cookie,
		'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0'
		
		}
		urll='/proxy/domain/w.qzone.qq.com/cgi-bin/likes/internal_dolike_app?g_tk='+str(make_g_tk(qzone_cookie))
		httpClient = http.client.HTTPConnection("h5.qzone.qq.com")
		httpClient.request("POST", urll, urllib.parse.urlencode(body), headers)
		response = httpClient.getresponse()
		#print(response.read())
		if "succ" in str(response.read()):
			print("点赞成功")
			get_send_msg(uins[i],tids[i])
		else:
			print("点赞失败。。请联系作者")
		httpClient.close()

def get_send_msg(uin,tid):
	'''
	开始发表评论
	'''	
	body={'qzreferrer':url, 
	  'topicId':str(uin)+'_'+str(tid)+'__1',
	  'feedsType':'100',
	  'inCharset':'utf-8',
	  'outCharset':'utf-8',
	  'plat':'qzone',
	  'source':'ic',
	  'hostUin':str(uin),
	  'uin':str(qq),
	  'format':'fs',
	  'ref':'feeds',
	  'content':'好文章，为你点赞',
	  'private':'0',
	  'paramstr':'1'
	  
	}
	headers = {
	'Cookie': qzone_cookie,
	'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0'
	
	}
	urll='/proxy/domain/taotao.qzone.qq.com/cgi-bin/emotion_cgi_re_feeds?g_tk='+str(make_g_tk(qzone_cookie))
	httpClient = http.client.HTTPConnection("h5.qzone.qq.com")
	httpClient.request("POST", urll, urllib.parse.urlencode(body), headers)
	response = httpClient.getresponse()
	if "\"subcode\":0" in str(response.read()):
		print("评论成功")
	else:
		print("评论失败。。请联系作者")
	httpClient.close()
		
if __name__ == '__main__':
	print("欢迎使用QQ空间秒赞评论一键程序")
	qq_value = input('请输入QQ号码：')
	qq=qq_value
	url='https://user.qzone.qq.com/'+str(qq)+'/infocenter?via=toolbar'
	UA = 'Mozilla/5.0 (Windows NT 10.0; WOW64; rv:49.0) Gecko/20100101 Firefox/49.0'
	cookie_value = input('请输入QQ空间Cookie：')
	qzone_cookie = cookie_value
	ms=input('请多少分刷新一次：')
	while True:
		get_qzone_msg()
		get_msg()
		get_dian()
		get_zan()
		unikeys=[]
		curkeys=[]
		uins=[]
		tids=[]
		time.sleep(int(ms)*60)  

	
	
	
	
	
	
	
	
	
	
	