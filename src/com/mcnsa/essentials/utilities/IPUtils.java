package com.mcnsa.essentials.utilities;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.ParseException;

public class IPUtils {
	public static InetAddress parseIpAddress(String ip) throws ParseException {
		String[] parts = ip.split("\\.");
		if(parts.length != 4) {
			throw new ParseException("IP address must be in the format 'xxx.xxx.xxx.xxx'", -1);
		}

		byte[] data = new byte[4];
		for(int i = 0; i < 4; i++) {
			try {
				int part = Integer.parseInt(parts[i]);
				if(part > 255 || part < 0) {
					throw new ParseException("IP address fields must be in the range [0, 255]", i);
				}
				
				data[i] = (byte)part;
			}
			catch(NumberFormatException e) {
				throw new ParseException("IP address fields must be integers!", i);
			}
		}

		try {
			return InetAddress.getByAddress(data);
		}
		catch (UnknownHostException e) {
			throw new Error("UnknownHostException somehow thrown when creating an InetAddress", e);
		}
	}
	
	public static String stripIP(InetSocketAddress addr) {
		return stripIP(addr.getAddress());
	}
	
	public static String stripIP(InetAddress addr) {
		String ipString = addr.toString();
		// strip off the starting '/'
		if(ipString.startsWith("/")) {
			ipString = ipString.substring(1);
		}
		// strip off the back port number
		if(ipString.contains(":")) {
			ipString = ipString.split(":", 2)[0];
		}
		return ipString;
	}
}
