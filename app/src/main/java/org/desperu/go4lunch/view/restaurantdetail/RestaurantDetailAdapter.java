package org.desperu.go4lunch.view.restaurantdetail;

import org.desperu.go4lunch.base.BaseAdapter;
import org.desperu.go4lunch.viewmodel.UserDBViewModel;

import java.util.List;

public class RestaurantDetailAdapter extends BaseAdapter {


    private final int layoutId;
    private List<UserDBViewModel> restaurantList;

    public RestaurantDetailAdapter(int layoutId, List<UserDBViewModel> restaurantList) {
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
