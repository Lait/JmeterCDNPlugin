package com.lait.jmeter.cdn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

class WorkerThread extends Thread {
	private Socket socket;

	public WorkerThread(Socket s) {
		this.socket = s;
	}
	
	@Override
	public void run() {
		try {
            // Wrapper the InputStream to BufferedReader
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Wrapper the OutputStream to BufferedWriter
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			while(!Thread.interrupted()) {
				String line = input.readLine();
				System.out.println(line);
				output.write("Got it");
				output.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


public class CDNPushServer extends Thread {
	private int port;
	private CDNCache<String, CacheEntry> cache;
	
	public CDNPushServer(CDNCache<String, CacheEntry> cache) {
		this.port = 10086;
		this.cache = cache;
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
