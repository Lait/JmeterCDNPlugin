package com.lait.jmeter.cdn.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.config.gui.MultipartUrlConfigGui;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import com.lait.jmeter.cdn.CDNSimulationJavaImpl;
import com.lait.jmeter.cdn.CDNSimulationSampler;

public class CDNSimulationSamplerGui extends HttpTestSampleGui {
    @Override
    public String getStaticLabel() {
        return "CDN simulator";
    }
	
	@Override
    public TestElement createTestElement() {
		//把httpsamplerproxy的内容也封装进去
        HTTPSamplerBase sampler = new CDNSimulationSampler();
        modifyTestElement(sampler);
        return sampler;
    }
}
