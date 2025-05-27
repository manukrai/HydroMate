package at.fhj.hydromate.database;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import at.fhj.hydromate.beans.HydrationEntry;

/**
 * Repräsentiert die zentrale Datenbank der Anwendung für Hydrationseinträge.
 *
 * Diese Klasse definiert die SQLite-Datenbank mit Hilfe der Room-Persistenzbibliothek.
 * Sie verwaltet die Entität {@link HydrationEntry} und stellt Zugriff über das zugehörige DAO
 * {@link HydrationDao} bereit.
 *
 * <p>Die Datenbank enthält nur eine Entität: HydrationEntry, und verwendet Version 1.</p>
 *
 * <p>Wird typischerweise als Singleton implementiert, um die Datenbankinstanz
 * im gesamten Anwendungskontext gemeinsam zu nutzen.</p>
 *
 * Beispiel für den Zugriff:
 * <pre>{@code
 * HydrationDatabase db = Room.databaseBuilder(context,
 *     HydrationDatabase.class, "hydration-db").build();
 * HydrationDao dao = db.hydrationDao();
 * }</pre>
 */
@Database(entities = {HydrationEntry.class}, version = 1)
public abstract class HydrationDatabase extends RoomDatabase {
    /**
     * Gibt das Data Access Object (DAO) zurück, um auf {@link HydrationEntry}-Daten zuzugreifen.
     *
     * @return Die {@link HydrationDao}-Instanz für Datenbankoperationen.
     */
    public abstract HydrationDao hydrationDao();
}