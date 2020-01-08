package org.desperu.go4lunch.view.main.fragments;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.view.base.BaseFragment;
import org.desperu.go4lunch.models.User;
import org.desperu.go4lunch.view.adapter.WorkmatesAdapter;
import org.desperu.go4lunch.viewmodel.UserDBViewModel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class WorkmatesFragment extends BaseFragment {

    // FOR DESIGN
    @BindView(R.id.fragment_recycler_view_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fragment_recycler_view) RecyclerView recyclerView;

    // FOR BUNDLE
    public static final String QUERY_TERM_WORKMATES = "queryTerm";

    // FOR DATA
    private WorkmatesAdapter adapter;
    private List<UserDBViewModel> allWorkmatesList = new ArrayList<>();
    private List<User> allUsersList = new ArrayList<>();
    private String queryTerm;

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getFragmentLayout() { return R.layout.fragment_recycler_view; }

    @Override
    protected void configureDesign() {
        this.setDataFromBundle();
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
     * Set data from bundle.
     */
    private void setDataFromBundle() {
        this.queryTerm = getArguments() != null ? getArguments().getString(QUERY_TERM_WORKMATES) : null;
    }

    /**
     * Configure recycler view.
     */
    private void configureRecyclerView() {
        // Create adapter passing in the sample user data
        this.adapter = new WorkmatesAdapter(R.layout.fragment_workmates_item, allWorkmatesList);
        // Attach the adapter to the recyclerView to populate items
        this.recyclerView.setAdapter(this.adapter);
        // Set layout manager to position the items
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * Configure swipe to refresh.
     */
    private void configureSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::getAllWorkmatesList);
    }

    // --------------
    // REQUEST
    // --------------

    /**
     * Load all workmates list.
     */
    private void getAllWorkmatesList() {
        assert getActivity() != null;
        UserDBViewModel allUsers = new UserDBViewModel(getActivity().getApplication());
        allUsers.fetchAllUsers();
        allUsers.getAllUsersListLiveData().observe(this, this::manageRequestResponse);
    }

    // --------------
    // ACTION
    // --------------

    /**
     * Method called when query term changed.
     * @param query Query term.
     */
    public void onSearchQueryTextChange(String query) {
        this.queryTerm = query;
        this.updateRecyclerView(this.searchQueryInList());
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
        this.allWorkmatesList.clear();
        for (User user : allUsersList) {
            // User data from firestore
            UserDBViewModel userDBViewModel = new UserDBViewModel(getActivity().getApplication(), user.getUid());
            userDBViewModel.fetchUser();
            this.allWorkmatesList.add(userDBViewModel);
        }
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    // --------------
    // UTILS
    // --------------

    /**
     * Manage request response.
     * @param allUsersList All users list.
     */
    private void manageRequestResponse(List<User> allUsersList) {
        this.allUsersList = this.sortWorkmatesList(allUsersList);
        if (queryTerm != null && !queryTerm.isEmpty())
            this.updateRecyclerView(this.searchQueryInList());
        else this.updateRecyclerView(this.allUsersList);
    }

    /**
     * Sort workmates list to show at top decided user for lunch.
     * @param allUsersList All users list.
     * @return All users list sorted.
     */
    @Contract("_ -> param1")
    private List<User> sortWorkmatesList(@NotNull List<User> allUsersList) {
        List<User> decidedUsers = new ArrayList<>();
        List<User> notDecidedUsers = new ArrayList<>();
        for (User user : allUsersList) {
            if (user.getBookedRestaurantId() != null)
                decidedUsers.add(user);
            else notDecidedUsers.add(user);
        }
        allUsersList.clear();
        allUsersList.addAll(decidedUsers);
        allUsersList.addAll(notDecidedUsers);
        return allUsersList;
    }

    /**
     * Search query term on name of user in allUsersList.
     * @return Found users list.
     */
    private List<User> searchQueryInList() {
        List<User> foundUsers = new ArrayList<>();
        for (User user : allUsersList) {
            if (user.getUserName().toLowerCase().contains(queryTerm.toLowerCase()))
                foundUsers.add(user);
        }
        return foundUsers;
    }
}