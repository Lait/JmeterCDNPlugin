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
	
	//为了确保被所有线程共享，cdn必须在内存中只有一份，所以使用单例模式
	private static final CDN instance = new CDN();
	
	public static CDN getInstance() {
		return instance;
	}
	
	private CDN() {
		this.cache = new CDNCache<String, CacheEntry>("CDN-Cache in memory", 2048);
		this.server = new CDNPushServer(this);
		new TestPageLoader(this).load();
	}
	
	//这里采用懒惰更新策略，在取值得时候才对数据可用性进行验证，删除过期数据或直接回源
	public String get(String url) {
		CacheEntry entry = this.cache.get(url);
		
		//如果记录不存在或设置了no-cache则直接返回
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
        
        if( cacheControl != null && //如果包含no-store 或  private则不在cdn缓存
            (cacheControl.contains("no-store") || cacheControl.contains("private"))) 
        {
            return;
        }
                
        // 如果no-cache没有设置则进行后续处理，否则直接跳过，让expireDate保持null
        if(cacheControl != null && !cacheControl.contains("no-cache")) {    
        	noCache = true;
            
            if(cacheControl.contains(MAX_AGE)) {// max-age优先级最高，会覆盖expire的设置
            	//获取max age的值
                long maxAgeInSecs = Long.parseLong(
                		//使用", "对余下的字符串进行分割，所得的第一个片段即为数值
                        cacheControl.substring(cacheControl.indexOf(MAX_AGE)+MAX_AGE.length())
                            .split("[, ]")[0] // Bug 51932 - allow for optional trailing attributes
                        );
                expiresDate=new Date(System.currentTimeMillis()+maxAgeInSecs*1000);
                
            } else if(expires==null) { // max-age和expire都没有设置的情况
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
                	//如果expire不合法，则把时间设置为January 1, 1970, 00:00:00 GMT.
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

	//如果返回值在(200, 299)之间则不可缓存，反之则可以
	private boolean isCacheable(HTTPSampleResult res) {
        final String responseCode = res.getResponseCode();
        return "200".compareTo(responseCode) <= 0  // $NON-NLS-1$
            && "299".compareTo(responseCode) >= 0; // $NON-NLS-1$
	}
	
	public void printAll() {
		this.cache.printAll();
	}
}
