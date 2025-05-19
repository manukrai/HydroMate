package at.fhj.hydromate.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import at.fhj.hydromate.R;
import at.fhj.hydromate.beans.HydrationEntry;

public class DrinkAdapter extends RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder> {
    private List<HydrationEntry> drinkList;

    public DrinkAdapter(List<HydrationEntry> drinkList) {
        this.drinkList = drinkList;
    }

    @NonNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drink, parent, false);
        return new DrinkViewHolder(view);
    }

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

    }

    @Override
    public int getItemCount() {
        return drinkList.size();
    }

    public static class DrinkViewHolder extends RecyclerView.ViewHolder {
        TextView drinkType, drinkVolume;
        ImageView drinkIcon;

        public DrinkViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkType = itemView.findViewById(R.id.drink_type);
            drinkVolume = itemView.findViewById(R.id.drink_volume);
            drinkIcon = itemView.findViewById(R.id.drink_icon);
        }
    }
}
