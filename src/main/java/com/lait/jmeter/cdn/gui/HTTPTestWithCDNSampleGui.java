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
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.config.gui.MultipartUrlConfigGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import com.lait.jmeter.cdn.CDNSimulationSampler;

public class HTTPTestWithCDNSampleGui extends AbstractSamplerGui implements ItemListener {

    private static final long serialVersionUID = 240L;
    
    private static final Font FONT_VERY_SMALL = new Font("SansSerif", Font.PLAIN, 9);
    
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);

    private MultipartUrlConfigGui urlConfigGui;
    

    private JCheckBox getImages;
    
    private JCheckBox concurrentDwn;
    
    private JTextField concurrentPool; 

    private JCheckBox isMon;

    private JCheckBox useMD5;

    private JLabel labelEmbeddedRE = new JLabel(JMeterUtils.getResString("web_testing_embedded_url_pattern")); // $NON-NLS-1$

    private JTextField embeddedRE; // regular expression used to match against embedded resource URLs

    private JTextField sourceIpAddr; // does not apply to Java implementation
    
    private JComboBox sourceIpType = new JComboBox(HTTPSamplerBase.getSourceTypeList());

    private final boolean isAJP;

	private JCheckBox useHttpCacheControl;

	private JTextArea resourceUrls;
    
    public HTTPTestWithCDNSampleGui() {
        isAJP = false;
        init();
    }

    // For use by AJP
    protected HTTPTestWithCDNSampleGui(boolean ajp) {
        isAJP = ajp;
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    //Set default value
    public void configure(TestElement element) {
        super.configure(element);
               
        urlConfigGui.configure(element);
        
        final CDNSimulationSampler samplerBase = (CDNSimulationSampler) element;
		useHttpCacheControl.setSelected(samplerBase.USE_CACHE_CONTROL);
		resourceUrls.setText(samplerBase.PAGES_LIST);
        getImages.setSelected(samplerBase.isImageParser());
        concurrentDwn.setSelected(samplerBase.isConcurrentDwn());
        concurrentPool.setText(samplerBase.getConcurrentPool());
        isMon.setSelected(samplerBase.isMonitor());
        useMD5.setSelected(samplerBase.useMD5());
        embeddedRE.setText(samplerBase.getEmbeddedUrlRE());
        if (!isAJP) {
            sourceIpAddr.setText(samplerBase.getIpSource());
            sourceIpType.setSelectedIndex(samplerBase.getIpSourceType());
        }
    }

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

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        
        urlConfigGui.modifyTestElement(sampler);
        
        final CDNSimulationSampler cdnSampler = (CDNSimulationSampler) sampler;
        cdnSampler.loadPagesManually(this.resourceUrls.getText());
        cdnSampler.useHttpCacheControl(this.useHttpCacheControl.isSelected());
        cdnSampler.setImageParser(getImages.isSelected());
        enableConcurrentDwn(getImages.isSelected());
        cdnSampler.setConcurrentDwn(concurrentDwn.isSelected());
        cdnSampler.setConcurrentPool(concurrentPool.getText());
        cdnSampler.setMonitor(isMon.isSelected());
        cdnSampler.setMD5(useMD5.isSelected());
        cdnSampler.setEmbeddedUrlRE(embeddedRE.getText());
        if (!isAJP) {
            cdnSampler.setIpSource(sourceIpAddr.getText());
            cdnSampler.setIpSourceType(sourceIpType.getSelectedIndex());
        }
        this.configureTestElement(sampler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelResource() {
        return "web_testing_title"; // $NON-NLS-1$
    }

    private void init() {// called from ctor, so must not be overridable
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        // URL CONFIG
        urlConfigGui = new MultipartUrlConfigGui(true, !isAJP);
        add(urlConfigGui, BorderLayout.CENTER);

        // Bottom (embedded resources, source address and optional tasks)
        
        JPanel bottomPane = new VerticalPanel();
        bottomPane.add(createCDNConfigPanel());
        bottomPane.add(createEmbeddedRsrcPanel());
        JPanel optionAndSourcePane = new HorizontalPanel();
        optionAndSourcePane.add(createSourceAddrPanel());
        optionAndSourcePane.add(createOptionalTasksPanel());
        bottomPane.add(optionAndSourcePane);
        add(bottomPane, BorderLayout.SOUTH);
    }
    
    protected JPanel createCDNConfigPanel() {
    	final JPanel cdnConfigPanel = new VerticalPanel();
    	cdnConfigPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "CDN preload"));
    	this.useHttpCacheControl = new JCheckBox();
    	this.resourceUrls = new JTextArea(3, 10);
    	cdnConfigPanel.add(new JLabel("Use Cache-Control"));
    	cdnConfigPanel.add(useHttpCacheControl);
    	cdnConfigPanel.add(new JLabel("Resource urls"));
    	cdnConfigPanel.add(resourceUrls);
    	return cdnConfigPanel;
    }

    protected JPanel createEmbeddedRsrcPanel() {
        final JPanel embeddedRsrcPanel = new VerticalPanel();
        embeddedRsrcPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("web_testing_retrieve_title"))); // $NON-NLS-1$

        final JPanel checkBoxPanel = new HorizontalPanel();
        // RETRIEVE IMAGES
        getImages = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images")); // $NON-NLS-1$
        getImages.setFont(FONT_SMALL);
        // add a listener to activate or not concurrent dwn.
        getImages.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) { enableConcurrentDwn(true); }
                else { enableConcurrentDwn(false); }
            }
        });
        // Download concurrent resources
        concurrentDwn = new JCheckBox(JMeterUtils.getResString("web_testing_concurrent_download")); // $NON-NLS-1$
        concurrentDwn.setFont(FONT_SMALL);
        concurrentDwn.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (getImages.isSelected() && e.getStateChange() == ItemEvent.SELECTED) { concurrentPool.setEnabled(true); }
                else { concurrentPool.setEnabled(false); }
            }
        });
        concurrentPool = new JTextField(2); // 2 column size
        concurrentPool.setFont(FONT_SMALL);
        concurrentPool.setMaximumSize(new Dimension(30,20));

        checkBoxPanel.add(getImages);
        checkBoxPanel.add(concurrentDwn);
        checkBoxPanel.add(concurrentPool);
        embeddedRsrcPanel.add(checkBoxPanel);

        // Embedded URL match regex
        labelEmbeddedRE.setFont(FONT_SMALL);
        checkBoxPanel.add(labelEmbeddedRE);
        embeddedRE = new JTextField(10);
        checkBoxPanel.add(embeddedRE);
        embeddedRsrcPanel.add(checkBoxPanel);

        return embeddedRsrcPanel;
    }

    protected JPanel createOptionalTasksPanel() {
        // OPTIONAL TASKS
        final JPanel checkBoxPanel = new HorizontalPanel();
        checkBoxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("optional_tasks"))); // $NON-NLS-1$

        // Is monitor
        isMon = new JCheckBox(JMeterUtils.getResString("monitor_is_title")); // $NON-NLS-1$
        isMon.setFont(FONT_SMALL);
        // Use MD5
        useMD5 = new JCheckBox(JMeterUtils.getResString("response_save_as_md5")); // $NON-NLS-1$
        useMD5.setFont(FONT_SMALL);

        checkBoxPanel.add(isMon);
        checkBoxPanel.add(useMD5);

        return checkBoxPanel;
    }
    
    protected JPanel createSourceAddrPanel() {
        final JPanel sourceAddrPanel = new HorizontalPanel();
        sourceAddrPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("web_testing_source_ip"))); // $NON-NLS-1$

        if (!isAJP) {
            // Add a new field source ip address (for HC implementations only)
            sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
            sourceIpType.setFont(FONT_VERY_SMALL);
            sourceAddrPanel.add(sourceIpType);

            sourceIpAddr = new JTextField();
            sourceAddrPanel.add(sourceIpAddr);
        }
        return sourceAddrPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        super.clearGui();
        getImages.setSelected(false);
        concurrentDwn.setSelected(false);
        concurrentPool.setText(String.valueOf(HTTPSamplerBase.CONCURRENT_POOL_SIZE));
        enableConcurrentDwn(false);
        isMon.setSelected(false);
        useMD5.setSelected(false);
        urlConfigGui.clear();
        embeddedRE.setText(""); // $NON-NLS-1$
        if (!isAJP) {
            sourceIpAddr.setText(""); // $NON-NLS-1$
            sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
        }
    }
    
    private void enableConcurrentDwn(boolean enable) {
        if (enable) {
            concurrentDwn.setEnabled(true);
            labelEmbeddedRE.setEnabled(true);
            embeddedRE.setEnabled(true);
            if (concurrentDwn.isSelected()) {
                concurrentPool.setEnabled(true);
            }
        } else {
            concurrentDwn.setEnabled(false);
            concurrentPool.setEnabled(false);
            labelEmbeddedRE.setEnabled(false);
            embeddedRE.setEnabled(false);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            enableConcurrentDwn(true);
        } else {
            enableConcurrentDwn(false);
        }
    }

}
