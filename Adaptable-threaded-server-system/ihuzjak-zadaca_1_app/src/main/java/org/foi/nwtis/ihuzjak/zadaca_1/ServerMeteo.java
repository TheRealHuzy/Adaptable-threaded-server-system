package org.foi.nwtis.ihuzjak.zadaca_1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.foi.nwtis.ihuzjak.vjezba_03.konfiguracije.Konfiguracija;
import org.foi.nwtis.ihuzjak.vjezba_03.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.ihuzjak.vjezba_03.konfiguracije.NeispravnaKonfiguracija;

// TODO: Auto-generated Javadoc
/**
 * Klasa ServerMeteo koja učitava meteo podatke te odgovara na upite vezane uz njih
 */
public class ServerMeteo {

	/** Port servera ServerMeteo */
	int port;
	
	/** Maskimalni broj čekaća */
	int maksCekaca;
	
	/** Definiran soket */
	Socket veza = null;
	
	/** Lista objekata AerodromMeteo u koju se učitavaju podaci */
	List<AerodromMeteo> aerodromiMeteo = new ArrayList<>();
	
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
	
	/** Regularni izraz za konfiguracijsku datoteku */
	static String argument = "^(\\w+).(txt|bin|json|xml)$";
	
	/** Objekt konfiguracije */
	static public Konfiguracija konfig = null;
	
	/** Format datuma koji sadrži i vrijeme u milisekundama */
	static SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	/** Format datuma koji se prima na obradu */
	static SimpleDateFormat datumFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	/** Format decimalnih brojeva s jednom decimalom */
	private static final DecimalFormat df = new DecimalFormat("0.0");
	
	/** Matcher za meteoIcao */
	Matcher mMeteoIcao;
	
	/** Matcher za meteoIcaoDatum */
	Matcher mMeteoIcaoDatum;
	
	/** Matcher za tempTempTemp */
	Matcher mTempTempTemp;
	
	/** Matcher za tempTempTempDatum */
	Matcher mTempTempTempDatum;

	/**
	 * Main metoda klase ServerMeteo u kojoj se prima i obrađuje zahtjev te se šalje odgovor
	 *
	 * @param args Argumenti koje dobiva main metoda
	 */
	public static void main(String[] args) {
		
		if(args.length != 1 || !ispitajArgument(args[0])) {
			System.out.println("ERROR 10 >> Broj argumenata nije 1 ili je neispravan!");
			return;
		}
		ucitavanjePodataka(args[0]);
		if(konfig == null) {
			System.out.println("ERROR 19 >> Problem s konfiguracijom!");
			return;
		}		
		if(!provjeriKonfig()) {
			System.out.println("ERROR 19 >> Problem s nepotpunim konfiguracijskim podacima!");
			return;
		}
		
		int port = Integer.parseInt(konfig.dajPostavku("port"));
		int maksCekaca = Integer.parseInt(konfig.dajPostavku("maks.cekaca"));
		
		ServerMeteo sm = new ServerMeteo(port, maksCekaca);
		
		if(sm.ispitajPort(port)) {
			return;
		}
		
		String NazivDatotekeMeteoPodataka = konfig.dajPostavku("datoteka.meteo");
		
		if(!sm.pripremiMeteo(NazivDatotekeMeteoPodataka)){
			return;
		}
		
		sm.obradaZahtjeva();
	}
	
	/**
	 * Ispitaj argument metoda ispituje je li unesena zadovoljavajuća konfiguracijska datoteka
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
		if(konfig.dajPostavku("datoteka.meteo") == null) {
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
			System.out.println("ERROR 19 >> Traženi port je vec zauzet!");
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
				AerodromMeteo am;
				try {
					am = new AerodromMeteo(p[0], Double.parseDouble(p[1]), Double.parseDouble(p[3]),
							Double.parseDouble(p[2]), p[4], isoFormat.parse(p[4]).getTime());
					
					aerodromiMeteo.add(am);
				} catch (NumberFormatException | ParseException e) {
					System.out.println("ERROR 19 >> Problem sa učitavanjem podataka datoteke "
							+ nazivDatotekeMeteoPodataka);
					return false;
				}
			}
		} catch (IOException e) {
			System.out.println("ERROR 19 >> Ne postoji datoteka zadana u konfiguraciji s nazivom: "
					+ nazivDatotekeMeteoPodataka);
			return false;
		}
		return true;
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
			System.out.println("ERROR 19 >> Greška pri učitavanju datoteke.");
		}
	}

	/**
	 * Konstruktor klase ServerMeteo
	 *
	 * @param port Port servera ServerMeteo
	 * @param maksCekaca Maksimalni broj čekača
	 */
	public ServerMeteo(int port, int maksCekaca) {
		
		super();
		this.port = port;
		this.maksCekaca = maksCekaca;
	}

	/**
	 * Obrada zahtjeva je metoda koja obrađuje primljeni zahtjev
	 */
	public void obradaZahtjeva() {

		try (ServerSocket ss = new ServerSocket(this.port, this.maksCekaca))
		{
			while (true) {
				this.veza = ss.accept();

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
					
					provjeraSintakseObrade(osw, tekst);
				} catch (SocketException e) {
					System.out.println("ERROR 19 >> Neuspješno stvaranje socketa");
				}
			}

		} catch (IOException ex) {
			System.out.println("ERROR 19 >> Port je već zauzet!");
			return;
		}
	}
	
	/**
	 * Provjera sintakse obrade je metoda koja provjerava sintaksu komande kako bi usmjerila obradu
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param tekst Sadržaj primljene komande
	 */
	private void provjeraSintakseObrade(OutputStreamWriter osw, StringBuilder tekst) {
		
		if(mMeteoIcao.matches()) {
			izvrsiMeteoIcao(osw, tekst.toString());
		} else if (mMeteoIcaoDatum.matches()) {
			izvrsiMeteoIcaoDatum(osw, tekst.toString());
		} else if (mTempTempTemp.matches()){
			izvrsiTempTempTemp(osw, tekst.toString());
		} else if (mTempTempTempDatum.matches()) {
			izvrsiTempTempTempDatum(osw, tekst.toString());
		} else {
			ispisNaOSW(osw, "ERROR 10 >> Sintaksa komande nije uredu");
		}
		return;
	}

	/**
	 * Stvori matchere je metoda koja inicijalizira objekte tipa Matcher
	 *
	 * @param tekst Sadržaj primljene komande
	 */
	private void stvoriMatchere(StringBuilder tekst) {
		
		Pattern pMeteoIcao = Pattern.compile(meteoIcao);
		Pattern pMeteoIcaoDatum = Pattern.compile(meteoIcaoDatum);
		Pattern pTempTempTemp = Pattern.compile(tempTempTemp);
		Pattern pTempTempTempDatum = Pattern.compile(tempTempTempDatum);
		
		mMeteoIcao = pMeteoIcao.matcher(tekst.toString());
		mMeteoIcaoDatum = pMeteoIcaoDatum.matcher(tekst.toString());
		mTempTempTemp = pTempTempTemp.matcher(tekst.toString());
		mTempTempTempDatum = pTempTempTempDatum.matcher(tekst.toString());
	}

	/**
	 * Izvrši meteo icao je metoda koja obrađuje naredbu meteoIcao
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param komanda Sadržaj primljene komande
	 */
	private void izvrsiMeteoIcao(OutputStreamWriter osw, String komanda) {
		
		String[] p = komanda.split(" ");
		String icao = p[1];
		String odgovor = null;
		AerodromMeteo nAerodrom = null;
		
		for(AerodromMeteo am : this.aerodromiMeteo) {
			if(am.getIcao().compareTo(icao) == 0) {
				if (nAerodrom == null) {
					nAerodrom = am;
				} else if (nAerodrom.getTime() < am.getTime()) {
					nAerodrom = am;
				}
			}
		}
		if (nAerodrom == null) {
			ispisNaOSW(osw, "ERROR 11 Nema traženog aerodroma!");
			return;
		}
		
		odgovor = "OK " + df.format(nAerodrom.getTemp()) + " " + nAerodrom.getVlaga() + " "
				+ nAerodrom.getTlak() + " " + nAerodrom.getVrijeme() + ";";
		
		ispisNaOSW(osw, odgovor);
		return;
	}
	
	/**
	 * Izvrši meteo icao je metoda koja obrađuje naredbu meteoIcaoDatum
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param komanda Sadržaj primljene komande
	 */
	private void izvrsiMeteoIcaoDatum(OutputStreamWriter osw, String komanda) {
		
		String[] p = komanda.split(" ");
		String icao = p[1];
		long datum = 0;
		
		try {
			datum = datumFormat.parse(p[2]).getTime();
		} catch (ParseException e1) {
			System.out.println("ERROR 19 >> Neispravan datum!");
		}
		String odgovor = null;
		
		for(AerodromMeteo am : this.aerodromiMeteo) {
			if(am.getIcao().compareTo(icao) == 0 && am.getTime() > datum && am.getTime()
					< (long)(datum + 86400000)) {
				if (odgovor == null) {
					odgovor = "OK";
				}
				 odgovor += " " + df.format(am.getTemp()) + " " + am.getVlaga() +
						 " " + am.getTlak()+ " " + am.getVrijeme() + ";";
			}
		}
		if (odgovor == null) {
			ispisNaOSW(osw, "ERROR 11 Nema traženog aerodroma ili podataka na zadani datum!");
			return;
		}
		ispisNaOSW(osw, odgovor);
		return;
	}
	
	/**
	 * Izvrši temp temp temp je metoda koja obrađuje naredbu tempTempTemp
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param komanda Sadržaj primljene komande
	 */
	private void izvrsiTempTempTemp(OutputStreamWriter osw, String komanda) {
		
		komanda = komanda.replace(',', '.');
		String[] p = komanda.split(" ");
		double temp1 = Double.parseDouble(p[1]);
		double temp2 = Double.parseDouble(p[2]);

		String odgovor = null;
		
		for(AerodromMeteo am : this.aerodromiMeteo) {
			if(am.getTemp() >= temp1 && am.getTemp() <= temp2) {
				if (odgovor == null) {
					odgovor = "OK";
				}
				 odgovor += " " + am.getIcao() + " " + df.format(am.getTemp()) + " " + am.getVlaga() + 
					" " + am.getTlak() + " " + am.getVrijeme() + ";";
			}
		}
		if (odgovor == null) {
			ispisNaOSW(osw, "ERROR 11 Nema temperature u traženom rasponu!");
			return;
		}
		ispisNaOSW(osw, odgovor);
		return;
	}
	
	/**
	 * Izvrši temp temp temp datum je metoda koja obrađuje naredbu tempTempTempDatum
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param komanda Sadržaj primljene komande
	 */
	private void izvrsiTempTempTempDatum(OutputStreamWriter osw, String komanda) {
		
		komanda = komanda.replace(',', '.');
		String[] p = komanda.split(" ");
		double temp1 = Double.parseDouble(p[1]);
		double temp2 = Double.parseDouble(p[2]);
		long datum = 0;
		
		try {
			datum = datumFormat.parse(p[3]).getTime();
		} catch (ParseException e1) {
			ispisNaOSW(osw, "ERROR 19 >> Neispravan datum!");
		}
		String odgovor = null;
		
		for(AerodromMeteo am : this.aerodromiMeteo) {
			if(am.getTemp() > temp1 && am.getTemp() < temp2 && am.getTime() >
					datum && am.getTime() < datum + 86400000) {
				if (odgovor == null) {
					odgovor = "OK";
				}
				 odgovor += " " + am.getIcao() + " " + df.format(am.getTemp()) + " " + am.getVlaga() + 
					" " + am.getTlak() + " " + am.getVrijeme()+ ";";
			}
		}
		if (odgovor == null) {
			ispisNaOSW(osw, "ERROR 11 Nema temperature u traženom rasponu ili tog datuma!");
			return;
		}
		saljiOdgovor(osw, odgovor);
		return;	
	}
	
	/**
	 * Ispis na OSW je metoda koja piše odgovor na komandu u OutputStreamWriter u slučaju greške
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param odgovor Odgovor na traženu komandu ukoliko je došlo do greške
	 */
	private void ispisNaOSW(OutputStreamWriter osw, String odgovor) {
		try {
			osw.write(odgovor);
			osw.flush();
			osw.close();
			this.veza.close();
		} catch (IOException e) {
			System.out.println("ERROR 19 >> Problem sa slanjem odgovora ili zatvaranjem mrežne utičnice!");
		}
	}
	
	/**
	 * Šalji odgovor je metoda koja piše odgovor na komandu u OutputStreamWriter
	 *
	 * @param osw Objekt pisača za pisanje odgovora
	 * @param odgovor Odgovor na traženu komandu
	 */
	private void saljiOdgovor(OutputStreamWriter osw, String odgovor) {
		try {
			osw.write(odgovor);
			osw.flush();
			osw.close();
			this.veza.close();
		} catch (IOException e) {
			System.out.println("ERROR 19 >> Problem sa slanjem odgovora ili zatvaranjem mrežne utičnice!");
		}
	}
}
