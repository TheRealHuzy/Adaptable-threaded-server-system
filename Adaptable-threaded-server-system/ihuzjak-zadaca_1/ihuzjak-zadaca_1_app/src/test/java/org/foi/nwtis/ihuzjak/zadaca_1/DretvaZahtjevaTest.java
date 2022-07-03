package org.foi.nwtis.ihuzjak.zadaca_1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DretvaZahtjevaTest {

	DretvaZahtjeva dretvaZahtjeva = null;
	ServerGlavni serverGlavni = null;

	String imeLozinka = "^(USER \\w+ PASSWORD \\w+) (.*)$";
	String airport = "^AIRPORT$";
	String airportIcao = "^AIRPORT ([A-Z]{4})$";
	String airportIcaoBroj = "^AIRPORT [A-Z]{4} [+-]?[0-9]+$";
	String meteoIcao = "^METEO ([A-Z]{4})$";
	String meteoIcaoDatum = "^METEO ([A-Z]{4}) (\\d{4}-\\d{2}-\\d{2})$";
	String tempTempTemp = "^TEMP [+-]?([0-9]+[\\,\\.]?[0-9]*|[\\,\\.][0-9]+)"
			+ " [+-]?([0-9]+[\\,\\.]?[0-9]*|[\\,\\.][0-9]+)$";
	String tempTempTempDatum = "^TEMP [+-]?([0-9]+[\\,\\.]?[0-9]*|[\\,\\.][0-9]+) "
			+ "[+-]?([0-9]+[\\,\\.]?[0-9]*|[\\,\\.][0-9]+) ?(\\d{4}-\\d{2}-\\d{2})$";
	String distanceIcaoIcao = "^DISTANCE ([A-Z]{4}) ([A-Z]{4})$";
	String distanceClear = "^DISTANCE CLEAR$";
	String cacheBackup = "^CACHE BACKUP$";
	String cacheRestore = "^CACHE RESTORE$";
	String cacheClear = "^CACHE CLEAR$";
	String cacheStat = "^CACHE STAT$";
	Matcher mImeLozinka;
	Matcher mMeteoIcao;
	Matcher mMeteoIcaoDatum;
	Matcher mTempTempTemp;
	Matcher mTempTempTempDatum;
	Matcher mAirport;
	Matcher mAirportIcao;
	Matcher mAirportIcaoBroj;
	Matcher mDistanceIcaoIcao;
	Matcher mDistanceClear;
	Matcher mCacheBackup;
	Matcher mCacheRestore;
	Matcher mCacheClear;
	Matcher mCacheStat;

	String komanda = "METEO LDZA";

	List<Korisnik> korisnici = new ArrayList<>();
	
	CacheServera cs = new CacheServera();
	
	static SimpleDateFormat isoFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

	String imePrivremeneDatoteke = "Stat.txt";
	File privremenaDatoteka = new File(imePrivremeneDatoteke);
	boolean cacheIspravno;
	String imeDatoteke = "Cache.txt";
	File datoteka = new File(imeDatoteke);
	
	@Test
	@Order(1)
	void ispitajTestStvoriMatchere() {
		assertNull(this.mMeteoIcao);
		assertNull(this.mMeteoIcaoDatum);
		assertNull(this.mTempTempTemp);
		assertNull(this.mTempTempTempDatum);
		assertNull(this.mAirport);
		assertNull(this.mAirportIcao);
		assertNull(this.mAirportIcaoBroj);
		assertNull(this.mDistanceIcaoIcao);
		assertNull(this.mDistanceClear);
		assertNull(this.mCacheBackup);
		assertNull(this.mCacheRestore);
		assertNull(this.mCacheClear);
		assertNull(this.mCacheStat);
	}

	void testStvoriMatchere(String komanda) {

		Pattern pMeteoIcao = Pattern.compile(meteoIcao);
		Pattern pMeteoIcaoDatum = Pattern.compile(meteoIcaoDatum);
		Pattern pTempTempTemp = Pattern.compile(tempTempTemp);
		Pattern pTempTempTempDatum = Pattern.compile(tempTempTempDatum);

		mMeteoIcao = pMeteoIcao.matcher(komanda.toString());
		mMeteoIcaoDatum = pMeteoIcaoDatum.matcher(komanda.toString());
		mTempTempTemp = pTempTempTemp.matcher(komanda.toString());
		mTempTempTempDatum = pTempTempTempDatum.matcher(komanda.toString());

		Pattern pAirport = Pattern.compile(airport);
		Pattern pAirportIcao = Pattern.compile(airportIcao);
		Pattern pAirportIcaoBroj = Pattern.compile(airportIcaoBroj);

		mAirport = pAirport.matcher(komanda.toString());
		mAirportIcao = pAirportIcao.matcher(komanda.toString());
		mAirportIcaoBroj = pAirportIcaoBroj.matcher(komanda.toString());

		Pattern pDistanceIcaoIcao = Pattern.compile(distanceIcaoIcao);
		Pattern pDistanceClear = Pattern.compile(distanceClear);

		mDistanceIcaoIcao = pDistanceIcaoIcao.matcher(komanda.toString());
		mDistanceClear = pDistanceClear.matcher(komanda.toString());

		Pattern pCacheBackup = Pattern.compile(cacheBackup);
		Pattern pCacheRestore = Pattern.compile(cacheRestore);
		Pattern pCacheClear = Pattern.compile(cacheClear);
		Pattern pCacheStat = Pattern.compile(cacheStat);

		mCacheBackup = pCacheBackup.matcher(komanda.toString());
		mCacheRestore = pCacheRestore.matcher(komanda.toString());
		mCacheClear = pCacheClear.matcher(komanda.toString());
		mCacheStat = pCacheStat.matcher(komanda.toString());
	}

	@Test
	@Order(2)
	void ispitajTestStvoriMatchere2() {
		testStvoriMatchere("METEO LDZA");
		
		assertNotNull(this.mMeteoIcao);
		assertNotNull(this.mMeteoIcaoDatum);
		assertNotNull(this.mTempTempTemp);
		assertNotNull(this.mTempTempTempDatum);
		assertNotNull(this.mAirport);
		assertNotNull(this.mAirportIcao);
		assertNotNull(this.mAirportIcaoBroj);
		assertNotNull(this.mDistanceIcaoIcao);
		assertNotNull(this.mDistanceClear);
		assertNotNull(this.mCacheBackup);
		assertNotNull(this.mCacheRestore);
		assertNotNull(this.mCacheClear);
		assertNotNull(this.mCacheStat);
	}

	@Test
	@Order(3)
	void ispitajTestPopraviAkoTreba() {
		String odgovor = "OK LDZA 4,4 87,0 1015,0 2021-01-07 16:01:01.718";
		String icao = "LDZA";
		String odgovor2 = testPopraviAkoTreba(odgovor, icao);
		assertEquals(odgovor, odgovor2);
	}

	String testPopraviAkoTreba(String odgovor, String icao) {

		String provjeraIcao = odgovor.substring(3, 7);
		String ostatak = odgovor.substring(3);
		Pattern pIcao = Pattern.compile("^[A-Z]{4}$");
		Matcher mIcao = pIcao.matcher(provjeraIcao.toString());
		if (mIcao.matches()) {
			return odgovor;
		}
		return "OK " + icao + " " + ostatak;
	}

	@Test
	@Order(4)
	void ispitajTestPotvrdiPrijavu() {
		Korisnik k = new Korisnik("Kos", "Pero", "pkos", "123456");
		Korisnik k2 = new Korisnik("Begra", "Miro", "mbegra", "123456");
		Pattern pImeLozinka = Pattern.compile(imeLozinka);
		mImeLozinka = pImeLozinka.matcher("USER Pero PASSWORD 123456 METEO LDZA");
		korisnici.add(k);
		korisnici.add(k2);
		boolean ispit = testPotvrdiPrijavu(false);
		assertTrue(ispit);
	}

	boolean testPotvrdiPrijavu(boolean dobraPijava) {

		if (mImeLozinka.matches()) {
			String imeLozinka = mImeLozinka.group(1).toString();
			String ime = imeLozinka.split(" ")[1];
			String lozinka = imeLozinka.split(" ")[3];

			for (Korisnik k : korisnici) {
				if (k.getIme().compareTo(ime) == 0 && k.getLozinka().compareTo(lozinka) == 0) {
					dobraPijava = true;
					break;
				}
			}
		} else {
			dobraPijava = false;
		}
		return dobraPijava;
	}
	
	
	@Test
	@Order(5)
	void ispitajTestProvjeriUnosUMemoriju() {
		Pattern pAirport = Pattern.compile(airport);
		Pattern pAirportIcao = Pattern.compile(airportIcao);
		Pattern pAirportIcaoBroj = Pattern.compile(airportIcaoBroj);
		Pattern pDistanceIcaoIcao = Pattern.compile(distanceIcaoIcao);
		mAirport = pAirport.matcher(komanda.toString());
		mAirportIcao = pAirportIcao.matcher(komanda.toString());
		mAirportIcaoBroj = pAirportIcaoBroj.matcher(komanda.toString());
		mDistanceIcaoIcao = pDistanceIcaoIcao.matcher(komanda.toString());
		
		boolean ispit = testProvjeriUnosUMemoriju("USER Pero PASSWORD 123456 AIRPORT");
		assertFalse(ispit);
	}
	
	boolean testProvjeriUnosUMemoriju(String komanda) {

		
		if(mAirport.matches() || mAirportIcao.matches() || mAirportIcaoBroj.matches()
				|| mDistanceIcaoIcao.matches()) {
			for (Memorija m : cs.memorija) {
				if (m.komanda.compareTo(komanda) == 0) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	@Test
	@Order(6)
	void ispitajTestSortirajMemoriju() {
		testSortirajMemoriju();
	}
	
	void testSortirajMemoriju() {
		Collections.sort(cs.memorija, Collections.reverseOrder());
	}
	
	@Test
	@Order(7)
	void ispitajTestSpremiUMemoriju() {
		testSpremiUMemoriju("USER Pero PASSWORD 123456 AIRPORT", "OK LDZA; LOWW;");
		assertNotNull(cs.memorija);
	}
	
	void testSpremiUMemoriju(String komanda, String odgovor) {
		
		Date date = new Date();
		String datumVrijeme = isoFormat.format(new Timestamp(date.getTime()));
		Memorija m = new Memorija(komanda, odgovor, 1, datumVrijeme);
		cs.memorija.add(m);
		return;
	}
	
	@Test
	@Order(8)
	void ispitajTestProvjeriMemoriju() {
		String komanda = "USER Pero PASSWORD 123456 AIRPORT";
		String odgovor = testProvjeriMemoriju(komanda);
		assertEquals(odgovor, "");
	}
	
	public String testProvjeriMemoriju(String komanda) {
		
		for (Memorija m : cs.memorija) {
			if (m.komanda.compareTo(komanda) == 0) {
				Date date = new Date();
				m.zadnjeVrijeme = isoFormat.format(new Timestamp(date.getTime()));
				m.brojPoziva++;
				return m.odgovor;
			}
		}
		return "";
	}
	
	@Test
	@Order(9)
	void ispitajTestOcistiMemoriju() {
		String odgovor = testOcistiMemoriju();
		assertEquals(odgovor, "OK");
	}
	
	String testOcistiMemoriju() {
		cs.memorija.clear();
		return "OK";
	}
	
	@Test
	@Order(10)
	void ispitajTestPrebrojiZnakove() {
		int odgovor = testPrebrojiZnakove("OK LDZA; LOWW;");
		assertNotEquals(odgovor, 0);
	}
	
	public int testPrebrojiZnakove(String odgovor) {
		
		return odgovor.length();
	}
	
	@Test
	@Order(11)
	void ispitajTestStvoriStatIspis() {
		String komanda = "USER Pero PASSWORD 123456 AIRPORT";
		String odgovor = "OK LDZA; LOWW;";
		Date date = new Date();
		String datumVrijeme = isoFormat.format(new Timestamp(date.getTime()));
		Memorija m = new Memorija(komanda, odgovor, 1, datumVrijeme);
		cs.memorija.add(m);
		
		String odgovor2 = testStvoriStatIspis(m);
		assertNotNull(odgovor2);
		
	}
	
	String testStvoriStatIspis(Memorija m) {
		
		String brojPoziva = String.valueOf(3);
		String linija = "\n";
		linija += String.format("%-30s%15s%30s", m.komanda, brojPoziva, m.zadnjeVrijeme + "\\n");
		return linija;
	}
	
	@Test
	@Order(12)
	void ispitajTestUpisiUDatoteku() {
		cacheIspravno = true;
		testUpisiUDatoteku("Formatiran zapis");
		assertTrue(cacheIspravno);
	}
	
	void testUpisiUDatoteku(String odgovor) {
		
		try (FileWriter fw = new FileWriter(privremenaDatoteka);)
		{
			if (!privremenaDatoteka.exists()) {
				cacheIspravno = true;
			}
			fw.write(odgovor);
		} catch (IOException e) {
			cacheIspravno = false;
		}
	}
	
	@Test
	@Order(13)
	void ispitajTestCitajIzDatoteke() {
		String odgovor = testCitajIzDatoteke();
		assertNotNull(odgovor);
	}
	
	String testCitajIzDatoteke() {
		
		String odgovor = "";
		try (Scanner citac = new Scanner(privremenaDatoteka);)
		{
			while (citac.hasNextLine()) {
				String linija = citac.nextLine();
				odgovor += linija + "\n";
			}
		} catch (FileNotFoundException e) {
			cacheIspravno = false;
		}
		return odgovor;
	}
	
	@Test
	@Order(14)
	void ispitajTestIzbrisiPrivremenuDatoteku() {
		testIzbrisiPrivremenuDatoteku();
		assertFalse(privremenaDatoteka.exists());
	}
	
	void testIzbrisiPrivremenuDatoteku() {
		privremenaDatoteka.delete();
	}
	
	@Test
	@Order(15)
	void ispitajTestUcitajSerijalizinuDatoteku() {
		cacheIspravno = true;
		testUcitajSerijalizinuDatoteku();
		assertFalse(cacheIspravno);
	}
	
	void testUcitajSerijalizinuDatoteku(){
		
		try (FileInputStream fis = new FileInputStream(datoteka);
				ObjectInputStream ois = new ObjectInputStream(fis);)
		{
			CacheServera cs = (CacheServera) ois.readObject();
			serverGlavni.cs = cs;
		}
		catch (Exception e) {
			cacheIspravno = false;
		}
		return;
	}
	
	@Test
	@Order(16)
	void ispitajTestPisiUSerijalizinuDatoteku() {
		cacheIspravno = true;
		testPisiUSerijalizinuDatoteku();
		assertFalse(cacheIspravno);
	}
	
	void testPisiUSerijalizinuDatoteku(){
		
		try (FileOutputStream fos = new FileOutputStream(datoteka);
				ObjectOutputStream oos = new ObjectOutputStream(fos);)
		{
			oos.writeObject(serverGlavni.cs);
		}
		catch (Exception e) {
			cacheIspravno = false;
		}
		return;
	}
	
	@Test
	@Order(17)
	void ispitajTestIspisiStatistiku() {
		String odgovor = testIspisiStatistiku();
		assertNotEquals(odgovor, "");
	}
	
	String testIspisiStatistiku() {

		String odgovor = "OK ";
		String odgovor2 = "";
		try {
			odgovor2 += String.format("%-30s%5s%30s", "Predmet komande", "Broj korištenja",
					"Zadnje vrijeme" + "\\n");
			odgovor2 += "OK";
			odgovor += 25 +";\\n\n";
		} catch (Exception e) {
			odgovor = "ERROR 49 >> Problem sa privremenom datotekom!";
			return odgovor;
		}
		return odgovor + odgovor2;
	}
	
	@Test
	@Order(18)
	void ispitajTestIspitajIspravnost() {
		cacheIspravno = true;
		String odgovor = testIspitajIspravnost();
		assertEquals(odgovor, "OK");
	}
	
	String testIspitajIspravnost() {
		if (cacheIspravno) {
			return "OK";
		} else {
			return "";
		}
	}
	
	/*
	@Test
	@Order(7)
	void ispitaj() {
		
	}*/
}
