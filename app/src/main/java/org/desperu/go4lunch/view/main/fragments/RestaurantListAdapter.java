package org.desperu.go4lunch.view.main.fragments;

import org.desperu.go4lunch.base.BaseAdapter;
import org.desperu.go4lunch.viewmodel.RestaurantViewModel;

import java.util.List;

public class RestaurantListAdapter extends BaseAdapter {

    private final int layoutId;
    private List<RestaurantViewModel> restaurantList;

    public RestaurantListAdapter(int layoutId, List<RestaurantViewModel> restaurantList) {
        this.layoutId = layoutId;
        this.restaurantList = restaurantList;
    }

    @Override
    protected Object getObjForPosition(int position) { return this.restaurantList.get(position); }

    @Override
    protected int getLayoutIdForPosition(int position) { return layoutId; }

    @Override
    public int getItemCount() { return this.restaurantList.size(); }
}
