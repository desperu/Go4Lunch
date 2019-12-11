package org.desperu.go4lunch.view.restaurantdetail;

import androidx.recyclerview.widget.RecyclerView;

import org.desperu.go4lunch.databinding.FragmentRestaurantDetailItemBinding;
import org.desperu.go4lunch.viewmodel.UserDBViewModel;
import org.jetbrains.annotations.NotNull;

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    private final FragmentRestaurantDetailItemBinding binding;

    public RestaurantViewHolder(@NotNull FragmentRestaurantDetailItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(UserDBViewModel userDBViewModel) {
        binding.setUserDBViewModel(userDBViewModel);
        binding.executePendingBindings();
    }
}