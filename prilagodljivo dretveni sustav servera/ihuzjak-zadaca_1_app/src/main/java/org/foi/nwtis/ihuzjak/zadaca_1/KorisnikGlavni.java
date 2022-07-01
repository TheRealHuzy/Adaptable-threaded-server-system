package org.foi.nwtis.ihuzjak.zadaca_1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * Klasa KorisnikGlavni koja stvara upite, obrađuje ih i šalje na ServerGlavni
 */
public class KorisnikGlavni {

	/** Komanda koja se šalje na serverGlavni */
	static String komanda = "";
	
	/** Početna adresa servera koja se prepisuje kod učitavanja komande */
	static String adresaServera = "localhost";
	
	/** Početni port servera koji se prepisuje kod učitavanja komande */
	static int portSevera = 8003;
	
	/** Maksimalno čekanje da odgovor servera */
	static int maksCekanje;
	
	/** Pomočna varijabla koja služi za stvaranje komande */
	static String upit;
	
	/** Regularni izraz za ime korisnika */
	static String ime = "^[\\w|-]{3,10}$";
	
	/** Regularni izraz za lozinku */
	static String lozinka = "^[\\w|-|#|!]{3,10}$";
	
	/** Regularni izraz za adresu */
	static String adresa = "^[\\w|.]+$";
	
	/** Regularni izraz za IP adresu */
	static String ipAdresa = "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$";
	
	/** Regularni izraz za port */
	static String port = "^[8-9][0-9][0-9][0-9]$";
	
	/** Regularni izraz za brojeve */
	static String broj = "^[0-9]+$";
	
	/** Regularni izraz za icao*/
	static String icao = "^[A-Z]{4}$";
	
	/** Regularni izraz za datum */
	static String datum = "^(\\d{2}.\\d{2}.\\d{4}.)$";
	
	/** Regularni izraz za decimalni broj */
	static String dBroj = "^[+-]?([0-9]+\\,?[0-9]*|\\,[0-9]+)$";
	
	/** Lista elemenata koje komanda može ili mora sadržavati */
	static List<String> elementi = new ArrayList<String>();
	
	/** Format datuma koji se prima kao komanda */
	static SimpleDateFormat isoFormat = new SimpleDateFormat("dd.MM.yyyy.");
	
	/** Format datuma u koji se pretvara prije no što se šalje na server */
	static String isoFormat2 = "yyyy-MM-dd";

	/**
	 * Main metoda klase KorisnikGlavni u kojoj se prima, obrađuje i šalje komanda
	 *
	 * @param args Argumenti koje dobiva main metoda
	 */
	public static void main(String[] args) {
		
		KorisnikGlavni kg = new KorisnikGlavni();
		ucitajElemente();
		komanda = stvoriString(args);
		komanda = stvoriKomadnu(komanda);
		if (komanda == "") {
			System.out.println("ERROR 1 >> Komanda je neispravna!");
			return;
		}
		String odgovor = kg.posaljiKomandu(adresaServera, portSevera, komanda);
		System.out.println(odgovor);
	}

	/**
	 * Učitaj elemente je metoda koja popunjava listu elementima koje komanda mora ili može sadržavati
	 */
	private static void ucitajElemente() {
		
		elementi.add("-k "); //0
		elementi.add("-l "); //1
		elementi.add("-s "); //2
		elementi.add("-p "); //3
		elementi.add("-t "); //4
		
		elementi.add("--aerodrom");		//5
		elementi.add("--spremi");		//6
		elementi.add("--vrati");		//7
		elementi.add("--isprazni");		//8
		elementi.add("--statistika");	//9
		
		elementi.add("--aerodrom ");	//10
		elementi.add("--meteo ");		//11
		elementi.add("--udaljenost ");	//12
		elementi.add("--km ");			//13
		elementi.add("--datum ");		//14
		elementi.add("--tempOd ");		//15
		elementi.add("--tempDo ");		//16
		
		elementi.add("--aerodromOd ");	//17
		elementi.add("--aerodromDo ");	//18
	}

	/**
	 * Stvori string metoda od dobivenih argumenata stvara jedan string
	 *
	 * @param args Argumenti koji se prosljeđuju iz main funkcije
	 * @return odgovor String koji se stvara spajanjem svih argumenata
	 */
	private static String stvoriString(String[] args) {
		
		String odgovor = "";
		for (String s : args) {
			odgovor += s + " ";
		}
		return odgovor.trim();
	}

	/**
	 * Stvori komadnu metoda je glavna metoda koja ispituje valjanost argumenata i stvara komandu
	 *
	 * @param komanda Vrijednost svih unesenih argumenata u jednom Stringu
	 * @return upit Vrijednost stvorene komande koja će se slati na ServerGlavni
	 */
	private static String stvoriKomadnu(String komanda) {
		if(!ispitajAutentifikaciju(komanda)) {
			return "";
		}
		if(!ispitajServerInfo(komanda)) {
			return "";
		}
		String k = ispitaj(komanda, elementi.get(0), elementi.get(0).length());
		String l = ispitaj(komanda, elementi.get(1), elementi.get(1).length());
		String s = ispitaj(komanda, elementi.get(2), elementi.get(2).length());
		String p = ispitaj(komanda, elementi.get(3), elementi.get(3).length());
		String t = ispitaj(komanda, elementi.get(4), elementi.get(4).length());
		if (t.compareTo("") == 0) {
			komanda += " -t 10000";
			t = "10000";
		}
		maksCekanje = Integer.parseInt(t);
		if (!ispitajOsnovniRegex(k, l, s, p, t)) {
			return "";
		}
		upit = "USER " + k + " PASSWORD " + l;
		adresaServera = s;
		portSevera = Integer.parseInt(p);
		
		String ostatak = stvoriOstatakUpita(komanda);
		if (ostatak.length() == 0) {
			return "";
		}
		upit += ostatak;
		return upit;
	}
	
	/**
	 * Ispitaj autentifikaciju provjera postoje li argumenti imena i lozinke u komandi
	 *
	 * @param komanda String koji sadrži sve unesene argumente
	 * @return true Vraća se istina, ako postoje argumenti za ime i lozinku
	 */
	private static boolean ispitajAutentifikaciju(String komanda) {
		
		if(!komanda.contains("-k ") || !komanda.contains("-l ")) {
			return false;
		}
		return true;
	}
	
	/**
	 * Ispitaj server info provjerava postoje li argumenti za server i port
	 *
	 * @param komanda String koji sadrži sve unesene argumente
	 * @return true Vraća se istina, ako postoje argumenti za server i port
	 */
	private static boolean ispitajServerInfo(String komanda) {
		
		if(!komanda.contains("-s ") || !komanda.contains("-p ")) {
			return false;
		}
		return true;
	}
	
	/**
	 * Ispitaj
	 *
	 * @param komanda String koji sadrži sve unesene argumente
	 * @param element Jedan od elemenata liste koji služi kao sidro za pronalazak vrijednosti koja ga slijedi
	 * @param pomjer Odmak koji je jednak duljini elementa koji se ispituje
	 * @return komanda String vrijednost iza elementa do prve praznine ili kraja Stringa
	 */
	private static String ispitaj(String komanda, String element, int pomjer) {
		
		int pIndeks = pomjer;
		int rIndeks;
		pIndeks += komanda.indexOf(element);

		komanda = komanda.substring(pIndeks);
		
		rIndeks = komanda.indexOf(" ");
		if (rIndeks == -1) {
			rIndeks = komanda.length();
		}
		komanda = komanda.substring(0, rIndeks);
		komanda.trim();
		return komanda;
	}
	
	/**
	 * Ispitaj Osnovni Regularni izraz ispituje regex za ime, lozinku, adresu/IP aresu, port i cekanje
	 *
	 * @param k Vrijednost imena iz argumenata
	 * @param l Vrijednost lozinke iz argumenata
	 * @param s Vrijednost adrese/IP adrese iz argumenata
	 * @param p Vrijednost porta iz argumenata
	 * @param t Vrijednost cekanja iz argumenata
	 * @return true Vraća se istina, ako su sve vrijednosti prošle provjeru
	 */
	private static boolean ispitajOsnovniRegex(String k, String l, String s, String p, String t) {
		
		Pattern pIme = Pattern.compile(ime);
		Pattern pLozinka = Pattern.compile(lozinka);
		Pattern pAdresa = Pattern.compile(adresa);
		Pattern pIpAdresa = Pattern.compile(ipAdresa);
		Pattern pPort = Pattern.compile(port);
		Pattern pCekanje = Pattern.compile(broj);
		
		Matcher mIme = pIme.matcher(k.toString());
		Matcher mLozinka = pLozinka.matcher(l.toString());
		Matcher mAdresa = pAdresa.matcher(s.toString());
		Matcher mIpAdresa = pIpAdresa.matcher(s.toString());
		Matcher mPort = pPort.matcher(p.toString());
		Matcher mCekanje = pCekanje.matcher(t.toString());
		if(!mIme.matches() || !mLozinka.matches() || !mPort.matches() || !mCekanje.matches()) {
			return false;
		}
		if (!mAdresa.matches() && !mIpAdresa.matches()) {
			return false;
		}
		return true;
	}

	/**
	 * Stvori ostatak upita je metoda koja stvara dio upita koji se ne odnosi na podatke za autentifikaciju
	 *
	 * @param komanda String koji sadrži sve unesene argumente
	 * @return odgovor Varijabla koja sadrži ostatak komande ili je prazna ovisno o broju argumenata i obradi
	 */
	private static String stvoriOstatakUpita(String komanda) {
		
		String[] p = komanda.split(" ");
		String odgovor = "";
		if (p.length == 11) {
			odgovor = obradiKomanduArg1(komanda);
		} else if (p.length == 12) {
			odgovor = obradiKomanduArg2(komanda);
		} else if (p.length == 14) {
			odgovor = obradiKomanduArg4(komanda);
		} else if (p.length == 15) {
			odgovor = obradiKomanduArg5(komanda);
		} else if (p.length == 16) {
			odgovor = obradiKomanduArg6(komanda);
		} else {
			return "";
		}
		return odgovor;
	}
	
	/**
	 * Obradi komandu Arg1 je metoda koja stvara ostatak komande ako se uz indetikaciju nalazi još jedan argument
	 *
	 * @param komanda String koji sadrži sve unesene argumente
	 * @return odgovor Ostatak upita nakon obrade
	 */
	private static String obradiKomanduArg1(String komanda) {
		
		String odgovor = "";
		if (komanda.contains(elementi.get(5))) {
			odgovor += " AIRPORT";
			return odgovor;
		} else if (komanda.contains(elementi.get(6))) {
			odgovor += " CACHE BACKUP";
			return odgovor;
		} else if (komanda.contains(elementi.get(7))) {
			odgovor += " CACHE RESTORE";
			return odgovor;
		} else if (komanda.contains(elementi.get(8))) {
			odgovor += " CACHE CLEAR";
			return odgovor;
		} else if (komanda.contains(elementi.get(9))) {
			odgovor += " CACHE STAT";
			return odgovor;
		}
		return "";
	}
	
	/**
	 * Obradi komandu Arg2 je metoda koja stvara ostatak komande ako se uz indetikaciju nalazi još dva argumenta
	 *
	 * @param komanda String koji sadrži sve unesene argumente
	 * @return odgovor Ostatak upita nakon obrade
	 */
	private static String obradiKomanduArg2(String komanda) {
		
		String odgovor = "";
		if (komanda.contains(elementi.get(10))) {
			String i = ispitaj(komanda, elementi.get(10), elementi.get(10).length());
			Pattern pIcao = Pattern.compile(icao);
			Matcher mIcao = pIcao.matcher(i.toString());
			if (mIcao.matches()) {
				odgovor += " AIRPORT " + i;
				return odgovor;
			}
		} else if (komanda.contains(elementi.get(11))) {
			String i = ispitaj(komanda, elementi.get(11), elementi.get(11).length());
			Pattern pIcao = Pattern.compile(icao);
			Matcher mIcao = pIcao.matcher(i.toString());
			if (mIcao.matches()) {
				odgovor += " METEO " + i;
				return odgovor;
			}
		} else if (komanda.contains(elementi.get(8)) && komanda.contains(elementi.get(12))) {
			odgovor += " DISTANCE CLEAR";
			return odgovor;
		}
		return "";
	}
	
	/**
	 * Obradi komandu Arg4 je metoda koja stvara ostatak komande ako se uz indetikaciju nalazi još četiri argumenta
	 *
	 * @param komanda String koji sadrži sve unesene argumente
	 * @return odgovor Ostatak upita nakon obrade
	 */
	private static String obradiKomanduArg4(String komanda) {
		
		String odgovor = "";
		if (komanda.contains(elementi.get(10)) && komanda.contains(elementi.get(13))) {
			String i = ispitaj(komanda, elementi.get(10), elementi.get(10).length());
			String b = ispitaj(komanda, elementi.get(13), elementi.get(13).length());
			Pattern pIcao = Pattern.compile(icao);
			Matcher mIcao = pIcao.matcher(i.toString());
			Pattern pBroj = Pattern.compile(broj);
			Matcher mBroj = pBroj.matcher(b.toString());
			if (mIcao.matches() && mBroj.matches()) {
				odgovor += " AIRPORT " + i + " " + b;
				return odgovor;
			}
		} else if (komanda.contains(elementi.get(11)) && komanda.contains(elementi.get(14))) {
			String i = ispitaj(komanda, elementi.get(11), elementi.get(11).length());
			String d = ispitaj(komanda, elementi.get(14), elementi.get(14).length());
			Pattern pIcao = Pattern.compile(icao);
			Matcher mIcao = pIcao.matcher(i.toString());
			Pattern pDatum = Pattern.compile(datum);
			Matcher mDatum = pDatum.matcher(d.toString());
			if (mIcao.matches() && mDatum.matches()) {
				d = pretvoriDatum(d);
				odgovor += " METEO " + i + " " + d;
				return odgovor;
			}
		} else if (komanda.contains(elementi.get(15)) && komanda.contains(elementi.get(16))) {
			String t1 = ispitaj(komanda, elementi.get(15), elementi.get(15).length());
			String t2 = ispitaj(komanda, elementi.get(16), elementi.get(16).length());
			Pattern pTemp = Pattern.compile(dBroj);
			Matcher mTemp1 = pTemp.matcher(t1.toString());
			Matcher mTemp2 = pTemp.matcher(t2.toString());
			if (mTemp1.matches() && mTemp2.matches()) {
				odgovor += " TEMP " + t1 + " " + t2;
				return odgovor;
			}
		}
		return "";
	}
	
	/**
	 * Obradi komandu Arg5 je metoda koja stvara ostatak komande ako se uz indetikaciju nalazi još pet argumenata
	 *
	 * @param komanda String koji sadrži sve unesene argumente
	 * @return odgovor Ostatak upita nakon obrade
	 */
	private static String obradiKomanduArg5(String komanda) {
		
		String odgovor = "";
		if (komanda.contains(elementi.get(12)) && komanda.contains(elementi.get(17))
				&& komanda.contains(elementi.get(18))) {
			String i1 = ispitaj(komanda, elementi.get(17), elementi.get(17).length());
			String i2 = ispitaj(komanda, elementi.get(18), elementi.get(18).length());
			Pattern pIcao = Pattern.compile(icao);
			Matcher mIcao1 = pIcao.matcher(i1.toString());
			Matcher mIcao2 = pIcao.matcher(i2.toString());
			if (mIcao1.matches() && mIcao2.matches()) {
				odgovor += " DISTANCE " + i1 + " " + i2;
				return odgovor;
			}
		}
		return "";
	}
	
	/**
	 * Obradi komandu Arg6 je metoda koja stvara ostatak komande ako se uz indetikaciju nalazi još šest argumenata
	 *
	 * @param komanda String koji sadrži sve unesene argumente
	 * @return odgovor Ostatak upita nakon obrade
	 */
	private static String obradiKomanduArg6(String komanda) {
		
		String odgovor = "";
		if (komanda.contains(elementi.get(14)) && komanda.contains(elementi.get(15))
				&& komanda.contains(elementi.get(16))) {
			String d = ispitaj(komanda, elementi.get(14), elementi.get(14).length());
			String t1 = ispitaj(komanda, elementi.get(15), elementi.get(15).length());
			String t2 = ispitaj(komanda, elementi.get(16), elementi.get(16).length());
			Pattern pBroj = Pattern.compile(dBroj);
			Pattern pDatum = Pattern.compile(datum);
			Matcher mDatum = pDatum.matcher(d.toString());
			Matcher mBroj1 = pBroj.matcher(t1.toString());
			Matcher mBroj2 = pBroj.matcher(t2.toString());
			if (mBroj1.matches() && mBroj2.matches() && mDatum.matches()) {
				d = pretvoriDatum(d);
				odgovor += " TEMP " + t1 + " " + t2 + " " + d;
				return odgovor;
			}
		}
		return "";
	}

	/**
	 * Pretvori datum.
	 *
	 * @param datumString Vrijednost unesenog datuma kao Stringa
	 * @return odgovor Datum sa izmjenjenim formatom
	 */
	private static String pretvoriDatum(String datumString) {
		
		Date datumDatum;
		try {
			datumDatum = (Date) isoFormat.parse(datumString);
		} catch (ParseException e) {
			return "";
		}
		isoFormat.applyPattern(isoFormat2);
		String odgovor = isoFormat.format(datumDatum);
		return odgovor;
	}

	/**
	 * Pošalji komandu.
	 *
	 * @param adresa Adresa servera na koji se šalje komanda
	 * @param port Port servera na koji se šalje komanda
	 * @param komanda Komanda koja se šalje na Server Glavni ili neki drugi server
	 * @return Odgovor koji je poslao server ili prazni String ukoliko je došlo do komplikacija
	 */
	public String posaljiKomandu(String adresa, int port, String komanda) {
		
		InetSocketAddress isa = new InetSocketAddress(adresa, port);
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
			System.out.println("ERROR 2 >> Problem sa spajanjem na server!");
		}
		return null;
	}
}
