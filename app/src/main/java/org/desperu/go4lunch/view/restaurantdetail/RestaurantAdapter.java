package org.desperu.go4lunch.view.restaurantdetail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.desperu.go4lunch.databinding.FragmentRestaurantDetailItemBinding;
import org.desperu.go4lunch.viewmodel.UserDBViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    // FOR DATA
    private List<String> userId;

    // CONSTRUCTOR
    public RestaurantAdapter(List<String> userId) {
        this.userId = userId;
    }

    @Override
    public RestaurantViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        // CREATE VIEW HOLDER AND INFLATING ITS XML LAYOUT WITH DATA BINDING
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        FragmentRestaurantDetailItemBinding binding = FragmentRestaurantDetailItemBinding.inflate(layoutInflater, parent, false);
        return new RestaurantViewHolder(binding);
    }

    // UPDATE VIEW HOLDER WITH A USER
    public void onBindViewHolder(@NotNull RestaurantViewHolder holder, int position) {
        UserDBViewModel viewModel = new UserDBViewModel(this.userId.get(position));
        viewModel.fetchUser();
        holder.bind(viewModel);
    }

    // RETURN THE TOTAL COUNT OF ITEMS IN THE LIST
    @Override
    public int getItemCount() {
        return this.userId.size();
    }

    public String getUserId(int position) { return this.userId.get(position); }
}
