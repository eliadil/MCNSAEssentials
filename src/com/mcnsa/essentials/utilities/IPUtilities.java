package com.mcnsa.essentials.utilities;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

public class IPUtilities {
	// https://gist.github.com/ryantenney/3164334#file-parseipaddress-java
	public static InetAddress parseIpAddress(String ip) throws IllegalArgumentException {
		StringTokenizer tok = new StringTokenizer(ip, ".");

		if (tok.countTokens() != 4) {
			throw new IllegalArgumentException("IP address must be in the format 'xxx.xxx.xxx.xxx'");
		}

		byte[] data = new byte[4];
		int i = 0;
		while (tok.hasMoreTokens()) {
			String strVal = tok.nextToken();
			try {
				int val = Integer.parseInt(tok.nextToken(), 10);

				if (val < 0 || val > 255) {
					throw new IllegalArgumentException("Illegal value '" + val + "' at byte " + (i + 1) + " in the IP address.");
				}

				data[i++] = (byte) Integer.parseInt(tok.nextToken(), 10);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Illegal value '" + strVal + "' at token " + (i + 1) + " in the IP address.", e);
			}
		}

		try {
			return InetAddress.getByAddress(ip, data);
		} catch (UnknownHostException e) {
			// This actually can't happen since the method InetAddress.getByAddress(String, byte[])
			// doesn't perform any lookups and we have already guaranteed that the length of data is 4
			throw new Error("UnknownHostException somehow thrown when creating an InetAddress", e);
		}
	}
}
