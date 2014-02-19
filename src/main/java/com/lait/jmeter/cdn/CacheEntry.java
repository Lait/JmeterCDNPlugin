package com.lait.jmeter.cdn;

import java.util.Date;

public class CacheEntry {
	private String response;
	
    private String lastModified;
    private String etag;
    private Date   expires;
    private boolean noCache;
    
    //用于调试和扩展
    private String cacheControl;
    
    public CacheEntry(String response, String lastModified, Date expires, String etag, boolean noCache, String cacheControl){
       this.response     = response;
       this.lastModified = lastModified;
       this.etag         = etag;
       this.expires      = expires;
       this.noCache      = noCache;
       
       this.cacheControl = cacheControl;
   }
    public String getLastModified() {
        return lastModified;
    }
    public String getEtag() {
        return etag;
    }
    @Override
    public String toString(){
        return lastModified+" "+etag;
    }
    public Date getExpires() {
        return expires;
    }
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public boolean isNoCache() {
		return this.noCache;
	}
	public String getCacheControl() {
		return this.cacheControl;
	}
}