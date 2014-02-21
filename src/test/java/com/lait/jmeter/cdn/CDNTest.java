package com.lait.jmeter.cdn;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CDNTest {

	@Test
	public void testLoadPage() {
		CDN cdn = CDN.getInstance();
		assertNotNull(cdn.get("http://www.baidu.com/"));
		cdn.printAll();
	}

}
