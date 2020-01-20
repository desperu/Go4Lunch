package org.desperu.go4lunch.view.main.fragments;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.libraries.places.api.model.Place;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.view.base.BaseFragment;
import org.desperu.go4lunch.models.User;
import org.desperu.go4lunch.view.adapter.WorkmatesAdapter;
import org.desperu.go4lunch.viewmodel.RestaurantInfoViewModel;
import org.desperu.go4lunch.viewmodel.UserDBViewModel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

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
    private List<User> notDecidedUsers = new ArrayList<>();
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
        allUsers.getAllUsersListLiveData().observe(this, this::sortWorkmatesList);
    }

    // --------------
    // ACTION
    // --------------

    /**
     * Method called when query term changed.
     * @param query Query term.
     */
    public void onSearchQueryTextChange(@NotNull String query) {
        this.queryTerm = query;
        if (!query.isEmpty())
            this.updateRecyclerView(this.searchQueryInList());
        else getAllWorkmatesList();
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
        this.allUsersList = allUsersList;
        if (queryTerm != null && !queryTerm.isEmpty())
            this.updateRecyclerView(this.searchQueryInList());
        else this.updateRecyclerView(allUsersList);
    }

    /**
     * Sort workmates list to show at top decided user for lunch.
     * @param allUsersList All users list.
     */
    private void sortWorkmatesList(@NotNull List<User> allUsersList) {
        List<User> decidedUsers = new ArrayList<>();
        List<User> notDecidedUsers = new ArrayList<>();

        // Sort booked and unbooked users
        for (User user : allUsersList) {
            if (user.getBookedRestaurantId() != null)
                decidedUsers.add(user);
            else notDecidedUsers.add(user);
        }
        if (!decidedUsers.isEmpty())
            setSortedRestaurantNameList(decidedUsers);
        sortNotDecidedUserList(notDecidedUsers, decidedUsers.isEmpty());
    }

    /**
     * Set restaurant name list, and sort alphabetically.
     * @param userList User list.
     */
    private void setSortedRestaurantNameList(@NotNull List<User> userList) {
        assert getActivity() != null;
        List<Place> placeList = new ArrayList<>();
        List<String> restaurantNamesList = new ArrayList<>();
        for (User user : userList) {
            RestaurantInfoViewModel restaurantInfoViewModel = new RestaurantInfoViewModel(
                    getActivity().getApplication(), user.getBookedRestaurantId());
            restaurantInfoViewModel.getPlaceLiveData().observe(this, place -> {
                placeList.add(place);
                restaurantNamesList.add(place.getName());
                if (restaurantNamesList.size() == userList.size()) {
                    Collections.sort(restaurantNamesList);
                    this.sortUserList(userList, placeList, restaurantNamesList);
                }
            });
        }
    }

    /**
     * Sort user list by booked restaurant name.
     * @param userList User list.
     * @param placeList Booked place list.
     * @param sortedRestaurantNames Restaurant name list sorted.
     */
    private void sortUserList(List<User> userList, List<Place> placeList,
                              @NotNull Collection<String> sortedRestaurantNames) {
        List<User> allUsersList = new ArrayList<>();
        // Sort place list with sorted restaurant names list
        int position = -1;
        for (String restaurantName : sortedRestaurantNames) {
            position++;
            for (int i = 0; i < placeList.size(); i++) {
                if (restaurantName.equals(placeList.get(i).getName())) {
                    placeList.add(position, placeList.get(i));
                    placeList.remove(i + 1);
                }
            }
        }

        // Sort userList with sorted place list
        position = -1;
        for (Place place : placeList) {
            position++;
            for (int i = 0; i < userList.size(); i++) {
                if (Objects.equals(place.getId(), userList.get(i).getBookedRestaurantId())
                        && !allUsersList.contains(userList.get(i))) {
                    allUsersList.add(position, userList.get(i));
                }
            }
        }

        allUsersList.addAll(notDecidedUsers);
        this.manageRequestResponse(allUsersList);
    }

    /**
     * Sort not decided user list alphabetically.
     * @param userList Not decider user list.
     * @param isDecidedUsersEmpty Is decided users list is empty.
     */
    private void sortNotDecidedUserList(@NotNull List<User> userList, boolean isDecidedUsersEmpty) {
        // Create sorted user name list
        Collection<String> sortedUserNames = new TreeSet<>(Collator.getInstance());
        for (User user : userList)
            sortedUserNames.add(user.getUserName());

        // Sort user list with sorted user name list
        int position = -1;
        for (String userName : sortedUserNames) {
            position++;
            for (int i = 0; i < userList.size(); i++) {
                if (userName.equals(userList.get(i).getUserName())) {
                    userList.add(position, userList.get(i));
                    userList.remove(i + 1);
                }
            }
        }
        notDecidedUsers.clear();
        notDecidedUsers.addAll(userList);
        if (isDecidedUsersEmpty) this.manageRequestResponse(notDecidedUsers);
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