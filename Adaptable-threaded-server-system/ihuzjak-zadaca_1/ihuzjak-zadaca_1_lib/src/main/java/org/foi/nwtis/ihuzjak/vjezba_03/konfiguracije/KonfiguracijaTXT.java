package org.foi.nwtis.ihuzjak.vjezba_03.konfiguracije;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class KonfiguracijaTXT extends KonfiguracijaApstraktna {
	public final static String TIP = "txt";

	public KonfiguracijaTXT(String nazivDatoteke) {
		super(nazivDatoteke);
	}
	
	@Override
	public void ucitajKonfiguraciju(String nazivDatoteke) throws NeispravnaKonfiguracija{
		File datoteka = new File(nazivDatoteke);
		String tip = Konfiguracija.dajTipKonfiguracije(nazivDatoteke);
		if (tip == null || tip.compareTo(TIP) != 0) {
			throw new NeispravnaKonfiguracija("Datoteka: "+ nazivDatoteke + " nema tip: " + TIP);
		} else if (!datoteka.exists() || !datoteka.canRead()) {
			throw new NeispravnaKonfiguracija("Datoteka: "+ nazivDatoteke + " nije ispravnog tipa/ne postoji/"
					+ "ne može se čitati" + KonfiguracijaTXT.TIP);
		}
		try {
			this.postavke.load(new FileInputStream(datoteka));
		} catch (IOException e) {
			throw new NeispravnaKonfiguracija(e.getMessage());
		}
	}
	
	@Override
	public void spremiKonfiguraciju(String nazivDatoteke) throws NeispravnaKonfiguracija {
		File datoteka = new File(nazivDatoteke);
		String tip = Konfiguracija.dajTipKonfiguracije(nazivDatoteke);
		if (tip == null || tip.compareTo(TIP) != 0) {
			throw new NeispravnaKonfiguracija("Datoteka: "+ nazivDatoteke + " nema tip: " + TIP);
		} else if (datoteka.exists() && !datoteka.canWrite()) {
			throw new NeispravnaKonfiguracija("Datoteka: "+ nazivDatoteke + " nije ispravnog tipa/ne postoji/"
					+ "ne može se pisati u datoteku" + KonfiguracijaTXT.TIP);
		}
		try {
			this.postavke.store(new FileOutputStream(datoteka),"NTWIS 2020");
		} catch (IOException e) {
			throw new NeispravnaKonfiguracija(e.getMessage());
		}
	}
}
