package at.fhj.hydromate.database;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {HydrationEntry.class}, version = 1)
public abstract class HydrationDatabase extends RoomDatabase {
    public abstract HydrationDao hydrationDao();
}