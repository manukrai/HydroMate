package at.fhj.hydromate.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import at.fhj.hydromate.R;
import at.fhj.hydromate.beans.HydrationEntry;

/**
 * Adapter für die RecyclerView-Liste der Getränkeeinnahmen
 * Verwaltet die Darstellung und Interaktion mit einzelnen Getränkeeinnahmen
 */
public class DrinkAdapter extends RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder> {
    /**
     * Liste der Getränkeeinnahmen (HydrationEntry-Objekte)
     */
    private List<HydrationEntry> drinkList;

    /**
     * Konstruktor zur Initialisierung mit der Getränkeliste
     *
     * @param drinkList Liste von {@link HydrationEntry}-Objekten
     */
    public DrinkAdapter(List<HydrationEntry> drinkList) {
        this.drinkList = drinkList;
    }

    /**
     * Erstellt eine neue ViewHolder-Instanz und bindet das Layout
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return Eine neue Instanz {@link DrinkViewHolder}
     */
    @NonNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drink, parent, false);
        return new DrinkViewHolder(view);
    }

    /**
     * Bindet die Daten eines HydrationEntry-Objekts an das ViewHolder-Layout
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull DrinkViewHolder holder, int position) {
        HydrationEntry drink = drinkList.get(position);
        holder.drinkVolume.setText(drink.getVolume() + " ml x" + drink.getProcent());

        switch (drink.getDrinkType()) {
            case "water":
                holder.drinkIcon.setImageResource(R.drawable.water_bottle);
                holder.drinkType.setText("Water");
                break;
            case "coffee":
                holder.drinkIcon.setImageResource(R.drawable.coffee_cup);
                holder.drinkType.setText("Coffee");
                break;
            case "juice":
                holder.drinkIcon.setImageResource(R.drawable.juice);
                holder.drinkType.setText("Juice");
                break;
            case "tea":
                holder.drinkIcon.setImageResource(R.drawable.tea);
                holder.drinkType.setText("Tea");
                break;
            case "milk":
                holder.drinkIcon.setImageResource(R.drawable.milk);
                holder.drinkType.setText("Milk");
                break;
            case "beer":
                holder.drinkIcon.setImageResource(R.drawable.beer);
                holder.drinkType.setText("Beer");
                break;
            case "liquor":
                holder.drinkIcon.setImageResource(R.drawable.liquor);
                holder.drinkType.setText("Liquor");
                break;
        }

        holder.btDelete.setId(drink.getId());

    }

    /**
     * Gibt die Gesamtanzahl der Listeneinträge zurück
     *
     * @return Anzahl der Einträge
     */
    @Override
    public int getItemCount() {
        return drinkList.size();
    }

    /**
     * ViewHolder-Klasse für die Darstellung eines einzelnen Getränkeelements
     */
    public static class DrinkViewHolder extends RecyclerView.ViewHolder {
        /**
         *TestView drinkType Anzeige des Getränketyps
         * TestView drinkVolume zur Anzeige des Getränkevolumens
         * ImageView drinkIcon für die Getränkesorte
         * Button btDelete zum Löschen des Eintrags
         */
        TextView drinkType, drinkVolume;
        ImageView drinkIcon;

        Button btDelete;

        /**
         * Konstruktor zum Initialiseren der View-Elemente
         * @param itemView Die View, die dem einzelnen Eintrag zugeordnet ist
         */
        public DrinkViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkType = itemView.findViewById(R.id.drink_type);
            drinkVolume = itemView.findViewById(R.id.drink_volume);
            drinkIcon = itemView.findViewById(R.id.drink_icon);
            btDelete = itemView.findViewById(R.id.delete_button);
        }
    }
}
