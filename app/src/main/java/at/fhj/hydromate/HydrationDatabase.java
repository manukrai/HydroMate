package at.fhj.hydromate;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import at.fhj.hydromate.HydrationEntry;

@Database(entities = {HydrationEntry.class}, version = 1)
public abstract class HydrationDatabase extends RoomDatabase {
    public abstract HydrationDao hydrationDao();
}