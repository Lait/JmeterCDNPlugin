package com.lait.jmeter.cdn;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
	}
	
	public HTTPSampleResult get(String url) {
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
	
	public void set(HTTPSampleResult response, String lastModified, String cacheControl, 
			String expires, String etag, String url, String date) 
	{
        Date expiresDate = null;
        boolean noCache = false;
        final String MAX_AGE = "max-age=";
        
        /*************************HACKS************************/
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, +1);
	    SimpleDateFormat dateFormat = new SimpleDateFormat(
	            "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		expires = dateFormat.format(cal.getTime());
		cacheControl = null;
		/*******************************************************/
        if (cacheControl == null) {
        	System.out.println("No cache-control offered, use default settings.");
        } else {  
	        //如果cacheControl不存在或包含no-store则不进行缓存
        	if (cacheControl.contains("no-store") || cacheControl.contains("private")) {
	        	System.out.println("This record of key:" + url + " is not cacheable!");
	            return;
	        }
	        
	        if (cacheControl != null && cacheControl.contains("no-cache")) {
	        	noCache = true;
	        }
	        
	        if(cacheControl != null && !cacheControl.contains("no-cache")) {    
	            if(cacheControl.contains(MAX_AGE)) {
	            	long maxAgeInSecs = Long.parseLong(
	                        cacheControl.substring(cacheControl.indexOf(MAX_AGE)+MAX_AGE.length())
	                            .split("[, ]")[0] // Bug 51932 - allow for optional trailing attributes
	                        );
	                expiresDate=new Date(System.currentTimeMillis()+maxAgeInSecs*1000);
	                
	            } else if(expires==null) {
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
	                	//如果格式不合法，则设置为初始时间January 1, 1970, 00:00:00 GMT.
	                    expiresDate = new Date(0L); 
	                }
	            } 
	        }
        }
        System.out.println("Record of key:" + url + " is added to cdn.");
		this.cache.put(url, new CacheEntry(response, lastModified, expiresDate, etag, noCache, cacheControl));
	}

	public boolean isCached(String url) {
		this.cache.get(url);
		return this.cache.isPresent(url);
	}

	public void set(HttpURLConnection conn, HTTPSampleResult res) {
        if (isCacheable(res)){
        	System.out.println("This response is cacheable.");
            String lastModified = conn.getHeaderField(HTTPConstants.LAST_MODIFIED);
            String expires      = conn.getHeaderField(HTTPConstants.EXPIRES);
            String etag         = conn.getHeaderField(HTTPConstants.ETAG);
            String url          = conn.getURL().toString();
            String cacheControl = conn.getHeaderField(HTTPConstants.CACHE_CONTROL);
            String date         = conn.getHeaderField(HTTPConstants.DATE);
           
            set(res, lastModified, cacheControl, expires, etag, url, date);
        } else {
        	System.out.println("This response is not cacheable.");
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
