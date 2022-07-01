package org.foi.nwtis.ihuzjak.zadaca_1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
 * The Class ServerGlavni.
 */
public class ServerGlavni {

	/** Port servera ServerGlavni */
	int port;
	
	/** Maksimalni broj čekača */
	int maksCekaca;
	
	/** Definiran soket */
	Socket veza = null;
	
	/** Broj trenutno aktivnih dretvi */
	int aktivneDretve = 0;
	
	/** Lista objekata Korisnik u koju se učitavaju podaci */
	List<Korisnik> korisnici = new ArrayList<>();
	
	/** Lista koja sadrži reference na sve aktivne dretve zahtjeva */
	List<DretvaZahtjeva> dretve = new ArrayList<DretvaZahtjeva>();
	
	/** Referenca za dretvu koja uzrokuje jednodretveni način rada */
	DretvaZahtjeva prekidajucaDretva;
	
	/** Memorija servera */
	CacheServera cs = new CacheServera();
	
	/** Zastavica o jednodretvenosti */
	boolean jednoDretveni = false;
	
	/** Regularni izraz za konfiguracijsku datoteku */
	static String argument = "^(\\w+).(txt|bin|json|xml)$";
	
	/** Objekt konfiguracije */
	static public Konfiguracija konfig = null;

	/**
	 * Main metoda klase ServerMeteo u kojoj se prima i prosljeđuje zahtjev dretvi te se šalje odgovor
	 *
	 * @param args Argumenti koje dobiva main metoda
	 */
	public static void main(String[] args) {
		
		if(args.length != 1 || !ispitajArgument(args[0])) {
			System.out.println("ERROR 40 >> Broj argumenata nije 1 ili je neispravan!");
			return;
		}
		ucitavanjePodataka(args[0]);
		if(konfig == null) {
			System.out.println("ERROR 49 >> Problem s konfiguracijom.");
			return;
		}		
		if(!provjeriKonfig()) {
			System.out.println("ERROR 49 >> Problem s nepotpunim konfiguracijskim podacima!");
			return;
		}
		
		int port = Integer.parseInt(konfig.dajPostavku("port"));
		int maksCekaca = Integer.parseInt(konfig.dajPostavku("maks.cekaca"));
		String NazivDatotekeMeteoKorisnika = konfig.dajPostavku("datoteka.korisnika");
		
		ServerGlavni sm = new ServerGlavni(port, maksCekaca);
		
		if(sm.ispitajPort(port)) {
			return;
		}
		if(!sm.pripremiKorisnici(NazivDatotekeMeteoKorisnika)){
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
	public static boolean ispitajArgument(String arg) {
		
		Pattern pArgument = Pattern.compile(argument);
		Matcher mArgument = pArgument.matcher(arg.toString());
		if (mArgument.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * Pripremi korisnici je metoda koja učitava podatke o korisnicima
	 *
	 * @param NazivDatotekeKorisnika Varijabla koja sadrži ime datoteke iz koje se čitaju podaci
	 * @return true Vraća se istina, ako je čitanje bilo uspješno
	 */
	public boolean pripremiKorisnici(String NazivDatotekeKorisnika) {
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(NazivDatotekeKorisnika, Charset.forName("UTF-8")));
			while (true) {
				String linija = br.readLine();
				if (linija == null || linija.isEmpty()) {
					break;
				}
				String[] p = linija.split(";");
				Korisnik k;
				k = new Korisnik(p[0], p[1], (p[2]), p[3]);
				korisnici.add(k);
			}
			br.close();
		} catch (IOException e) {
			System.out.println("ERROR 49 >> Problem sa podacima korisnika!");
			return false;
		}
		return true;
	}

	/**
	 * Učitavanje podataka je metoda koja stvara objekt konfiguracije
	 *
	 * @param nazivDatoteke Vrijednost naziva datoteke koji je potreban za stvaranje konfiguracije
	 */
	public static void ucitavanjePodataka(String nazivDatoteke) {
		
		try {
			konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
		} catch (NeispravnaKonfiguracija e) {
			System.out.println("ERROR 49 >> Greška pri učitavanju datoteke.");
		}
	}
	
	/**
	 * Provjeri konfig metoda provjera postoje li u konfiguracijskoj datoteci svi potrebni elementi
	 *
	 * @return true Vraća se istina, ako svi konfiguracijski elementi postoje
	 */
	public static boolean provjeriKonfig() {
		
		if(konfig.dajPostavku("port") == null) {
			return false;
		}
		if(konfig.dajPostavku("maks.cekaca") == null) {
			return false;
		}
		if(konfig.dajPostavku("datoteka.korisnika") == null) {
			return false;
		}
		if(konfig.dajPostavku("maks.cekanje") == null) {
			return false;
		}
		if(konfig.dajPostavku("server.aerodroma.adresa") == null) {
			return false;
		}
		if(konfig.dajPostavku("server.aerodroma.port") == null) {
			return false;
		}
		if(konfig.dajPostavku("server.meteo.adresa") == null) {
			return false;
		}
		if(konfig.dajPostavku("server.meteo.port") == null) {
			return false;
		}
		if(konfig.dajPostavku("server.udaljenosti.adresa") == null) {
			return false;
		}
		if(konfig.dajPostavku("server.udaljenosti.port") == null) {
			return false;
		}
		if(konfig.dajPostavku("datoteka.meduspremnika") == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * Konstruktor klase ServerGlavni
	 *
	 * @param port Port servera ServerGlavni
	 * @param maksCekaca Maksimalni broj čekača
	 */
	public ServerGlavni(int port, int maksCekaca) {
		super();
		this.port = port;
		this.maksCekaca = maksCekaca;
	}
	
	/**
	 * Ispitaj port je metoda koja ispituje je li port slobodan
	 *
	 * @param port Vrijednost porta koji se ispituje
	 * @return true Vraća se istina, ako je port zauzet
	 */
	public boolean ispitajPort(int port) {
		
		try {
			ServerSocket s = new ServerSocket(port);
			s.close();
		} catch (IOException e) {
			System.out.println("ERROR 49 >> Traženi port je vec zauzet!");
			return true;
		}
		return false;
	}

	/**
	 * Obrada zahtjeva je metoda koja obrađuje primljeni zahtjev na način da stvara dretve
	 */
	public synchronized void obradaZahtjeva() {
		try (ServerSocket ss = new ServerSocket(this.port, this.maksCekaca))
		{
			while (true) {
				if (jednoDretveni) {
					for (DretvaZahtjeva dz : dretve) {
						if (dz != prekidajucaDretva) {
							try {
								dz.join();
							} catch (InterruptedException e) {
								System.out.println("ERROR 49 >> Čekanje dretvi je prekinuto!");
							}
						}
					}
					synchronized (prekidajucaDretva) {
						prekidajucaDretva.notify();
					}
					try {
						wait();
					} catch (InterruptedException e1) {
						System.out.println("ERROR 49 >> Čekanje dretve u jednodretvenom načinu"
								+ " rada je prekinuto!");
					}
				}
				this.veza = ss.accept();
				
				DretvaZahtjeva dretvaZahtjeva = new DretvaZahtjeva(this, veza, konfig, aktivneDretve);
				dretve.add(dretvaZahtjeva);
				dretvaZahtjeva.start();
				aktivneDretve++;
				
				try {
					wait();
				} catch (InterruptedException e1) {
					System.out.println("ERROR 49 >> Čekanje dretve u višedretvenom načinu"
							+ " rada je prekinuto!");
				}
			}
		} catch (IOException ex) {
			System.out.println("ERROR 49 >> Port je već zauzet!");
		}
	}
}
