package com.lait.jmeter.cdn;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestPageLoaderTest {

	@Test
	public void test() {
		CDN cdn = CDN.getInstance();
		assertNotNull(cdn.get("http://192.168.56.102:9000/"));
		assertNotNull(cdn.get("http://192.168.56.102:9000/login"));
		assertNotNull(cdn.get("http://192.168.56.102:9000/signup"));
		//cdn.printAll();
	}

}
