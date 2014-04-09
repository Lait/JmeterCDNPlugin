package com.lait.jmeter.cdn;

import java.io.IOException;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.sampler.HTTPJavaImpl;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.PostWriter;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class CDNSimulationJavaImpl extends HTTPJavaImpl {
	
    private static final boolean OBEY_CONTENT_LENGTH = false; // $NON-NLS-1$

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final int MAX_CONN_RETRIES = 10;

    static {
        log.info("Maximum connection retries = " + MAX_CONN_RETRIES); // $NON-NLS-1$
        // Temporary copies, so can set the final ones
    }

    private static final byte[] NULL_BA = new byte[0];// can share these

    /** Handles writing of a post or put request */
    private transient PostWriter postOrPutWriter;

    private volatile HttpURLConnection savedConn;
    
    private CDN cdn;
    
    protected CDNSimulationJavaImpl(HTTPSamplerBase base) {
        super(base);
        this.cdn = CDN.getInstance();
    }
    
    /**********************************************************************
     * 由于在超类中不可见而不可继承，故直接拷贝一份过来使用                                *
     * 来自org.apache.jmeter.protocol.http.sampler.HTTPJavaImpl           *
     **********************************************************************/
    
    /**
     * Send POST data from <code>Entry</code> to the open connection.
     * This also handles sending data for PUT requests
     *
     * @param connection
     *            <code>URLConnection</code> where POST data should be sent
     * @return a String show what was posted. Will not contain actual file upload content
     * @exception IOException
     *                if an I/O exception occurs
     */
    protected String sendPostData(URLConnection connection) throws IOException {
        return postOrPutWriter.sendPostData(connection, testElement);
    }

    protected String sendPutData(URLConnection connection) throws IOException {
        return postOrPutWriter.sendPostData(connection, testElement);
    }
    
    /**
     * From the <code>HttpURLConnection</code>, store all the "set-cookie"
     * key-pair values in the cookieManager of the <code>UrlConfig</code>.
     *
     * @param conn
     *            <code>HttpUrlConnection</code> which represents the URL
     *            request
     * @param u
     *            <code>URL</code> of the URL request
     * @param cookieManager
     *            the <code>CookieManager</code> containing all the cookies
     *            for this <code>UrlConfig</code>
     */
    protected void saveConnectionCookies(HttpURLConnection conn, URL u, CookieManager cookieManager) {
        if (cookieManager != null) {
            for (int i = 1; conn.getHeaderFieldKey(i) != null; i++) {
                if (conn.getHeaderFieldKey(i).equalsIgnoreCase(HTTPConstants.HEADER_SET_COOKIE)) {
                    cookieManager.addCookieFromHeader(conn.getHeaderField(i), u);
                }
            }
        }
    }
    
    /**********************************************************************
     * 结束                                                                                                                  *
     **********************************************************************/
    
    //解析urls字符串，加载包含的页面到cdn内存
    public void load(String urls) {
    	String[] pUrls = urls.split("\n");
    	if (pUrls[0].length() <= 0) return;
		for (String pUrl : pUrls) {
			pUrl = pUrl.trim();
			if (pUrl.contains(" ")) {
				System.out.println(pUrl + " is not a valid url");
				return;
			}
			
			this.loadPage(pUrl);
		}
    }
    
    protected void loadPage(String pUrl) {
    	System.out.println("Loading page:" + pUrl);
    	URL url = null;
		try {
			url = new URL(pUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
    	this.sample(url, "GET", false, 0);
    }
    
    //从cdn中取出数据填充到res
    protected HTTPSampleResult setResFromCDN(String requestUrl, URL url, String method, HTTPSampleResult res) {
    	System.out.println("Record of key:" + requestUrl + " is cached in cdn. So use it");
        /*Debug
        System.out.println("*" + temp.getResponseCode() + "*");
        System.out.println("*" + temp.getResponseMessage() + "*");
        System.out.println("*" + temp.getContentType() + "*");
        System.out.println("*" + temp.getDataEncodingWithDefault() + "*");
        if (temp.isRedirect()) System.out.println("*" + temp.getRedirectLocation() + "*");
        System.out.println("*" + temp.getHeadersSize() + "*");
        System.out.println("*" + temp.getURL() + "*");
        */
    	/*
    	HTTPSampleResult temp = cdn.get(requestUrl);
    	res.sampleEnd();
    	res.setResponseCode(temp.getResponseCode());
    	res.setSuccessful(true);
    	res.setResponseMessage(temp.getResponseMessage());
    	res.setContentType(temp.getContentType());
        res.setEncodingAndType(temp.getDataEncodingWithDefault());
        if (res.isRedirect()) {
        	res.setRedirectLocation(temp.getRedirectLocation());
        }
        res.setHeadersSize(temp.getHeadersSize());
        res.setURL(temp.getURL());
        */
        res.sampleEnd();
        res.setResponseNoContent();
        res.setSuccessful(true);
        return res;
    }
    
    //从源服务器中取得数据填充到res中返回
    protected HTTPSampleResult setResFromSource(String requestUrl, URL url, String method, HTTPSampleResult res) {
    	System.out.println("Record of key:" + requestUrl + " is not cached in cdn.");
        // Sampling proper - establish the connection and read the response:
        // Repeatedly try to connect:
        int retry;
        byte[] responseData;
        
        HttpURLConnection  conn         = null;
        final CacheManager cacheManager = getCacheManager();
        
        try {
	        // Start with 0 so tries at least once, and retries at most MAX_CONN_RETRIES times
	        for (retry = 0; retry <= MAX_CONN_RETRIES; retry++) {
	            try {
	                conn = setupConnection(url, method, res);
	                // Attempt the connection:
	                savedConn = conn;
	                conn.connect();
	                break;
	            } catch (BindException e) {
	                if (retry >= MAX_CONN_RETRIES) {
	                    log.error("Can't connect after "+retry+" retries, "+e);
	                    throw e;
	                }
	                log.debug("Bind exception, try again");
	                if (conn!=null) {
	                    savedConn = null; // we don't want interrupt to try disconnection again
	                    conn.disconnect();
	                }
	                setUseKeepAlive(false);
	                continue; // try again
	            } catch (IOException e) {
	                log.debug("Connection failed, giving up");
	                throw e;
	            }
	        }
	        if (retry > MAX_CONN_RETRIES) {
	            // This should never happen, but...
	            throw new BindException();
	        }
	        if (method.equals(HTTPConstants.POST)) {
	            String postBody = sendPostData(conn);
	            res.setQueryString(postBody);
	        }
	        else if (method.equals(HTTPConstants.PUT)) {
	            String putBody = sendPutData(conn);
	            res.setQueryString(putBody);
	        }
	        responseData = readResponse(conn, res);
	        res.sampleEnd();
	        
	        //成功从源服务器获取数据，现在开始填充数据到res中
	        res.setResponseData(responseData);
	
	        @SuppressWarnings("null") // Cannot be null here
	        int errorLevel = conn.getResponseCode();
	        String respMsg = conn.getResponseMessage();
	        String hdr=conn.getHeaderField(0);
	        if (hdr == null) {
	            hdr="(null)";  // $NON-NLS-1$
	        }
	        if (errorLevel == -1){// Bug 38902 - sometimes -1 seems to be returned unnecessarily
	            if (respMsg != null) {// Bug 41902 - NPE
	                try {
	                    errorLevel = Integer.parseInt(respMsg.substring(0, 3));
	                    log.warn("ResponseCode==-1; parsed "+respMsg+ " as "+errorLevel);
	                  } catch (NumberFormatException e) {
	                    log.warn("ResponseCode==-1; could not parse "+respMsg+" hdr: "+hdr);
	                  }
	            } else {
	                respMsg=hdr; // for result
	                log.warn("ResponseCode==-1 & null ResponseMessage. Header(0)= "+hdr);
	            }
	        }
	        if (errorLevel == -1) {
	            res.setResponseCode("(null)"); // $NON-NLS-1$
	        } else {
	            res.setResponseCode(Integer.toString(errorLevel));
	        }
	        res.setSuccessful(isSuccessCode(errorLevel));
	
	        if (respMsg == null) {// has been seen in a redirect
	            respMsg=hdr; // use header (if possible) if no message found
	        }
	        res.setResponseMessage(respMsg);
	
	        String ct = conn.getContentType();
	        if (ct != null){
	            res.setContentType(ct);// e.g. text/html; charset=ISO-8859-1
	            res.setEncodingAndType(ct);
	        }
	
	        String responseHeaders = getResponseHeaders(conn);
	        res.setResponseHeaders(responseHeaders);
	
	        if (res.isRedirect()) {
	            res.setRedirectLocation(conn.getHeaderField(HTTPConstants.HEADER_LOCATION));
	        }
	        
	        // record headers size to allow HTTPSampleResult.getBytes() with different options
	        res.setHeadersSize(responseHeaders.replaceAll("\n", "\r\n") // $NON-NLS-1$ $NON-NLS-2$
	                .length() + 2); // add 2 for a '\r\n' at end of headers (before data) 
	        if (log.isDebugEnabled()) {
	            log.debug("Response headersSize=" + res.getHeadersSize() + " bodySize=" + res.getBodySize()
	                    + " Total=" + (res.getHeadersSize() + res.getBodySize()));
	        }
	        
	        // If we redirected automatically, the URL may have changed
	        if (getAutoRedirects()){
	            res.setURL(conn.getURL());
	        }
	
	        // Store any cookies received in the cookie manager:
	        saveConnectionCookies(conn, url, getCookieManager());
	
	        System.out.println("Update record of key:" + requestUrl + " in cdn");
	        this.cdn.set(conn, new HTTPSampleResult(res));
	        
	        // Save cache information
	        if (cacheManager != null){
	            cacheManager.saveDetails(conn, res);
	        }
	        return res;
        } catch (IOException e) {
            res.sampleEnd();
            savedConn = null; // we don't want interrupt to try disconnection again
            // We don't want to continue using this connection, even if KeepAlive is set
            if (conn != null) { // May not exist
                conn.disconnect();
            }
            conn=null; // Don't process again
            return errorResult(e, res);
        } finally {
            // calling disconnect doesn't close the connection immediately,
            // but indicates we're through with it. The JVM should close
            // it when necessary.
            savedConn = null; // we don't want interrupt to try disconnection again
            disconnect(conn); // Disconnect unless using KeepAlive
        }
    }
    
    @Override
    protected HTTPSampleResult sample(URL url, String method, boolean areFollowingRedirect, int frameDepth) {
    	/*
    	System.out.println("-" + url + "-");
    	System.out.println("-" + method + "-");
    	System.out.println("-" + areFollowingRedirect + "-");
    	System.out.println("-" + frameDepth + "-");
    	*/
    	
        String urlStr = url.toString();
        log.debug("Start : sample " + urlStr);

        HTTPSampleResult res = new HTTPSampleResult();
        res.setMonitor(isMonitor());

        res.setSampleLabel(urlStr);
        res.setURL(url);
        res.setHTTPMethod(method);

        res.sampleStart(); // Count the retries as well in the time

        // Check cache for an entry with an Expires header in the future
        final CacheManager cacheManager = getCacheManager();
        if (cacheManager != null && HTTPConstants.GET.equalsIgnoreCase(method)) {
           if (cacheManager.inCache(url)) {
               res.sampleEnd();
               res.setResponseNoContent();
               res.setSuccessful(true);
               return res;
           }
        }

        String fullURL = "http://" + url.getHost();
        if (url.getPort() > 0 && url.getPort() != url.getDefaultPort()) {
        	fullURL = fullURL + ":" + url.getPort();
        }
        fullURL = fullURL + url.getPath();
        
        if (cdn.isCached(fullURL)) {
        	res = setResFromCDN(fullURL, url, method, res);
        } else {
        	res = setResFromSource(fullURL, url, method, res);
        } 
        res = resultProcessing(areFollowingRedirect, frameDepth, res);
        log.debug("End : sample");
        return res;
    }
}
