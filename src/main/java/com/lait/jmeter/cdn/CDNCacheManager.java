package com.lait.jmeter.cdn;

import java.net.HttpURLConnection;
import java.util.Date;

import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;

public class CDNCacheManager {
	CacheImpl<String, CacheEntry> cache;

	private static final long ONE_YEAR_MS = 365*24*60*60*1000L;
	
	private static final CDNCacheManager instance = new CDNCacheManager();
	
	public static final CDNCacheManager getInstance() {
		return instance;
	}
	
	private CDNCacheManager() {
		this.cache = new CacheImpl<String, CacheEntry>("CDN-Cache in memory", 2048);
	}
	
	public String get(String url) {
		CacheEntry entry = this.cache.get(url);
		if (entry == null) return null;
		
		Date curr = new Date();
		if (curr.compareTo(entry.getExpires()) > 0) {
			this.cache.remove(url);
			return null;
		} else {
			return entry.getResponse();
		}
	}
	
	public void set(String response, String lastModified, String cacheControl, 
			String expires, String etag, String url, String date) 
	{
        Date expiresDate = null; // i.e. not using Expires
        final String MAX_AGE = "max-age=";
        
        if(cacheControl != null && cacheControl.contains("no-store")) {
            // We must not store an CacheEntry, otherwise a 
            // conditional request may be made
            return;
        }
        if (expires != null) {
            try {
                expiresDate = DateUtil.parseDate(expires);
            } catch (DateParseException e) {
                expiresDate = new Date(0L);; // invalid dates must be treated as expired
            }
        }
        // if no-cache is present, ensure that expiresDate remains null, which forces revalidation
        if(cacheControl != null && !cacheControl.contains("no-cache")) {    
            // the max-age directive overrides the Expires header,
            if(cacheControl.contains(MAX_AGE)) {
                long maxAgeInSecs = Long.parseLong(
                        cacheControl.substring(cacheControl.indexOf(MAX_AGE)+MAX_AGE.length())
                            .split("[, ]")[0] // Bug 51932 - allow for optional trailing attributes
                        );
                expiresDate=new Date(System.currentTimeMillis()+maxAgeInSecs*1000);

            } else if(expires==null) { // No max-age && No expires
                if(!StringUtils.isEmpty(lastModified) && !StringUtils.isEmpty(date)) {
                    try {
                        Date responseDate = DateUtil.parseDate( date );
                        Date lastModifiedAsDate = DateUtil.parseDate( lastModified );
                        // see https://developer.mozilla.org/en/HTTP_Caching_FAQ
                        // see http://www.ietf.org/rfc/rfc2616.txt#13.2.4 
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
            }  
            // else expiresDate computed in (expires!=null) condition is used
        }
		this.cache.put(url, new CacheEntry(response, lastModified, expiresDate, etag));
	}

	public boolean isCached(String url) {
		this.cache.get(url);
		return this.cache.isPresent(url);
	}

	public void saveDetails(HttpURLConnection conn, HTTPSampleResult res) {
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

	private boolean isCacheable(HTTPSampleResult res) {
        final String responseCode = res.getResponseCode();
        return "200".compareTo(responseCode) <= 0  // $NON-NLS-1$
            && "299".compareTo(responseCode) >= 0; // $NON-NLS-1$
	}
}
