package com.lait.jmeter.cdn;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.Interruptible;

public class CDNSimulationSampler extends HTTPSamplerBase implements Interruptible  {

    private static final long serialVersionUID = 241L;

    private final transient CDNSimulationJavaImpl cdn;
    
    public CDNSimulationSampler(){
        cdn = new CDNSimulationJavaImpl(this);
    }

    @Override
    public boolean interrupt() {
        return cdn.interrupt();
    }

    @Override
    protected HTTPSampleResult sample(java.net.URL u, String method,
            boolean areFollowingRedirect, int depth) {
        return cdn.sample(u, method, areFollowingRedirect, depth);
    }
}
