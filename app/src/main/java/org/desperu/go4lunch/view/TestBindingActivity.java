package org.desperu.go4lunch.view;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.base.BaseActivity;
import org.desperu.go4lunch.databinding.ActivityMainNavHeaderBinding;
import org.desperu.go4lunch.viewmodel.UserViewModel;

public class TestBindingActivity extends BaseActivity {

    // TODO to remove

    @Override
    protected int getActivityLayout() { return R.layout.activity_main_nav_header; }

    @Override
    protected void configureDesign() { this.configureToolBar(); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainNavHeaderBinding navHeaderBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_nav_header);
        UserViewModel userViewModel = new UserViewModel(getBaseContext());
        navHeaderBinding.setUserViewModel(userViewModel);
    }
}
