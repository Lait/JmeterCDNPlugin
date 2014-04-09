package com.lait.jmeter.cdn.gui;

import java.awt.event.ItemListener;

import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.testelement.TestElement;


import com.lait.jmeter.cdn.CDNSimulationSampler;

public class CDNSimulationSamplerGui extends HttpTestSampleGui implements ItemListener  {

	private static final long serialVersionUID = -8607693871007092945L;

	@Override
    public String getStaticLabel() {
        return "CDN simulator";
    }
	
	@Override
    public TestElement createTestElement() {
		CDNSimulationSampler sampler = new CDNSimulationSampler();
        modifyTestElement(sampler);
        return sampler;
    }
}