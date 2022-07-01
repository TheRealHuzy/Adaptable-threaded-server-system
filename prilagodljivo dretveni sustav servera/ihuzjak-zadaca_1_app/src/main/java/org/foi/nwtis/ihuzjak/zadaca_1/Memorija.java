package org.foi.nwtis.ihuzjak.zadaca_1;

import java.io.Serializable;

public class Memorija implements Comparable<Memorija>, Serializable{
	private static final long serialVersionUID = 1L;
	String komanda;
	String odgovor;
	int brojPoziva;
	String zadnjeVrijeme;
	
	public Memorija(String komanda, String odgovor, int brojPoziva, String zadnjeVrijeme) {
		super();
		this.komanda = komanda;
		this.odgovor = odgovor;
		this.brojPoziva = brojPoziva;
		this.zadnjeVrijeme = zadnjeVrijeme;
	}
	
	@Override
	public int compareTo(Memorija m) {
		return this.brojPoziva - m.brojPoziva;
	}
}
