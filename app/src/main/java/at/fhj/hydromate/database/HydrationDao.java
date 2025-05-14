package at.fhj.hydromate.database;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface HydrationDao {
    @Insert
    void insert(HydrationEntry entry);

    @Query("SELECT * FROM HydrationEntry ORDER BY date DESC")
    List<HydrationEntry> getAllEntries();

    @Query("SELECT SUM(volume) FROM HydrationEntry WHERE date = :todayDate")
    int getTotalVolumeForToday(String todayDate);

    @Query("SELECT SUM(volume * procent) FROM HydrationEntry WHERE date = :todayDate")
    double getEffectiveHydrationForToday(String todayDate);

}
