package org.desperu.go4lunch.view.main.fragments;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.view.base.BaseFragment;
import org.desperu.go4lunch.models.User;
import org.desperu.go4lunch.view.adapter.WorkmatesAdapter;
import org.desperu.go4lunch.viewmodel.RestaurantDBViewModel;
import org.desperu.go4lunch.viewmodel.UserDBViewModel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class WorkmatesFragment extends BaseFragment {

    @BindView(R.id.fragment_recycler_view_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fragment_recycler_view) RecyclerView recyclerView;

    private WorkmatesAdapter adapter;
    private List<UserDBViewModel> allWorkmatesList = new ArrayList<>();
    private List<RestaurantDBViewModel> bookedRestaurantList = new ArrayList<>();

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getFragmentLayout() { return R.layout.fragment_recycler_view; }

    @Override
    protected void configureDesign() {
        this.configureRecyclerView();
        this.getAllWorkmatesList();
        this.configureSwipeRefresh();
    }


    public WorkmatesFragment() {
        // Needed empty constructor
    }

    @NotNull
    @Contract(" -> new")
    public static WorkmatesFragment newInstance() { return new WorkmatesFragment(); }

    // --------------
    // CONFIGURATION
    // --------------

    /**
     * Configure recycler view.
     */
    private void configureRecyclerView() {
        // Create adapter passing in the sample user data
        this.adapter = new WorkmatesAdapter(R.layout.fragment_workmates_item, allWorkmatesList, bookedRestaurantList);
        // Attach the adapter to the recyclerView to populate items
        this.recyclerView.setAdapter(this.adapter);
        // Set layout manager to position the items
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * Load all workmates list.
     */
    private void getAllWorkmatesList() {
        assert getActivity() != null;
        UserDBViewModel allUsers = new UserDBViewModel(getActivity().getApplication());
        allUsers.fetchAllUsers();
        allUsers.getAllUsersListLiveData().observe(this, this::updateRecyclerView);
    }

    /**
     * Configure swipe to refresh.
     */
    private void configureSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::getAllWorkmatesList);
    }

    // --------------
    // UI
    // --------------

    /**
     * Update recycler view when received data.
     * @param allUsersList List of all apk users, from firestore.
     */
    private void updateRecyclerView(@NotNull List<User> allUsersList) {
        assert getActivity() != null;
        allWorkmatesList.clear();
        for (User user : allUsersList) {
            // User data from firestore
            UserDBViewModel userDBViewModel = new UserDBViewModel(getActivity().getApplication(), user.getUid());
            userDBViewModel.fetchUser();
            this.allWorkmatesList.add(userDBViewModel);

            // Restaurant data from firestore
            RestaurantDBViewModel restaurantDBViewModel = new RestaurantDBViewModel(user.getBookedRestaurantId());
            if (user.getBookedRestaurantId() != null) restaurantDBViewModel.fetchRestaurant();
            this.bookedRestaurantList.add(restaurantDBViewModel);
        }
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }
}