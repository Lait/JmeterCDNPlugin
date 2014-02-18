package com.lait.jmeter.cdn;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.Interruptible;

public class CDNSimulationSampler extends HTTPSamplerBase implements Interruptible {
	
	private static final long serialVersionUID = -3506320168036622521L;
	
	private CDNSimulationJavaImpl cdn;
	
	public CDNSimulationSampler() {
		
		this.cdn = new CDNSimulationJavaImpl(this);
	}
	
	public boolean interrupt() {
		
		return this.cdn.interrupt();
	}
	
	@Override
	protected HTTPSampleResult sample(java.net.URL url, String method,
			boolean areFollowingRedirect, int depth) {
		
		return this.cdn.sample(url, method, areFollowingRedirect, depth);
	}

}
