package com.lait.jmeter.cdn;

import java.util.Date;

public class CacheEntry {
	private String response;
	
    private String lastModified;
    private String etag;
    private Date   expires;
    
    public CacheEntry(String response, String lastModified, Date expires, String etag){
       this.response     = response;
       this.lastModified = lastModified;
       this.etag         = etag;
       this.expires      = expires;
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
}