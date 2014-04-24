package com.lait.jmeter.cdn;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.Interruptible;

public class CDNSimulationSampler extends HTTPSamplerBase implements Interruptible  {
    private static final long serialVersionUID = 241L;
    
    public static boolean USE_CACHE_CONTROL = true;
    public static String PAGES_LIST = "";
    
    private final transient CDNSimulationJavaImpl cdnSimulator;
    
    public CDNSimulationSampler(){
        cdnSimulator = new CDNSimulationJavaImpl(this);
        CDN.getInstance().useHttpCacheControl(USE_CACHE_CONTROL);
    }

    @Override
    public boolean interrupt() {
        return cdnSimulator.interrupt();
    }

    @Override
    protected HTTPSampleResult sample(java.net.URL u, String method,
            boolean areFollowingRedirect, int depth) {
        return cdnSimulator.sample(u, method, areFollowingRedirect, depth);
    }

	public void loadPagesManually(String text) {
		if (PAGES_LIST.compareTo(text) != 0) {
			PAGES_LIST = text;
			this.cdnSimulator.load(PAGES_LIST);
		}
	}
	
	public void useHttpCacheControl(boolean use) {
		if (!USE_CACHE_CONTROL == use) {
			System.out.println("Set Cache-Control:" + use);
			USE_CACHE_CONTROL = use;
			CDN.getInstance().useHttpCacheControl(USE_CACHE_CONTROL);
		}
	}
}
