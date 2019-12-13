package org.desperu.go4lunch.view.main.fragments;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.base.BaseFragment;
import org.desperu.go4lunch.models.User;
import org.desperu.go4lunch.viewmodel.UserDBViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class WorkmatesFragment extends BaseFragment {

    @BindView(R.id.fragment_recycler_view_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fragment_recycler_view) RecyclerView recyclerView;

    private WorkmatesAdapter adapter;
    private List<UserDBViewModel> workmatesList = new ArrayList<>();

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getFragmentLayout() { return R.layout.fragment_recycler_view; }

    @Override
    protected void configureDesign() {
        this.configureRecyclerView();
        this.loadWorkmatesList();
        this.configureSwipeRefresh();
    }


    public WorkmatesFragment() {
        // Needed empty constructor
    }

    public static WorkmatesFragment newInstance() { return new WorkmatesFragment(); }

    // --------------
    // CONFIGURATION
    // --------------

    /**
     * Configure recycler view.
     */
    private void configureRecyclerView() {
        // Create adapter passing in the sample user data
        this.adapter = new WorkmatesAdapter(R.layout.fragment_workmates_item, workmatesList);
        // Attach the adapter to the recyclerView to populate items
        this.recyclerView.setAdapter(this.adapter);
        // Set layout manager to position the items
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * Load all workmates list.
     */
    private void loadWorkmatesList() {
        UserDBViewModel allUsers = new UserDBViewModel();
        allUsers.fetchAllUsers(this);
    }

    /**
     * Configure swipe to refresh.
     */
    private void configureSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadWorkmatesList);
    }

    // --------------
    // UI
    // --------------

    /**
     * Update recycler view when received data.
     * @param allUsers List of all apk users, from firestore.
     */
    public void updateRecyclerView(@NotNull List<User> allUsers) {
        for (User user : allUsers) {
            UserDBViewModel userDBViewModel = new UserDBViewModel(getContext(), user.getUid());
            userDBViewModel.fetchUser();
            workmatesList.add(userDBViewModel);
        }
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }
}