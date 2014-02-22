package com.lait.jmeter.cdn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;

public class TestPageLoader {
	private CDN cdn;
	
	private String   _host;
	private int      _port;
	private String[] _urls;
	
	public TestPageLoader(CDN cdn) {
		this.cdn = cdn;
	}
	
	public void setHost(String host, int port, String[] urls) {
		this._host = host;
		this._port = port;
		this._urls = urls;
	}
	
	public void load() {
		for (String url : _urls) {
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
		client.getHostConfiguration().setHost(this._host, this._port, "http");
		HttpMethod method = new GetMethod(url);
		
		int retcode = client.executeMethod(method);
		if (retcode > 200 && retcode < 299) return;
		
		HTTPSampleResult res = new HTTPSampleResult();
		
		String fullUrl = "http://" + _host + url;
		
        String lastModified = getHeaderString(method.getResponseHeader("Last-Modified"));
        String expires      = getHeaderString(method.getResponseHeader("Expires"));
        String etag         = getHeaderString(method.getResponseHeader("Etag"));
        String cacheControl = getHeaderString(method.getResponseHeader("Cache-Control"));
        String date         = getHeaderString(method.getResponseHeader("Date"));
		

		
		this.cdn.set(res, lastModified, cacheControl, expires, etag, fullUrl, date);
	}
}
