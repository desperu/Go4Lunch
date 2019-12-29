package org.desperu.go4lunch.view.adapter;

import org.desperu.go4lunch.view.base.BaseAdapter;
import org.desperu.go4lunch.viewmodel.UserDBViewModel;

import java.util.List;

public class WorkmatesAdapter extends BaseAdapter {

    private final int layoutId;
    private List<UserDBViewModel> userList;

    public WorkmatesAdapter(int layoutId, List<UserDBViewModel> userList) {
        this.layoutId = layoutId;
        this.userList = userList;
    }

    @Override
    protected Object getObjForPosition(int position) { return this.userList.get(position); }

    @Override
    protected Object getObj2ForPosition(int position) {
        return null;
    }

    @Override
    protected int getLayoutIdForPosition(int position) { return layoutId; }

    @Override
    public int getItemCount() { return this.userList.size(); }
}
