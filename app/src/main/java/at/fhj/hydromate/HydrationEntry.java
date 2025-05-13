package at.fhj.hydromate;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class HydrationEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int volume;
    public double procent;
    public String date;
    public String drinkType;
}
