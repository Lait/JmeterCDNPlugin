package com.lait.jmeter.cdn;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;

public class TestPageLoader {
	private CDN cdn;
	
	private static final String   address = "http://192.168.56.102:9000";
	private static final String[] urls    = {"/", "/login", "/signup"};
	
	public TestPageLoader(CDN cdn) {
		this.cdn = cdn;
	}
	
	public void load() {
		for (String url : urls) {
			try {
				loadPage(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getHeaderString(Header h) {
		if (h == null) return null;
		else return h.getValue();
	}

	private void loadPage(String url) throws HttpException, IOException {
		HttpClient client = new HttpClient();
		client.getHostConfiguration().setHost("192.168.56.102", 9000, "http");
		HttpMethod method = new GetMethod(url);
		
		int retcode = client.executeMethod(method);
		if (retcode > 200 && retcode < 299) return;
		
		String body = null;
		String rurl = address + url;
		
        String lastModified = getHeaderString(method.getResponseHeader("Last-Modified"));
        String expires      = getHeaderString(method.getResponseHeader("Expires"));
        String etag         = getHeaderString(method.getResponseHeader("Etag"));
        String cacheControl = getHeaderString(method.getResponseHeader("Cache-Control"));
        String date         = getHeaderString(method.getResponseHeader("Date"));
		try {
			body = method.getResponseBodyAsString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/**************测试用*****************/
		//先把所有数据都保存在内存中，把过期时间都设置为一天后，保证测试时
		//所有数据都从cdn取得
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, +1);
	    SimpleDateFormat dateFormat = new SimpleDateFormat(
	            "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		expires = dateFormat.format(cal.getTime());
		cacheControl = null;
		/*******************************************************/
		this.cdn.set(body, lastModified, cacheControl, expires, etag, rurl, date);
	}
}
