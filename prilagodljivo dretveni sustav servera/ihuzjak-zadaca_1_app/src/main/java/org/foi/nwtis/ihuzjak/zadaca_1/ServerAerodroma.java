package org.foi.nwtis.ihuzjak.zadaca_1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.foi.nwtis.ihuzjak.vjezba_03.konfiguracije.Konfiguracija;
import org.foi.nwtis.ihuzjak.vjezba_03.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.ihuzjak.vjezba_03.konfiguracije.NeispravnaKonfiguracija;

// TODO: Auto-generated Javadoc
/**
 * Klasa ServerAerodroma koja prima i obrađuje zahtjeve te šalje odgovor na njih
 */
public class ServerAerodroma {

	/** Port servera ServerAerodroma */
	int port;
	
	/** Maksimalni broj čekača */
	int maksCekaca;
	
	/** Adresa servera ServerUdaljenosti */
	String SUdaljenostiAdresa;
	
	/** Port servera ServerUdaljenosti */
	int SUdaljenostiPort;
	
	/** Maksimalno čekanje na odgovor */
	int maksCekanje;
	
	/** Inicijalizacija soketa */
	Socket veza = null;
	
	/** Lista objekata Aerodrom koji se učitavaju prilikom pokretanja servera */
	List<Aerodrom> aerodromi = new ArrayList<>();
	
	/** Regularni izraz za komandu airport */
	String airport = "^AIRPORT$";
	
	/** Regularni izraz za komandu airportIcao */
	String airportIcao = "^AIRPORT ([A-Z]{4})$";
	
	/** Regularni izraz za komandu airporIcaoBroj */
	String airportIcaoBroj = "^AIRPORT [A-Z]{4} [+-]?[0-9]+$";
	
	/** Regularni izraz za konfiguracijsku datoteku */
	static String argument = "^(\\w+).(txt|bin|json|xml)$";
	
	/** Objekt konfiguracije */
	static public Konfiguracija konfig = null;
	
	/** Matcher za airport */
	Matcher mAirport;
	
	/** Matcher za airportIcao */
	Matcher mAirportIcao;
	
	/** Matcher za airportIcaoBroj */
	Matcher mAirportIcaoBroj;

	/** Pomoćna varijabla za stvaranje odgovora */
	String odgovorUdalj = "";
	
	/**
	 * Main je glavna metoda klase ServerAerodroma koja prima i obrađuje zahtjev te šalje odgovor
	 *
	 * @param args Argumenti koje dobiva main metoda
	 */
	public static void main(String[] args) {
		
		if(args.length != 1 || !ispitajArgument(args[0])) {
			System.out.println("ERROR 20 >> Broj argumenata nije 1 ili je neispravan!");
			return;
		}
		ucitavanjePodataka(args[0]);
		if(konfig == null) {
			System.out.println("ERROR 29 >> Problem s konfiguracijom.");
			return;
		}		
		if(!provjeriKonfig()) {
			System.out.println("ERROR 29 >> Problem s nepotpunim konfiguracijskim podacima!");
			return;
		}
		
		int port = Integer.parseInt(konfig.dajPostavku("port"));
		int maksCekaca = Integer.parseInt(konfig.dajPostavku("maks.cekaca"));
		int sUdaljenostiPort = Integer.parseInt(konfig.dajPostavku("server.udaljenosti.port"));
		String sUdaljenostiAdresa = konfig.dajPostavku("server.udaljenosti.adresa");
		int maksCekanje = Integer.parseInt(konfig.dajPostavku("maks.cekanje"));
				
		ServerAerodroma sm = new ServerAerodroma(port, maksCekaca, sUdaljenostiAdresa,
				sUdaljenostiPort, maksCekanje);
		
		if(sm.ispitajPort(port)) {
			return;
		}
		
		String NazivDatotekeMeteoPodataka = konfig.dajPostavku("datoteka.aerodroma");
		
		if(!sm.pripremiMeteo(NazivDatotekeMeteoPodataka)){
			return;
		};
		
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
	 * Konstruktor klase ServerAerodroma.
	 *
	 * @param port Port servera ServerAerodroma
	 * @param maksCekaca Maksimalni broj čekača
	 * @param sUdaljenostiAdresa Adresa servera ServerUdaljenosti
	 * @param sUdaljenostiPort Port servera ServerUdaljenosti
	 * @param maksCekanje the Maksimalno čekanje na odgovor
	 */
	public ServerAerodroma(int port, int maksCekaca, String sUdaljenostiAdresa,
			int sUdaljenostiPort, int maksCekanje) {
		
		super();
		this.port = port;
		this.maksCekaca = maksCekaca;
		SUdaljenostiAdresa = sUdaljenostiAdresa;
		SUdaljenostiPort = sUdaljenostiPort;
		this.maksCekanje = maksCekanje;
	}
	
	/**
	 * Ucitavanje podataka je metoda koja stvara objekt konfiguracije
	 *
	 * @param nazivDatoteke Vrijednost naziva datoteke koji je potreban za stvaranje konfiguracije
	 */
	private static void ucitavanjePodataka(String nazivDatoteke) {
		
		try {
			konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
		} catch (NeispravnaKonfiguracija e) {
			System.out.println("ERROR 29 >> Greška pri učitavanju datoteke!");
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
		if(konfig.dajPostavku("server.udaljenosti.port") == null) {
			return false;
		}
		if(konfig.dajPostavku("server.udaljenosti.adresa") == null) {
			return false;
		}
		if(konfig.dajPostavku("datoteka.aerodroma") == null) {
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
			System.out.println("ERROR 29 >> Traženi port je vec zauzet!");
			return true;
		}
		return false;
	}

	/**
	 * Pripremi meteo je metoda koja učitava meteo podatke
	 *
	 * @param nazivDatotekeMeteoPodataka Varijabla koja sadrži ime datoteke iz koje se čitaju podaci
	 * @return true Vraća se istina, ako je čitanje bilo uspješno
	 */
	private boolean pripremiMeteo(String nazivDatotekeMeteoPodataka) {
		
		try (BufferedReader br = new BufferedReader(new FileReader(nazivDatotekeMeteoPodataka, 
					Charset.forName("UTF-8"))))
		{
			while (true) {
				String linija = br.readLine();
				if (linija == null || linija.isEmpty()) {
					break;
				}
				String[] p = linija.split(";");
				Aerodrom a = null;
				try {
					a = new Aerodrom(p[0], p[1], p[2], p[3]);
					aerodromi.add(a);
				} catch (NumberFormatException e) {
					System.out.println("ERROR 29 >> Problem sa podacima aerodroma!");
					return false;
				}
			}
		} catch (IOException e) {
			System.out.println("ERROR 29 >> Ne postoji datoteka zadana u konfiguraciji s nazivom: "
					+ nazivDatotekeMeteoPodataka);
			return false;
		}
		return true;
	}

	/**
	 * Obrada zahtjeva je metoda koja obrađuje primljeni zahtjev na način da stvara dretvu
	 */
	public void obradaZahtjeva() {

		try (ServerSocket ss = new ServerSocket(this.port, this.maksCekaca))
		{
			while (true) {
				DretvaAerodroma dretvaObrade = new DretvaAerodroma(ss.accept());
				dretvaObrade.start();
			}
		} catch (IOException ex) {
			System.out.println("ERROR 29 >> Port je već zauzet!");
			return;
		}
	}
	
	/**
	 * Privatna klasa DretvaAerodroma koja obrađuje zahtjev
	 */
	private class DretvaAerodroma extends Thread {
		
		/** Inicijalizacija soketa */
		private Socket veza = null;
		
		/**
		 * Konstruktor klase DretvaAerodroma.
		 *
		 * @param Objekt socketa
		 */
		public DretvaAerodroma(Socket veza) {
			super();
			this.veza = veza;
		}
		
		/**
		 * Run je glavna metoda dretve koja obrađuje zahtjev
		 */
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
				
				stvoriMatchere(tekst);
				
				if(mAirport.matches()) {
					izvrsiAirport(osw, tekst.toString());
				} else if (mAirportIcao.matches()) {
					izvrsiAirportIcao(osw, tekst.toString());
				} else if (mAirportIcaoBroj.matches()){
					izvrsiAirportIcaoBroj(osw, tekst.toString());
				} else {
					ispisNaOSW(osw, "ERROR 20 Sintaksa komande nije uredu!");
				}
			} catch (IOException e) {
				System.out.println("ERROR 29 >> Problem u stvaranju čitača i pisača za komunikaciju!");
			}
		}
	}
	
	/**
	 * Stvori matchere je metoda koja inicijalizira objekte tipa Matcher
	 *
	 * @param tekst Sadržaj primljene komande
	 */
	private void stvoriMatchere(StringBuilder tekst) {
		
		Pattern pAirport = Pattern.compile(airport);
		Pattern pAirportIcao = Pattern.compile(airportIcao);
		Pattern pAirportIcaoBroj = Pattern.compile(airportIcaoBroj);
		
		mAirport = pAirport.matcher(tekst.toString());
		mAirportIcao = pAirportIcao.matcher(tekst.toString());
		mAirportIcaoBroj = pAirportIcaoBroj.matcher(tekst.toString());
	}	

	/**
	 * Izvrsi airport je metoda koja obrađuje naredbu airport
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param komanda Sadržaj primljene komande
	 */
	private void izvrsiAirport(OutputStreamWriter osw, String komanda) {
		
		String odgovor = "OK";
		for(Aerodrom a : aerodromi) {
			odgovor += " " + a.getIcao() + ";";
		}
		if (odgovor == "OK") {
			ispisNaOSW(osw, "ERROR 21 >> Nema traženog aerodroma!");
			return;
		}
		ispisNaOSW(osw, odgovor);
		return;
	}
	
	/**
	 * Izvrsi airport icao je metoda koja obrađuje naredbu airportIcao
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param Sadržaj primljene komande
	 */
	private void izvrsiAirportIcao(OutputStreamWriter osw, String komanda) {
		
		String[] p = komanda.split(" ");
		String icao = p[1];
		String odgovor = null;
		
		for(Aerodrom a : aerodromi) {
			if(a.getIcao().compareTo(icao) == 0) {
				if (odgovor == null) {
					odgovor = "OK";
				}
				odgovor += " " + a.getIcao() + " \"" + a.getNaziv() + "\" " + a.getGpsGS() + " " + a.getGpsGD();
			}
		}
		if (odgovor == null) {
			ispisNaOSW(osw, "ERROR 21 >> Nema traženog aerodroma!");
			return;
		}
		ispisNaOSW(osw, odgovor);
		return;
		
	}
	
	/**
	 * Izvrsi airport icao broj je metoda koja obrađuje naredbu airportIcaoBroj
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param Sadržaj primljene komande
	 */
	private void izvrsiAirportIcaoBroj(OutputStreamWriter osw, String komanda) {
		
		String[] p = komanda.split(" ");
		String icao = p[1];
		int broj = Integer.parseInt(p[2]);
		String odgovor = null;
		
		for(Aerodrom a : aerodromi) {
			if(a.getIcao().compareTo(icao) == 0) {
				odgovor = "OK";
			}
		}
		if (odgovor != "OK") {
			ispisNaOSW(osw, "ERROR 21 >> Nema traženog aerodroma!");
			return;
		}
		
		for(Aerodrom a : aerodromi) {
			String odgServera = ispitajUdaljenost(icao, a.getIcao());
			if (odgovorUdalj.compareTo("") != 0) {
				ispisNaOSW(osw, odgovorUdalj);
				return;
			}
			String udaljenost = odgServera.split(" ")[1];
			if (Integer.parseInt(udaljenost) <= broj && Integer.parseInt(udaljenost) != 0) {
				odgovor += " " + a.getIcao() + " " + udaljenost + ";";
			}
		}
		ispisNaOSW(osw, odgovor);
		return;
	}

	/**
	 * Ispitaj udaljenost je metoda koja šalje zahtjev na server ServerUdaljenosti
	 *
	 * @param icao Icao String vrijednost prvog aerodroma
	 * @param icao2 Icao String vrijednost drugog aerodroma
	 * @return Odgovor servera na traženi upit
	 */
	private String ispitajUdaljenost(String icao, String icao2) {
		
		odgovorUdalj = "";
		String komanda = "DISTANCE " + icao + " " + icao2;
		
		InetSocketAddress isa = new InetSocketAddress(SUdaljenostiAdresa, SUdaljenostiPort);
		try (Socket veza = new Socket())
		{
			veza.connect(isa, maksCekanje);
			InputStreamReader isr = new InputStreamReader(veza.getInputStream(),Charset.forName("UTF-8"));
			OutputStreamWriter osw = new OutputStreamWriter(veza.getOutputStream(),Charset.forName("UTF-8"));
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
			odgovorUdalj = "ERROR 22 >> Neuspješno spajanje na ServerUdaljenosti!";
		} 
		return "";
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
			System.out.println("ERROR 29 >> Problem sa slanjem odgovora!");
		}
	}
}
