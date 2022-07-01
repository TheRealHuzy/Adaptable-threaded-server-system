package org.foi.nwtis.ihuzjak.zadaca_1;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServerGlavniTest {
	
	ServerGlavni serverGlavni = null;
	
	String arg = "NWTiS_ihuzjak_4.txt";
	String argument = "^(\\w+).(txt|bin|json|xml)$";;
	
	int port = 8003;
	
	@BeforeEach
	void setUp() throws Exception {
		serverGlavni = new ServerGlavni(8003, 10);
	}

	@AfterEach
	void tearDown() throws Exception {
		serverGlavni = null;
	}
	
	@Test
	void provjeriTestIspitajArgument() {
		assertTrue(testIspitajArgument(arg));
	}
	@Test
	boolean testIspitajArgument(String arg) {
		
		Pattern pArgument = Pattern.compile(argument);
		Matcher mArgument = pArgument.matcher(arg.toString());
		if (mArgument.matches()) {
			return true;
		}
		return false;
	}
	
	@Test
	void testUcitavanjePodataka() {
		assertNull(ServerGlavni.konfig);
		ServerGlavni.ucitavanjePodataka("NWTiS_ihuzjak_4.txt");
		assertNotNull(ServerGlavni.konfig);
		assertNotEquals(0, ServerGlavni.konfig.dajSvePostavke().size());
	}
	
	@Test
	void ispitajTestIspitajPort() {
		assertTrue(testIspitajPort(port));
	}
	
	@Test
	boolean testIspitajPort(int port) {
		
		try {
			ServerSocket s = new ServerSocket(port);
			s.close();
		} catch (IOException e) {
			return true;
		}
		return false;
	}

}
