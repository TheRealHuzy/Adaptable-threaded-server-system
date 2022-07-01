package org.foi.nwtis.ihuzjak.zadaca_1;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 *
 * Klasa za aerodrom
 */
@AllArgsConstructor()
public class Aerodrom {
	@Getter
    @Setter 
    @NonNull 
    private String icao;
    @Getter
    @Setter 
    @NonNull 
    private String naziv;
    @Getter
    @Setter 
    @NonNull 
    private String gpsGS;
    @Getter
    @Setter 
    @NonNull 
    private String gpsGD;
}
