package org.desperu.go4lunch.view.restaurantdetail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;

import org.desperu.go4lunch.R;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    // FOR DATA
    private List<String> userId;
    private RequestManager glide;
    private Context context;

    // CONSTRUCTOR
    public RestaurantAdapter(List<String> userId, RequestManager glide, Context context) {
        this.userId = userId;
        this.glide = glide;
        this.context = context;
    }

    @Override
    public RestaurantViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        // CREATE VIEW HOLDER AND INFLATING ITS XML LAYOUT
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_restaurant_detail_item, parent, false);

        return new RestaurantViewHolder(view);
    }

    // UPDATE VIEW HOLDER WITH A USER
    @Override
    public void onBindViewHolder(@NotNull RestaurantViewHolder viewHolder, int position) {
        viewHolder.updateWithUser(this.userId.get(position), this.glide, this.context);
    }

    // RETURN THE TOTAL COUNT OF ITEMS IN THE LIST
    @Override
    public int getItemCount() {
        return this.userId.size();
    }

    public String getUser(int position) { return this.userId.get(position); }
}
