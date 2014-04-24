package com.lait.jmeter.cdn;

import static org.junit.Assert.*;

import org.junit.Test;

public class CDNSimulationSamplerTest {
	@Test
	public void testLoadPage() throws Exception {
		
		CDNSimulationSampler sampler = new CDNSimulationSampler();
		CDN.getInstance().useHttpCacheControl(false);
		
		//
		assertNull(CDN.getInstance().get("http://www.baidu.com/"));
		
		//
		sampler.loadPagesManually("http://www.baidu.com/\nhttp://www.360.cn/");
		assertNotNull(CDN.getInstance().get("http://www.baidu.com/"));
		assertNotNull(CDN.getInstance().get("http://www.360.cn/"));
		
		//
		sampler.loadPagesManually("http://www.tencent.com/zh-cn/index.shtml ");
		assertNotNull(CDN.getInstance().get("http://www.tencent.com/zh-cn/index.shtml"));
		
	}

}
