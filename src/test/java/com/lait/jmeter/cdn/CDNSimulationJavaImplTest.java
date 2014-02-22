package com.lait.jmeter.cdn;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CDNSimulationJavaImplTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		new CDNSimulationSampler();
		CDN cdn = CDN.getInstance();
		assertNotNull(cdn.get("http://www.baidu.com/"));
		assertNotNull(cdn.get("http://www.baidu.com/"));
	}

}
