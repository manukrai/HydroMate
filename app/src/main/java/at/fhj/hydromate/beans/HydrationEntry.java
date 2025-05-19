package at.fhj.hydromate.beans;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public double getProcent() {
        return procent;
    }

    public void setProcent(double procent) {
        this.procent = procent;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDrinkType() {
        return drinkType;
    }

    public void setDrinkType(String drinkType) {
        this.drinkType = drinkType;
    }
}
