package com.lait.jmeter.cdn;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.Interruptible;

public class CDNSimulationSampler extends HTTPSamplerBase implements Interruptible  {
    private static final long serialVersionUID = 241L;

    private final transient CDNSimulationJavaImpl cdnSimulator;
    
    public CDNSimulationSampler(){
        cdnSimulator = new CDNSimulationJavaImpl(this);
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
		this.cdnSimulator.load(text);
	}
}
