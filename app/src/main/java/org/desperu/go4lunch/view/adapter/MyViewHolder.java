package org.desperu.go4lunch.view.adapter;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class MyViewHolder extends RecyclerView.ViewHolder {

    private final ViewDataBinding binding;

    public MyViewHolder(@NotNull ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Object obj) {
        binding.setVariable(org.desperu.go4lunch.BR.obj, obj);
        binding.executePendingBindings();
    }

    public void bind2(Object obj2) {
        binding.setVariable(org.desperu.go4lunch.BR.obj2, obj2);
        binding.executePendingBindings();
    }
}