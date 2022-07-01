package org.foi.nwtis.ihuzjak.zadaca_1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.foi.nwtis.ihuzjak.vjezba_03.konfiguracije.Konfiguracija;

// TODO: Auto-generated Javadoc
/**
 * The Class DretvaZahtjeva.
 */
public class DretvaZahtjeva extends Thread {
	
	/** Referenca na ServerGlavni */
	ServerGlavni serverGlavni = null;
	
	/** Objekt konfiguracije */
	Konfiguracija konfig = null;
	
	/** Definiran soket */
	Socket veza = null;
	
	/** Broj dretve */
	int indeksDretve;
	
	/** Lista korisnika koja se preuzima od klase ServerGlavni */
	List<Korisnik> korisnici = new ArrayList<>();
	
	/** Objekti datoteke za međuspremnik i privremene datoteke za cacheStat */
	File datoteka, privremenaDatoteka;
	
	/** Imena datoteke za međuspremnik i privremene */
	String imeDatoteke, imePrivremeneDatoteke = "Stat.txt";
	
	/** Adresa servera ServerAerodroma */
	String SAerodromaAdresa;
	
	/** Port servera ServerAerodroma */
	int SAerodromaPort;
	
	/** Adresa servera ServerMeteo */
	String SMeteoAdresa;
	
	/** Port servera ServerMeteo*/
	int SMeteoPort;
	
	/** Adresa servera ServerUdaljenosti */
	String SUdaljenostiAdresa;
	
	/** Port servera ServerUdaljenosti */
	int SUdaljenostiPort;
	
	/** Maksimalno čekanje na odgovor */
	int maksCekanje;
	
	/** Format datuma koji sadrži i vrijeme u milisekundama */
	static SimpleDateFormat isoFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
	
	/** Pomoćna zastavica za obradu Cache naredbi */
	boolean cacheIspravno;
	
	/** Regularni izlaz za prvi dio komande */
	String imeLozinka = "^(USER \\w+ PASSWORD \\w+) (.*)$";
	
	/** Regularni izraz za komandu airport */
	String airport = "^AIRPORT$";
	
	/** Regularni izraz za komandu airportIcao */
	String airportIcao = "^AIRPORT ([A-Z]{4})$";
	
	/** Regularni izraz za komandu airporIcaoBroj */
	String airportIcaoBroj = "^AIRPORT [A-Z]{4} [+-]?[0-9]+$";
	
	/** Regularni izraz za komandu meteoIcao */
	String meteoIcao = "^METEO ([A-Z]{4})$";
	
	/** Regularni izraz za komandu meteoIcaoDatum */
	String meteoIcaoDatum = "^METEO ([A-Z]{4}) (\\d{4}-\\d{2}-\\d{2})$";
	
	/** Regularni izraz za komandu tempTempTemp */
	String tempTempTemp = "^TEMP [+-]?([0-9]+[\\,\\.]?[0-9]*|[\\,\\.][0-9]+)"
			+ " [+-]?([0-9]+[\\,\\.]?[0-9]*|[\\,\\.][0-9]+)$";
	
	/** Regularni izraz za komandu tempTempTempDatum */
	String tempTempTempDatum = "^TEMP [+-]?([0-9]+[\\,\\.]?[0-9]*|[\\,\\.][0-9]+) "
			+ "[+-]?([0-9]+[\\,\\.]?[0-9]*|[\\,\\.][0-9]+) ?(\\d{4}-\\d{2}-\\d{2})$";
	
	/** Regularni izraz za komandu distanceIcaoIcao */
	String distanceIcaoIcao= "^DISTANCE ([A-Z]{4}) ([A-Z]{4})$";
	
	/** Regularni izraz za komandu distanceClear */
	String distanceClear = "^DISTANCE CLEAR$";
	
	/** Regularni izraz za komandu cacheBackup */
	String cacheBackup = "^CACHE BACKUP$";
	
	/** Regularni izraz za komandu cacheRestore */
	String cacheRestore = "^CACHE RESTORE$";
	
	/** Regularni izraz za komandu cacheClear */
	String cacheClear = "^CACHE CLEAR$";
	
	/** Regularni izraz za komandu cacheStat */
	String cacheStat = "^CACHE STAT$";
	
	/** Matcher za prvi dio komande */
	Matcher mImeLozinka;
	
	/** Matcher za meteoIcao */
	Matcher mMeteoIcao;
	
	/** Matcher za meteoIcaoDatum */
	Matcher mMeteoIcaoDatum;
	
	/** Matcher za tempTempTemp */
	Matcher mTempTempTemp;
	
	/** Matcher za tempTempTempDatum */
	Matcher mTempTempTempDatum;
	
	/** Matcher za airport */
	Matcher mAirport;
	
	/** Matcher za airportIcao */
	Matcher mAirportIcao;
	
	/** Matcher za airportIcaoBroj */
	Matcher mAirportIcaoBroj;
	
	/** Matcher za distanceIcaoIcao */
	Matcher mDistanceIcaoIcao;
	
	/** Matcher za distanceClear */
	Matcher mDistanceClear;
	
	/** Matcher za cacheBackup */
	Matcher mCacheBackup;
	
	/** Matcher za cacheRestore */
	Matcher mCacheRestore;
	
	/** Matcher za cacheClear */
	Matcher mCacheClear;
	
	/** Matcher za cacheStat*/
	Matcher mCacheStat;
		
	/**
	 * Konstruktor klase DretvaZahtjeva.
	 *
	 * @param serverGlavni Referenca na glavni server
	 * @param veza Referenca na stvoreni soket
	 * @param konfig Referenca na konfiguraciju
	 * @param indeksDretve the Broj drtve koji određuje ServerGlavni
	 */
	public DretvaZahtjeva(ServerGlavni serverGlavni, Socket veza, Konfiguracija konfig, int indeksDretve) {
		super();
		this.serverGlavni = serverGlavni;
		this.konfig = konfig;
		this.veza = veza;
		this.indeksDretve = indeksDretve;
		this.korisnici = serverGlavni.korisnici;
	}

	/**
	 * Start je metoda koja prije pokretanja dretva iz refenrence na konfiguraciju postavlja određene varijable
	 */
	@Override
	public synchronized void start() {
		
		super.start();
		
		this.SAerodromaAdresa = konfig.dajPostavku("server.aerodroma.adresa");
		this.SAerodromaPort = Integer.parseInt(konfig.dajPostavku("server.aerodroma.port"));
		this.SMeteoAdresa = konfig.dajPostavku("server.meteo.adresa");
		this.SMeteoPort = Integer.parseInt(konfig.dajPostavku("server.meteo.port"));
		this.SUdaljenostiAdresa = konfig.dajPostavku("server.udaljenosti.adresa");
		this.SUdaljenostiPort = Integer.parseInt(konfig.dajPostavku("server.udaljenosti.port"));
		this.imeDatoteke = konfig.dajPostavku("datoteka.meduspremnika");
		this.maksCekanje = Integer.parseInt(konfig.dajPostavku("maks.cekanje"));
	}

	/**
	 * Run je glavna metoda dretve DretvaZahtjeva koja obrađuje zahtjev i komunicira sa serverom ServerGlavni
	 */
	@Override
	public synchronized void run() {
		
		Thread.currentThread().setName("ihuzjak_" + indeksDretve);
		try (InputStreamReader isr = new InputStreamReader(this.veza.getInputStream(), Charset.forName("UTF-8"));
			OutputStreamWriter osw = new OutputStreamWriter(this.veza.getOutputStream(), Charset.forName("UTF-8"));)
		{
			StringBuilder tekst = new StringBuilder();
			tekst = ucitajTekst(isr, tekst);
			this.veza.shutdownInput();
			
			Pattern pImeLozinka = Pattern.compile(imeLozinka);
			mImeLozinka = pImeLozinka.matcher(tekst.toString());
			boolean dobraPrijava = false;
			
			dobraPrijava = potvrdiPrijavu(osw, dobraPrijava);
			
			if (!dobraPrijava) {
				ispisNaOSW(osw, "ERROR 41 Netočno korsiničko ime i/ili lozinka!");
			} else {
				String komanda = mImeLozinka.group(2).toString();
				String odgovor = provjeriMemoriju(komanda);
				
				if(odgovor.compareTo("") != 0){
					ispisNaOSW(osw, odgovor);
					obavijestiServer();
					return;
				}
				
				stvoriMatchere(komanda);
				odgovor = provjeraSintakseObrade(osw, komanda, odgovor);
				
				if(odgovor.compareTo("") == 0) {
					return;
				}
				
				if(provjeriUnosUMemoriju(komanda)){
					spremiUMemoriju(komanda, odgovor);
				}
				ispisNaOSW(osw, odgovor);
			}		
		} catch (IOException e) {
			System.out.println("ERROR 49 >> Problem u stvaranju čitača i pisača za komunikaciju!");
		}
	}

	/**
	 * Provjera sintakse obrade je metoda koja određuje kako će se dobivena komanda obraditi
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param komanda Sadržaj primljene komande
	 * @param odgovor Referenca na odgovor koji će se generirati
	 * @return Sadržaj generiranog odgovora na komandu
	 */
	public String provjeraSintakseObrade(OutputStreamWriter osw, String komanda, String odgovor) {
		
		if(mTempTempTemp.matches() || mTempTempTempDatum.matches()) {
			obavijestiServer();
			odgovor = pozoviServer(osw, komanda.toString(), SMeteoAdresa, SMeteoPort);
		} else if (mMeteoIcao.matches() || mMeteoIcaoDatum.matches()) {
			obavijestiServer();
			String icao = komanda.split(" ")[1];
			String komanda2 = "AIRPORT " + icao;
			odgovor = pozoviServer(osw, komanda2.toString(), SAerodromaAdresa, SAerodromaPort);
			if (odgovor.startsWith("OK")) {
				odgovor = pozoviServer(osw, komanda.toString(), SMeteoAdresa, SMeteoPort);
				odgovor = popraviAkoTreba(odgovor, icao);
			}
		} else if (mAirport.matches() || mAirportIcao.matches() || mAirportIcaoBroj.matches()) {
			obavijestiServer();
			odgovor = pozoviServer(osw, komanda.toString(), SAerodromaAdresa, SAerodromaPort);
		} else if (mDistanceIcaoIcao.matches() || mDistanceClear.matches()){
			obavijestiServer();
			odgovor = pozoviServer(osw, komanda.toString(), SUdaljenostiAdresa, SUdaljenostiPort);
		} else if (mCacheBackup.matches()||mCacheRestore.matches()||mCacheClear.matches()||mCacheStat.matches()){
			serverGlavni.jednoDretveni = true;
			serverGlavni.prekidajucaDretva = this;
			obavijestiServer();
			
			try {
				wait();
			} catch (InterruptedException e) {
				ispisNaOSW(osw, "ERROR 49 >> Čekanje servera da se izvrše sve aktivne dretve"
						+ " je prekinuto!");
				return "";
			}
			
			odgovor = obradiCacheNaredbu(osw);
			serverGlavni.jednoDretveni = false;
			obavijestiServer();
		} else {
			ispisNaOSW(osw, "ERROR 40 >> Sintaksa komande nije uredu");
			return "";
		}
		return odgovor;
	}
	
	/**
	 * Popravi ako treba je metoda koja dodaje Icao, ako on nije poslan od strane servera MeteoIcao
	 *
	 * @param odgovor Sadržaj primljenog odgovora u koji se dodaje Icao
	 * @param icao Icao vrijednost koja se dodaje u odgovor
	 * @return novi odgovor s dodanim Icao ili bez, ako već postoji
	 */
	public String popraviAkoTreba(String odgovor, String icao) {
		
		String provjeraIcao = odgovor.substring(3, 7);
		String ostatak = odgovor.substring(3);
		Pattern pIcao = Pattern.compile("^[A-Z]{4}$");
		Matcher mIcao = pIcao.matcher(provjeraIcao.toString());
		if (mIcao.matches()) {
			return odgovor;
		}
		return "OK " + icao + " " + ostatak;
	}

	/**
	 * Obavijesti server je metoda koja obaviještava servera da se može dalje izvršavati
	 */
	public synchronized void obavijestiServer() {
		synchronized (serverGlavni) {
			serverGlavni.notify();
		}
	}

	/**
	 * Potvrdi prijavu je metoda koja obavlja autentifikaciju
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param dobraPijava Zastavica koja se postavlja na određenu vrijednost
	 * @return true Vraća se istina, ako je autentifikacija uspješna
	 */
	public boolean potvrdiPrijavu(OutputStreamWriter osw, boolean dobraPijava) {
		
		if (mImeLozinka.matches()) {
			String imeLozinka = mImeLozinka.group(1).toString();
			String ime = imeLozinka.split(" ")[1];
			String lozinka = imeLozinka.split(" ")[3];
			
			for(Korisnik k: korisnici) {
				if (k.getIme().compareTo(ime) == 0 && k.getLozinka().compareTo(lozinka) == 0) {
					dobraPijava = true;
					break;
				}
			}
		} else {
			ispisNaOSW(osw, "ERROR 40 Sintaksa komande nije uredu!");
		}
		return dobraPijava;
	}
	

	
	/**
	 * Ucitaj tekst je metoda koja čita dobivenu komandu i stvara StringBuilder
	 *
	 * @param isr Objekt čitača za čitanje odgovora
	 * @param tekst Primljena komanda
	 * @return the StringBuilder koji sadrži primljenu komandu
	 */
	public StringBuilder ucitajTekst(InputStreamReader isr, StringBuilder tekst) {
		while (true) {
			int i = 0;
			try {
				i = isr.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (i == -1) {
				break;
			}
			tekst.append((char) i);
		}
		return tekst;
	}
	

	/**
	 * Stvori matchere je metoda koja inicijalizira objekte tipa Matcher
	 *
	 * @param komanda Sadržaj primljene komande
	 */
	public void stvoriMatchere(String komanda) {
		
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

	
	/**
	 * Provjeri unos u memoriju je koja dodaje zapis u memoriju jedino ako odgovara određenim Matcherima
	 *
	 * @param komanda Sadržaj primljene komande
	 * @return true Vraća istinu jedino, ako je "matchan" i nije pronađen u memoriji
	 */
	public boolean provjeriUnosUMemoriju(String komanda) {
		if(mAirport.matches() || mAirportIcao.matches() || mAirportIcaoBroj.matches()
				|| mDistanceIcaoIcao.matches()) {
			for (Memorija m : serverGlavni.cs.memorija) {
				if (m.komanda.compareTo(komanda) == 0) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	

	/**
	 * Pozovi server je metoda koja šalje prepoznatu komandu na određeni server
	 *
	 * @param osw2 Objekt pisača za pisanje odgovora
	 * @param komanda Sadržaj primljene komande
	 * @param adresa Adresa na koju se komanda šalje
	 * @param port Port na koji se komanda šalje
	 * @return Odgovor server na komandu
	 */
	public String pozoviServer(OutputStreamWriter osw2, String komanda, String adresa, int port) {
		
		InetSocketAddress isa = new InetSocketAddress(adresa, port);
		try (Socket veza = new Socket();)
		{
			veza.connect(isa, maksCekanje);
			InputStreamReader isr = new InputStreamReader(veza.getInputStream(), Charset.forName("UTF-8"));
			OutputStreamWriter osw = new OutputStreamWriter(veza.getOutputStream(), Charset.forName("UTF-8"));
			osw.write(komanda);
			osw.flush();
			veza.shutdownOutput();
			StringBuilder tekst = new StringBuilder();
			while (true) {
				int i = isr.read();
				if (i == -1) {
					break;
				}
				tekst.append((char) i);
			}
			veza.shutdownInput();
			veza.close();
			return tekst.toString();
		} catch (IOException e) {
			if (port == SMeteoPort) {
				ispisNaOSW(osw2, "ERROR 42 >> Greška pri spajanju na ServerMeteo!");
			} else if (port == SAerodromaPort) {
				ispisNaOSW(osw2, "ERROR 43 >> Greška pri spajanju na ServerAerodroma!");
			} else if (port == SUdaljenostiPort) {
				e.printStackTrace();
				ispisNaOSW(osw2, "ERROR 44 >> Greška pri spajanju na ServerAerodroma!");
			} else {
				ispisNaOSW(osw2, "ERROR 49 >> Nepoznat port za spajanje!");
			}
		}
		return "";	
	}
	
	
	/**
	 * Obradi cache naredbu je metoda koja određuje koja će se cache naredba izvršiti
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @return Vraća odgovor na uspješnost izvršavanja koje može biti OK ili prazan String
	 */
	public String obradiCacheNaredbu(OutputStreamWriter osw) {

		String odgovor = "";
		cacheIspravno = true;
		
		if (mCacheBackup.matches()) {
			pisiUSerijalizinuDatoteku(osw);
			odgovor = ispitajIspravnost();
		} else if (mCacheRestore.matches()) {
			ucitajSerijalizinuDatoteku(osw);
			odgovor = ispitajIspravnost();
		} else if (mCacheClear.matches()) {
			odgovor = ocistiMemoriju();
		} else if (mCacheStat.matches()) {
			sortirajMemoriju();
			odgovor = ispisiStatistiku();
			upisiUDatoteku(odgovor, osw);
			if (!cacheIspravno) {
				odgovor = "";
				return odgovor;
			}
			odgovor = citajIzDatoteke(osw);
			izbrisiPrivremenuDatoteku();
			if (!cacheIspravno) {
				odgovor = "";
			}
		}
		return odgovor;
	}
	
	
	/**
	 * Ispitaj ispravnost je pomoćna metoda koja ispituje je li se vrijednost zastavice promijenila
	 *
	 * @return Vraća se OK, ako nema promjene ili prazan string
	 */
	public String ispitajIspravnost() {
		if (cacheIspravno) {
			return "OK";
		} else {
			return "";
		}
	}
	

	/**
	 * Provjeri memoriju je metoda koja provjera postoji li određeni zapis već u memoriji
	 *
	 * @param komanda Sadržaj primljene komande
	 * @return Vraća string odgovora, ako je komanda pronađena u memoriji ili prazan String
	 */
	public String provjeriMemoriju(String komanda) {
		
		for (Memorija m : serverGlavni.cs.memorija) {
			if (m.komanda.compareTo(komanda) == 0) {
				Date date = new Date();
				m.zadnjeVrijeme = isoFormat.format(new Timestamp(date.getTime()));
				m.brojPoziva++;
				return m.odgovor;
			}
		}
		return "";
	}
	
	
	/**
	 * Spremi U memoriju je metoda koja sprema komandu i odgovor u objekt Memorija
	 *
	 * @param komanda Sadržaj primljene komande
	 * @param odgovor Dobiveni odgovor na postavljenu komandu
	 */
	public void spremiUMemoriju(String komanda, String odgovor) {
		
		Date date = new Date();
		String datumVrijeme = isoFormat.format(new Timestamp(date.getTime()));
		Memorija m = new Memorija(komanda, odgovor, 1, datumVrijeme);
		serverGlavni.cs.memorija.add(m);
		return;
	}
	
	
	/**
	 * Sortiraj memoriju je metoda koja sortira memoriju po atributu broj korištenja
	 */
	public void sortirajMemoriju() {
		Collections.sort(serverGlavni.cs.memorija, Collections.reverseOrder());
	}
	
	
	/**
	 * Očisti memoriju je metoda koja briše se iz memorije
	 *
	 * @return the string
	 */
	public String ocistiMemoriju() {
		serverGlavni.cs.memorija.clear();
		return "OK";
	}
	
	
	/**
	 * Ispiši statistiku je metoda koja čita memoriju, formatira zapis, piše i čita iz datoteke te vraća odgovor
	 *
	 * @return Formatirani odgovor pročitan iz datoteke
	 */
	public String ispisiStatistiku() {

		String odgovor = "OK ";
		String odgovor2 = "";
		try {
			odgovor2 += String.format("%-30s%5s%30s", "Predmet komande", "Broj korištenja",
					"Zadnje vrijeme" + "\\n");
			for (Memorija m : serverGlavni.cs.memorija) {
				odgovor2 += stvoriStatIspis(m);
			}
			odgovor += prebrojiZnakove(odgovor2) +";\\n\n";
		} catch (Exception e) {
			odgovor = "ERROR 49 >> Problem sa privremenom datotekom!";
			return odgovor;
		}
		return odgovor + odgovor2;
		
	}
	
	
	/**
	 * Prebroji znakove je metoda koja ispituje koliko je memorija velika
	 *
	 * @param odgovor Sadržaj memorije
	 * @return Vraća se veličina memorije tj. broj znakova koje sadrži
	 */
	public int prebrojiZnakove(String odgovor) {
		
		return odgovor.length();
	}
	
	
	/**
	 * Stvori stat ispis je metoda koja formatira memoriju u ispis
	 *
	 * @param m Element memorije koji se formatira
	 * @return Formatirani String elementa memorije
	 */
	public String stvoriStatIspis(Memorija m) {
		
		String brojPoziva = String.valueOf(m.brojPoziva);
		String linija = "\n";
		linija += String.format("%-30s%15s%30s", m.komanda, brojPoziva, m.zadnjeVrijeme + "\\n");
		return linija;
	}
	
	
	/**
	 * Upiši U datoteku piše rezultat formatiranja međuspremnika u privremenu datoteku
	 *
	 * @param odgovor Formatirana memorija
	 * @param osw Objekt pisača za pisanje odgovora
	 */
	public void upisiUDatoteku(String odgovor, OutputStreamWriter osw) {
		
		privremenaDatoteka = new File(imePrivremeneDatoteke);
		try (FileWriter fw = new FileWriter(privremenaDatoteka);)
		{
			if (!privremenaDatoteka.exists()) {
				privremenaDatoteka.createNewFile();
			}
			fw.write(odgovor);
		} catch (IOException e) {
			ispisNaOSW(osw,"ERROR 49 >> Greška u pisanju u serijaliziranu datoteku!");
			cacheIspravno = false;
		}
	}
	
	
	/**
	 * Čitaj iz datoteke je metoda koja čita formatirane podatke iz datoteke te ih dodatno formatira
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @return Vraća se pročitani i formatirani sadržaj memorije iz datoteke
	 */
	public String citajIzDatoteke(OutputStreamWriter osw) {
		
		String odgovor = "";
		try (Scanner citac = new Scanner(privremenaDatoteka);)
		{
			while (citac.hasNextLine()) {
				String linija = citac.nextLine();
				odgovor += linija + "\n";
			}
		} catch (FileNotFoundException e) {
			ispisNaOSW(osw,"ERROR 49 >> Greška u čitanju datoteke!");
			cacheIspravno = false;
		}
		return odgovor;
	}
	
	
	/**
	 * Izbriši privremenu datoteku je metoda koja briše privremenu datoteku
	 */
	public void izbrisiPrivremenuDatoteku() {
		privremenaDatoteka.delete();
	}
	
	
	/**
	 * Učitaj serijalizinu datoteku je metoda koja učitava serijalizirane podatke
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 */
	public void ucitajSerijalizinuDatoteku(OutputStreamWriter osw){
		
		datoteka = new File(imeDatoteke);
		try (FileInputStream fis = new FileInputStream(datoteka);
				ObjectInputStream ois = new ObjectInputStream(fis);)
		{
			CacheServera cs = (CacheServera) ois.readObject();
			serverGlavni.cs = cs;
		}
		catch (Exception e) {
			ispisNaOSW(osw,"ERROR 49 >> Greška u čitanju serijalizirane datoteke!");
			cacheIspravno = false;
		}
		return;
	}
	
	
	/**
	 * Piši u serijalizinu datoteku je metoda koja serijalizira memoriju u datoteku
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 */
	public void pisiUSerijalizinuDatoteku(OutputStreamWriter osw){
		
		datoteka = new File(imeDatoteke);
		try (FileOutputStream fos = new FileOutputStream(datoteka);
				ObjectOutputStream oos = new ObjectOutputStream(fos);)
		{
			oos.writeObject(serverGlavni.cs);
		}
		catch (Exception e) {
			ispisNaOSW(osw,"ERROR 49 >> Greška u pisanju u serijaliziranu datoteku!");
			cacheIspravno = false;
		}
		return;
	}
	
	
	/**
	 * Ispis na OSW je metoda koja piše odgovor na komandu u OutputStreamWriter
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param odgovor Odgovor na traženu komandu
	 */
	public void ispisNaOSW(OutputStreamWriter osw, String odgovor) {
		
		try {
			osw.write(odgovor);
			osw.flush();
			osw.close();
		} catch (IOException e) {
			System.out.println("ERROR 49 >> Problem sa slanjem odgovora!");
		}
	}
}