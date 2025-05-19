package at.fhj.hydromate.database;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import at.fhj.hydromate.beans.HydrationEntry;

@Database(entities = {HydrationEntry.class}, version = 1)
public abstract class HydrationDatabase extends RoomDatabase {
    public abstract HydrationDao hydrationDao();
}