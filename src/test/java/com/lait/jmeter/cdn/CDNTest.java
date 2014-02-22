package com.lait.jmeter.cdn;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CDNTest  {

	@Test
	public void testLoadPage() {
		CDN cdn = CDN.getInstance();
		cdn.get("http://www.baidu.com/");
		assertNotNull("123");
		cdn.printAll();
	}

}
