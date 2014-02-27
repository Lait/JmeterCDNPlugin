package com.lait.jmeter.cdn;

import static org.junit.Assert.*;

import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.junit.Before;
import org.junit.Test;

import com.lait.jmeter.cdn.gui.CDNSimulationSamplerGui;

public class CDNSimulationSamplerTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testLoadPage() {
		CDNSimulationSampler sampler = new CDNSimulationSampler();
		sampler.loadPagesManually("http://www.baidu.com/\nhttp://www.sina.com.cn/");
		assertNotNull(CDN.getInstance().get("http://www.baidu.com/"));
		assertNotNull(CDN.getInstance().get("http://www.sina.com.cn/"));
		
		sampler.loadPagesManually("http://www.tencent.com/zh-cn/index.shtml ");
		assertNotNull(CDN.getInstance().get("http://www.tencent.com/zh-cn/index.shtml"));
	}

}
