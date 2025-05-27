package at.fhj.hydromate.database;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

import at.fhj.hydromate.beans.HydrationEntry;

/**
 * Data Access Object für den Zugriff auf die Hydration-Datenbank
 * Diese Schnittstelle definiert Methoden zum Einfügen, Abfragen und Löschen von
 * {@link HydrationEntry}-Einträgen in der lokalen SQLite-Datenbank mithilfe von Room
 */
@Dao
public interface HydrationDao {
    /**
     * Fügt einen neuen Hydrationeintrag in die Datenbank ein
     * @param entry Der einzufügende {@link HydrationEntry}
     */
    @Insert
    void insert(HydrationEntry entry);

    /**
     * Gibt alle gespeicherten Trinkeinträge sortiert nach Datum (Absteigend) zurück
     * @return Eine Liste alle{@link HydrationEntry}-Objekte
     */
    @Query("SELECT * FROM HydrationEntry ORDER BY date DESC")
    List<HydrationEntry> getAllEntries();

    /**
     * Gibt alle Trinkeinträge für ein bestimmtes Datum zurück
     * @param targetDate Das gewünschte Datum im Format "yyyy-mm-dd"
     * @return Eine Liste der {@link HydrationEntry}-Objekte für das angegebene Datum
     */
    @Query("SELECT * FROM HydrationEntry WHERE date = :targetDate ORDER BY date DESC")
    List<HydrationEntry> getEntriesForDate(String targetDate);

    /**
     * Gibt die Gesamtmenge der aufgenommenen Flüssigkeit (in ml) für den aktuellen Tag zurücl
     * @param todayDate Das heutige Datum im Format "yyyy-mm-dd"
     * @return Die Summe der aufgenommenen Flüssigkeit in Milliliter
     */
    @Query("SELECT SUM(volume) FROM HydrationEntry WHERE date = :todayDate")
    int getTotalVolumeForToday(String todayDate);

    /**
     * Gibt die effektive Hydration (Flüssigkeitsmenge multipliziert mit Prozentfaktor) für den aktuellen Tag zurück
     * Beispiel: 500 ml Wasser (100%) + 300 ml Kaffee (80%) = 740ml effektive Hydration
     * @param todayDate Das heutige Datum im Format "yyyy-mm.dd"
     * @return Die effektive Trinkmenge als Gleitkommazahl
     */
    @Query("SELECT SUM(volume * procent) FROM HydrationEntry WHERE date = :todayDate")
    double getEffectiveHydrationForToday(String todayDate);

    /**
     * Löscht einen Hydrationseintrag anhand seiner ID
     * @param id Die ID des zu löschenden Eintrags
     */
    @Query("DELETE FROM HydrationEntry WHERE id = :id")
    void deleteById(int id);
}
