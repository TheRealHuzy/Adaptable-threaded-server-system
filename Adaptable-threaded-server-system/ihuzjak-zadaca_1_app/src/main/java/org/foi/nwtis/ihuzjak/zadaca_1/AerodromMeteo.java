package org.foi.nwtis.ihuzjak.zadaca_1;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Klasa za meteo zapis aerodroma
 */
@AllArgsConstructor()
public class AerodromMeteo implements Serializable {
	private static final long serialVersionUID = 1L;
	@Getter
	@Setter
	@NonNull
	private String icao;
	@Getter
	@Setter
	private double temp;
	@Getter
	@Setter
	private double tlak;
	@Getter
	@Setter
	private double vlaga;
	@Getter
	@Setter
	@NonNull
	private String vrijeme;
	@Getter
	@Setter
	private long time;
}
