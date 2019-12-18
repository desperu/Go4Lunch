package org.desperu.go4lunch.view.adapter;

import org.desperu.go4lunch.view.base.BaseAdapter;
import org.desperu.go4lunch.viewmodel.RestaurantInfoViewModel;

import java.util.List;

public class RestaurantListAdapter extends BaseAdapter {

    private final int layoutId;
    private List<RestaurantInfoViewModel> restaurantList;

    public RestaurantListAdapter(int layoutId, List<RestaurantInfoViewModel> restaurantList) {
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
