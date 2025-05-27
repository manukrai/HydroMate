package at.fhj.hydromate.beans;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Repräsentiert einen Trinkeintrag in der Datenbank
 * Diese Klasse wird von Room als Datenbank-Entity verwendet
 */
@Entity
public class HydrationEntry {
    /**
     * Eindeutige ID des Eintrags
     * Wird automatisch generiert
     */
    @PrimaryKey(autoGenerate = true)
    public int id;

    /**
     * Getrunkene Flüssigkeitsmenge in Millilitern
     */
    public int volume;
    /**
     * Wirksamkeit des Getränks in Prozent (z.B.1.0 für Wasser, 0.8 für Kaffee)
     */
    public double procent;
    /**
     * Datum des Eintrags im Format "YYYY-MM-DD"
     */
    public String date;
    /**
     * Typ des Getränks
     */
    public String drinkType;

    /**
     * Gibt die ID des Eintrags zurück
     * @return die eindeutige ID
     */
    public int getId() {
        return id;
    }

    /**
     * Setzt die ID des Eintrags
     * @param id die neue ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gibt das Volumen des Getränke zurück
     * @return Volumen in Millilitern
     */
    public int getVolume() {
        return volume;
    }

    /**
     * Setzt das Volumen des Getränks
     * @param volume Volumen in Millilitern
     */
    public void setVolume(int volume) {
        this.volume = volume;
    }

    /**
     * Gibt den WIrkungsgrad (Prozentsatz) des Getränks zurück
     * @return Prozentwert (zwischen 0.0 und 1.0)
     */
    public double getProcent() {
        return procent;
    }

    /**
     * Setzt den Wirkungsgrad (Prozentsatz) des Getränks
     * @param procent Prozentwert
     */
    public void setProcent(double procent) {
        this.procent = procent;
    }

    /**
     * Gibt das Datum des Eintrags zurück
     * @return Datum als String
     */
    public String getDate() {
        return date;
    }

    /**
     * Setzt das Datum des Eintrags
     * @param date Datum im Format "YYYY-MM-DD"
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Gibt den Getränketyp zurück
     * @return Getränketyp als String
     */
    public String getDrinkType() {
        return drinkType;
    }

    /**
     * Setzt den Getränketyp
     * @param drinkType Getränketyp
     */
    public void setDrinkType(String drinkType) {
        this.drinkType = drinkType;
    }
}
