package org.desperu.go4lunch.view.adapter;

import org.desperu.go4lunch.view.base.BaseAdapter;
import org.desperu.go4lunch.viewmodel.RestaurantDBViewModel;
import org.desperu.go4lunch.viewmodel.RestaurantInfoViewModel;

import java.util.List;

public class RestaurantListAdapter extends BaseAdapter {

    private final int layoutId;
    private List<RestaurantInfoViewModel> restaurantList;
    private List<RestaurantDBViewModel> restaurantDBList;

    public RestaurantListAdapter(int layoutId, List<RestaurantInfoViewModel> restaurantList,
                                 List<RestaurantDBViewModel> restaurantDBList) {
        this.layoutId = layoutId;
        this.restaurantList = restaurantList;
        this.restaurantDBList = restaurantDBList;
    }

    @Override
    protected Object getObjForPosition(int position) { return this.restaurantList.get(position); }

    @Override
    protected Object getObj2ForPosition(int position) { return this.restaurantDBList.get(position); }

    @Override
    protected int getLayoutIdForPosition(int position) { return layoutId; }

    @Override
    public int getItemCount() { return this.restaurantList.size(); }
}
