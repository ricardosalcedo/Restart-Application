package com.bts.connector;

import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


public class JSchExampleSSHConnection {

	/**
	 * JSch Example Tutorial
	 * Java SSH Connection Program
	 */
	public static void main(String[] args) {
	    String host="rh722.banktrade.com";
	    String user="root";
	    String password="BT7nskrp";
	    String execWsadmin="/opt/IBM/WebSphere/AppServer/bin/wsadmin.sh";
	    String wasUser = "admin";
	    String wasPassword = "admin$";
	    String appManager = "set appManager [$AdminControl queryNames type=ApplicationManager,*]";
	    String stopApp = "$AdminControl invoke $appManager stopApplication UC_BanktradeKbank";
	    String startApp = "$AdminControl invoke $appManager startApplication UC_BanktradeKbank";
	    String appStatus = "$AdminControl completeObjectName type=Application,name=UC_BanktradeKbank,*";
	    try{
	    	
	    	java.util.Properties config = new java.util.Properties(); 
	    	config.put("StrictHostKeyChecking", "no");
	    	JSch jsch = new JSch();
	    	Session session=jsch.getSession(user, host, 22);
	    	session.setPassword(password);
	    	session.setConfig(config);
	    	session.connect();
	   		System.out.println("Connected");
	 
	    	Channel channel=session.openChannel("exec");
	        ((ChannelExec)channel).setCommand(execWsadmin);
	        channel.setInputStream(null);
	        ((ChannelExec)channel).setErrStream(System.err);
	        
	        InputStream in=channel.getInputStream();
	        OutputStream out=channel.getOutputStream();
	        channel.connect();
	        
	        out.write((wasUser+"\n"+wasPassword+"\n").getBytes()); 
	        out.flush(); 
	        	        
	        byte[] tmp=new byte[1024];
	        int counter = 0;
	        Boolean startFlag = false;
	        Boolean status = false;
	        while(true){
	        String response = null;
	          while(in.available()>0){
	            int i=in.read(tmp, 0, 1024);
	            if(i<0)break;
	            response = new String(tmp, 0, i);
	            System.out.print(response);
	            if (response.indexOf("WebSphere:name=UC_BanktradeKbank,process=") > 0) {
	            	System.out.println("\nApplication Running");
	            }
	            if (status) {
	            	channel.disconnect();
	            	break;
	            }
	            if (startFlag) {
	            	System.out.println("\nChecking Application status");
	            	out.write((appStatus+"\n").getBytes()); 
	            	out.flush();
	            	status = true;
	            }
	            if (response.indexOf("wsadmin>") > 0 && counter > 0 && !startFlag) {
	            	System.out.println("\nRestarting Application");
	              out.write((stopApp+"\n").getBytes()); 
				  out.flush();
				  out.write((startApp+"\n").getBytes()); 
				  out.flush();
				  startFlag = true;
	            }
	            if (response.indexOf("wsadmin>") > 0 && counter == 0) {
	            	System.out.println("\nInitializing Application Manager");
					  out.write((appManager+"\n").getBytes()); 
					  out.flush(); 
					  counter++;
	            }
	          }
	          if(channel.isClosed()){
	            System.out.println("exit-status: "+channel.getExitStatus());
	            break;
	          }

		      try{Thread.sleep(1000);}catch(Exception ee){}
	        }
	        channel.disconnect();
	        session.disconnect();
	        System.out.println("DONE");
	    }catch(Exception e){
	    	//e.printStackTrace();
	    	System.out.println(e.getMessage());
	    }

	}
}

