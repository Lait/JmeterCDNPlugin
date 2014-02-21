package com.lait.jmeter.cdn;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;

public class CDN {
	CDNCache<String, CacheEntry> cache;

	public static final long ONE_YEAR_MS = 365*24*60*60*1000L;

	private CDNPushServer server;
	
	//Ϊ��ȷ���������̹߳��?cdn�������ڴ���ֻ��һ�ݣ�����ʹ�õ���ģʽ
	private static final CDN instance = new CDN();
	private TestPageLoader loader;
	
	public static CDN getInstance() {
		return instance;
	}
	
	private CDN() {
		this.cache  = new CDNCache<String, CacheEntry>("CDN-Cache in memory", 2048);
		this.server = new CDNPushServer(this);
		this.loader = new TestPageLoader(this);
		
		//测试用
		String[] urls = {"/"};
		this.loader.setHost("www.baidu.com", 80, urls);
		this.loader.load();
	}
	
	public String get(String url) {
		CacheEntry entry = this.cache.get(url);
		
		if (entry == null || entry.isNoCache()) {
			System.out.println("Entry of key=" + url + "does not exist.");
			return null;
		}
		
		Date curr = new Date();

		if (entry.getExpires() != null && curr.compareTo(entry.getExpires()) > 0) {
			System.out.println("Expire date is  " + entry.getExpires());
			System.out.println("Current date is " + curr.toString());
			this.cache.remove(url);
			return null;
		} else {
			return entry.getResponse();
		}
	}
	
	public void set(String response, String lastModified, String cacheControl, 
			String expires, String etag, String url, String date) 
	{
        Date expiresDate = null;
        boolean noCache = false;
        final String MAX_AGE = "max-age=";
        
        if( cacheControl != null && //����no-store ��  private����cdn����
            (cacheControl.contains("no-store") || cacheControl.contains("private"))) 
        {
            return;
        }
                
        // ���no-cacheû����������к����?����ֱ�������expireDate����null
        if(cacheControl != null && !cacheControl.contains("no-cache")) {    
        	noCache = true;
            
            if(cacheControl.contains(MAX_AGE)) {// max-age���ȼ���ߣ��Ḳ��expire������
            	//��ȡmax age��ֵ
                long maxAgeInSecs = Long.parseLong(
                		//ʹ��", "�����µ��ַ���зָ��õĵ�һ��Ƭ�μ�Ϊ��ֵ
                        cacheControl.substring(cacheControl.indexOf(MAX_AGE)+MAX_AGE.length())
                            .split("[, ]")[0] // Bug 51932 - allow for optional trailing attributes
                        );
                expiresDate=new Date(System.currentTimeMillis()+maxAgeInSecs*1000);
                
            } else if(expires==null) { // max-age��expire��û�����õ����
                if(!StringUtils.isEmpty(lastModified) && !StringUtils.isEmpty(date)) {
                    try {
                        Date responseDate = DateUtil.parseDate( date );
                        Date lastModifiedAsDate = DateUtil.parseDate( lastModified );
                        expiresDate=new Date(System.currentTimeMillis()
                                +Math.round((responseDate.getTime()-lastModifiedAsDate.getTime())*0.1));
                    } catch(DateParseException e) {
                        // date or lastModified may be null or in bad format
                        expiresDate = new Date(System.currentTimeMillis()+ONE_YEAR_MS);                      
                    }
                } else {
                    // TODO Can't see anything in SPEC
                    expiresDate = new Date(System.currentTimeMillis()+ONE_YEAR_MS);                      
                }
            } else {
                try {
                    expiresDate = DateUtil.parseDate(expires);
                } catch (DateParseException e) {
                	//���expire���Ϸ������ʱ������ΪJanuary 1, 1970, 00:00:00 GMT.
                    expiresDate = new Date(0L); 
                }
            } 
        }
		this.cache.put(url, new CacheEntry(response, lastModified, expiresDate, etag, noCache, cacheControl));
	}

	public boolean isCached(String url) {
		this.cache.get(url);
		return this.cache.isPresent(url);
	}

	public void set(HttpURLConnection conn, HTTPSampleResult res) {
        if (isCacheable(res)){
            String lastModified = conn.getHeaderField(HTTPConstants.LAST_MODIFIED);
            String expires = conn.getHeaderField(HTTPConstants.EXPIRES);
            String etag = conn.getHeaderField(HTTPConstants.ETAG);
            String url = conn.getURL().toString();
            String cacheControl = conn.getHeaderField(HTTPConstants.CACHE_CONTROL);
            String date = conn.getHeaderField(HTTPConstants.DATE);
            set(res.getResponseDataAsString(), lastModified, cacheControl, expires, etag, url, date);
        }
	}

	//����ֵ��(200, 299)֮���򲻿ɻ��棬��֮�����
	private boolean isCacheable(HTTPSampleResult res) {
        final String responseCode = res.getResponseCode();
        return "200".compareTo(responseCode) <= 0  // $NON-NLS-1$
            && "299".compareTo(responseCode) >= 0; // $NON-NLS-1$
	}
	
	public void printAll() {
		this.cache.printAll();
	}
}
