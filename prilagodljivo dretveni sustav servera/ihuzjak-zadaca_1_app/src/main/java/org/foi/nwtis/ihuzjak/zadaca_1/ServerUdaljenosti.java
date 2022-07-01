package org.foi.nwtis.ihuzjak.zadaca_1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.foi.nwtis.ihuzjak.vjezba_03.konfiguracije.Konfiguracija;
import org.foi.nwtis.ihuzjak.vjezba_03.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.ihuzjak.vjezba_03.konfiguracije.NeispravnaKonfiguracija;

/**
 * Klasa ServerUdaljenosti koja izačunava ili pronalazi u spremniku udaljenost te je šalje kao odgovor na zahtjev
 */
public class ServerUdaljenosti {

	/** Port servera ServerUdaljenost */
	int port;
	
	/** Maksimalni broj čekača */
	int maksCekaca;
	
	/** Adresa servera ServerAerodroma */
	String SAerodromaAdresa;
	
	/** Port servera ServerAerodroma */
	int SAerodromaPort;
	
	/** Maksimalno čekanje na odgovor */
	int maksCekanje;
	
	/** Lista objekata Aerodrom koji se spremaju ukoliko već ne postoje u listi */
	List<Aerodrom> aerodromi = new ArrayList<>();
	
	/** Regularni izraz za komandu distanceIcaoIcao */
	String distanceIcaoIcao = "^DISTANCE ([A-Z]{4}) ([A-Z]{4})$";
	
	/** Regularni izraz za komandu distanceClear */
	String distanceClear = "^DISTANCE CLEAR$";
	
	/** Regularni izraz za konfiguracijsku datoteku */
	static String argument = "^(\\w+).(txt|bin|json|xml)$";
	
	/** Pomoćna varijabla za stvaranje odgovora prema ServeruAerodroma */
	String odgovorAero = "";
	
	/** Format decilanog broja bez decimalnih mjesta */
	private static final DecimalFormat df = new DecimalFormat("0");
	
	/** Objekt konfiguracije */
	static public Konfiguracija konfig = null;

	/**
	 * Main je glavna metoda klase ServerUdaljenosti koja prima i obrađuje zahtjev te šalje odgovor
	 *
	 * @param args Argumenti koje dobiva main metoda
	 */
	public static void main(String[] args) {
		
		if(args.length != 1 || !ispitajArgument(args[0])) {
			System.out.println("ERROR 30 >> Broj argumenata nije 1 ili je neispravan!");
			return;
		}
		ucitavanjePodataka(args[0]);
		if(konfig == null) {
			System.out.println("ERROR 39 >> Problem s konfiguracijom.");
			return;
		}
		if(!provjeriKonfig()) {
			System.out.println("ERROR 39 >> Problem s nepotpunim konfiguracijskim podacima!");
			return;
		}
		
		int port = Integer.parseInt(konfig.dajPostavku("port"));
		int maksCekaca = Integer.parseInt(konfig.dajPostavku("maks.cekaca"));
		int sUdaljenostiPort = Integer.parseInt(konfig.dajPostavku("server.aerodroma.port"));
		String sUdaljenostiAdresa = konfig.dajPostavku("server.aerodroma.adresa");
		int maksCekanje = Integer.parseInt(konfig.dajPostavku("maks.cekanje"));
				
		ServerUdaljenosti sm = new ServerUdaljenosti(port, maksCekaca, sUdaljenostiAdresa,
				sUdaljenostiPort, maksCekanje);
		
		if(sm.ispitajPort(port)) {
			return;
		}
				
		sm.obradaZahtjeva();
	}

	/**
	 * Ispitaj Argument metoda ispituje je li unesena zadovoljavajuća konfiguracijska datoteka
	 *
	 * @param arg Argument koji je dobiven kao unos
	 * @return true Vraća se istina, ako je argument zadovoljavajuć
	 */
	private static boolean ispitajArgument(String arg) {
		
		Pattern pArgument = Pattern.compile(argument);
		Matcher mArgument = pArgument.matcher(arg.toString());
		if (mArgument.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * Konstruktor klase ServerUdaljenosti.
	 *
	 * @param port Port servera ServerUdaljenosti
	 * @param maksCekaca Maksimalni broj čekača
	 * @param sUdaljenostiAdresa Adresa servera ServerAerodroma
	 * @param sAerodromaPort Port servera ServerAerodroma
	 * @param maksCekanje Maksimalno čekanje na odgovor
	 */
	public ServerUdaljenosti(int port, int maksCekaca, String sUdaljenostiAdresa,
			int sAerodromaPort, int maksCekanje) {
		super();
		this.port = port;
		this.maksCekaca = maksCekaca;
		SAerodromaAdresa = sUdaljenostiAdresa;
		SAerodromaPort = sAerodromaPort;
		this.maksCekanje = maksCekanje;
	}
	
	/**
	 * Učitavanje podataka je metoda koja stvara objekt konfiguracije
	 *
	 * @param nazivDatoteke Vrijednost naziva datoteke koji je potreban za stvaranje konfiguracije
	 */
	private static void ucitavanjePodataka(String nazivDatoteke) {
		
		try {
			konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
		} catch (NeispravnaKonfiguracija e) {
			System.out.println("ERROR 39 >> Greška pri učitavanju konfiguracijske datoteke!");
		}
	}
	
	/**
	 * Provjeri konfig metoda provjera postoje li u konfiguracijskoj datoteci svi potrebni elementi
	 *
	 * @return true Vraća se istina, ako svi konfiguracijski elementi postoje
	 */
	private static boolean provjeriKonfig() {
		
		if(konfig.dajPostavku("port") == null) {
			return false;
		}
		if(konfig.dajPostavku("maks.cekaca") == null) {
			return false;
		}
		if(konfig.dajPostavku("server.aerodroma.port") == null) {
			return false;
		}
		if(konfig.dajPostavku("server.aerodroma.adresa") == null) {
			return false;
		}
		if(konfig.dajPostavku("maks.cekanje") == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * Ispitaj port je metoda koja ispituje je li port slobodan
	 *
	 * @param port Vrijednost porta koji se ispituje
	 * @return true Vraća se istina, ako je port zauzet
	 */
	private boolean ispitajPort(int port) {
		
		try {
			ServerSocket s = new ServerSocket(port);
			s.close();
		} catch (IOException e) {
			System.out.println("ERROR 39 >> Traženi port je vec zauzet!");
			return true;
		}
		return false;
	}

	/**
	 * Obrada zahtjeva je metoda koja obrađuje primljeni zahtjev na način da stvara dretvu
	 */
	public void obradaZahtjeva() {
		
		try (ServerSocket ss = new ServerSocket(this.port, this.maksCekaca))
		{
			while (true) {
				DretvaUdaljenosti dretvaObrade = new DretvaUdaljenosti(ss.accept());
				dretvaObrade.start();
			}
		} catch (IOException ex) {
			System.out.println("ERROR 39 >> Port je već zauzet!");
			return;
		}

	}
	
	/**
	 * Privatna klasa DretvaUdaljenosti koja obrađuje zahtjev
	 */
	private class DretvaUdaljenosti extends Thread {
		
		/** Inicijalizacija soketa */
		private Socket veza = null;
		
		/**
		 * Konstruktor klase DretvaUdaljenosti
		 *
		 * @param veza Objekt socketa
		 */
		public DretvaUdaljenosti(Socket veza) {
			super();
			this.veza = veza;
		}
		
		/**
		 * Run je glavna metoda dretve koja obrađuje zahtjev
		 */
		@Override
		public void run() {
			
			try (InputStreamReader isr = new InputStreamReader(this.veza.getInputStream(),
					Charset.forName("UTF-8"));
					OutputStreamWriter osw = new OutputStreamWriter(this.veza.getOutputStream(),
					Charset.forName("UTF-8"));) 
			{
				StringBuilder tekst = new StringBuilder();
				while (true) {
					int i = isr.read();
					if (i == -1) {
						break;
					}
					tekst.append((char) i);
				}
				this.veza.shutdownInput();
				
				Pattern pDistanceIcaoIcao = Pattern.compile(distanceIcaoIcao);
				Pattern pDistanceClear = Pattern.compile(distanceClear);
				
				Matcher mDistanceIcaoIcao = pDistanceIcaoIcao.matcher(tekst.toString());
				Matcher mDistanceClear = pDistanceClear.matcher(tekst.toString());
				
				if(mDistanceIcaoIcao.matches()) {
					izvrsiDistanceIcaoIcao(osw, tekst.toString());
				} else if (mDistanceClear.matches()) {
					izvrsiDistanceClear(osw, tekst.toString());
				} else {
					ispisNaOSW(osw, "ERROR 30 >> Sintaksa komande nije uredu!");
				}
			} catch (IOException e) {
				System.out.println("ERROR 39 >> Problem u stvaranju čitača i pisača za komunikaciju!");
			}
		}
	}

	/**
	 * Izvrsi distance icao icao je metoda koja obrađuje naredbu distanceIcaoIcao
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param komanda Sadržaj primljene komande
	 */
	private void izvrsiDistanceIcaoIcao(OutputStreamWriter osw, String komanda) {
		
		String[] p = komanda.split(" ");
		String icao = p[1];
		String icao2 = p[2];
		
		Aerodrom a1 = null;
		Aerodrom a2 = null;
		String udaljenost = null;
		String odgovor = null;
		
		for(Aerodrom a : aerodromi) {
			if(a.getIcao().compareTo(icao) == 0) {
				a1 = a;
				break;
			}
		}
		for(Aerodrom a : aerodromi) {
			if(a.getIcao().compareTo(icao2) == 0) {
				a2 = a;
				break;
			}
		}
		if(a1 == null) {
			a1 = dajAerodrom(icao);
			if (odgovorAero.compareTo("") != 0) {
				ispisNaOSW(osw, odgovorAero);
				return;
			}
		}
		if(a2 == null) {
			a2 = dajAerodrom(icao2);
			if (odgovorAero.compareTo("") != 0) {
				ispisNaOSW(osw, odgovorAero);
				return;
			}
		}
		if(a1 == null) {
			ispisNaOSW(osw, "ERROR 31 >> Nema prvog traženog aerodroma!");
			return;
		} else {
			aerodromi.add(a1);
		}
		if(a2 == null) {
			ispisNaOSW(osw, "ERROR 31 >> Nema drugog traženog aerodroma!");
			return;
		} else {
			aerodromi.add(a2);
		}
		udaljenost = izracunajUdaljenost(a1, a2);
		odgovor = "OK " + udaljenost;
		
		ispisNaOSW(osw, odgovor);
	}
	
	/**
	 * Daj aerodrom je metoda koja se poziva kada ServerUdaljenosti nema podatke za određeni aerodrom
	 *
	 * @param icao Ime odnosno Icao aerodroma koji se šalje na dohvat
	 * @return aerodrom Dobiveni podaci o aerodromu koji se odmah pretvaraju u objekt
	 */
	private Aerodrom dajAerodrom(String icao) {
		
		odgovorAero = "";
		String komanda = "AIRPORT " + icao;
		Aerodrom a = null;
		
		InetSocketAddress isa = new InetSocketAddress(SAerodromaAdresa, SAerodromaPort);
		try (Socket veza = new Socket())
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
			if (tekst.toString().startsWith("ERROR")) {
				return null;
			}
			a = kreirajAerodrom(tekst.toString());
			return a;
		} catch (IOException e) {
			odgovorAero = "ERROR 32 >> Neuspješno spajanje na ServerAerodroma!";
		} 
		return a;
	}
	
	/**
	 * Kreiraj aerodrom je metoda koja od dobivenog odgovora stvara objekt aerodroma
	 *
	 * @param komanda Sadržaj primljene komande
	 * @return aerodrom Objekt aerodroma koji se sastavio od elemenata primljene komande
	 */
	private Aerodrom kreirajAerodrom(String komanda) {
		
		String p[] = komanda.split(" ");
		String icao = p[1];
		
		int indeks = komanda.indexOf('\"');
		int indeks2 = komanda.indexOf('\"', indeks + 1);
		
		String naziv = komanda.substring(indeks + 1, indeks2);
		String ostatak = komanda.substring(indeks2 + 2, komanda.length());
		
		String o[] = ostatak.split(" ");
		String GpsGS = o[0];
		String GpsGD = o[1];
		return new Aerodrom(icao, naziv, GpsGS, GpsGD);
	}

	/**
	 * Izracunaj udaljenost je metoda koja računa udaljenost između dvije točke na zemlji
	 *
	 * @param a1 Objekt prvog aerodroma koji sadrži koordinate
	 * @param a2 Objekt drugog aerodroma koji sadrži koordinate
	 * @return Izračunata udaljenost između dva aerodroma
	 */
	private String izracunajUdaljenost(Aerodrom a1, Aerodrom a2) {
		
		double a1GD = Double.parseDouble(a1.getGpsGD());
		double a1GS = Double.parseDouble(a1.getGpsGS());
		double a2GD = Double.parseDouble(a2.getGpsGD());
		double a2GS = Double.parseDouble(a2.getGpsGS());
		
		double udaljenostD = Math.toRadians(a2GD - a1GD);
		double udaljenostS = Math.toRadians(a2GS - a1GS);
		
		double izracun = Math.pow(Math.sin(udaljenostS / 2), 2)
				+ Math.cos(Math.toRadians(a1GS)) * Math.cos(Math.toRadians(a2GS))
				* Math.pow(Math.sin(udaljenostD / 2), 2);
		
		double udaljenost = (2 * Math.atan2(Math.sqrt(izracun), Math.sqrt(1 - izracun))) * 6371;
		return df.format(udaljenost);
	}

	/**
	 * Izvrsi distance clear je metoda koja obrađuje naredbu distanceClear
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param komanda Sadržaj primljene komande
	 */
	private void izvrsiDistanceClear(OutputStreamWriter osw, String komanda) {
		aerodromi.clear();
		ispisNaOSW(osw, "OK");
		return;
	}
	
	/**
	 * Ispis na OSW je metoda koja piše odgovor na komandu u OutputStreamWriter
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param odgovor Odgovor na traženu komandu
	 */
	private void ispisNaOSW(OutputStreamWriter osw, String odgovor) {
		try {
			osw.write(odgovor);
			osw.flush();
			osw.close();
		} catch (IOException e) {
			System.out.println("ERROR 39 >> Problem sa slanjem odgovora!");
		}
	}
}
