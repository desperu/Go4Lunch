package org.desperu.go4lunch.view.base;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.desperu.go4lunch.view.adapter.MyViewHolder;
import org.jetbrains.annotations.NotNull;

public abstract class BaseAdapter extends RecyclerView.Adapter<MyViewHolder> {

    @NotNull
    public MyViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(
                layoutInflater, viewType, parent, false);
        return new MyViewHolder(binding);
    }

    public void onBindViewHolder(@NotNull MyViewHolder holder, int position) {
        holder.bind(getObjForPosition(position));
        holder.bind2(getObj2ForPosition(position));
    }

    @Override
    public int getItemViewType(int position) {
        return getLayoutIdForPosition(position);
    }

    protected abstract Object getObjForPosition(int position);

    protected abstract Object getObj2ForPosition(int position);

    protected abstract int getLayoutIdForPosition(int position);
}
