package com.lait.jmeter.cdn;

import java.util.Date;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;

public class CacheEntry {
	private HTTPSampleResult response;
	
    private String lastModified;
    private String etag;
    private Date   expires;
    private boolean noCache;
    
    //���ڵ��Ժ���չ
    private String cacheControl;
    
    public CacheEntry(HTTPSampleResult response, String lastModified, Date expires, String etag, boolean noCache, String cacheControl){
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
	public HTTPSampleResult getResponse() {
		return response;
	}
	public void setResponse(HTTPSampleResult response) {
		this.response = response;
	}
	public boolean isNoCache() {
		return this.noCache;
	}
	public String getCacheControl() {
		return this.cacheControl;
	}
}