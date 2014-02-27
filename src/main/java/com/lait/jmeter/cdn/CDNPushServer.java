package com.lait.jmeter.cdn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.net.httpserver.HttpServer;

class WorkerThread extends Thread {
	private Socket socket;
	private static final CDNSimulationSampler PreloadSampler = new CDNSimulationSampler();

	public WorkerThread(Socket s) {
		this.socket = s;
	}
	
	@Override
	public void run() {
		try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			
            while(!Thread.interrupted()) {
            	/*CDN push ∏Ò Ω
            	 *  PUSH;URL;
            	 * 
            	 */
				String line = input.readLine();
				System.out.println(line);
				output.write("Got it");
				output.flush();
				String url = "http://www.baidu.com/";
				
				//Remove it before loading
				CDN.getInstance().remove(url);
				PreloadSampler.loadPagesManually(url);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


public class CDNPushServer extends Thread {
	private int port;
	private CDN cdn;
	
	public CDNPushServer(CDN cdn) {
		this.port = 10086;
		this.cdn = cdn;
	}
	
	@Override
	public void run() {
		ServerSocket ss;
		try {
			ss = new ServerSocket(port);
			while (!Thread.interrupted()) {				
				Socket s=ss.accept();
				byte [] buf = new byte[1024];
				s.close();
				ss.close();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
}
