package org.desperu.go4lunch.view.base;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseUser;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.viewmodel.UserAuthViewModel;

import butterknife.ButterKnife;
import icepick.Icepick;

public abstract class BaseActivity extends AppCompatActivity {

    // --------------------
    // BASE METHODS
    // --------------------

    protected abstract int getActivityLayout();
    protected abstract void configureDesign();

    // --------------------
    // LIFE CYCLE
    // --------------------

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(this.getActivityLayout());
        Icepick.restoreInstanceState(this, savedInstanceState);
        ButterKnife.bind(this); //Configure ButterKnife
        this.configureDesign();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    // --------------------
    // UI
    // --------------------

    protected void configureToolBar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    protected void configureUpButton(){
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);
    }

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    protected FirebaseUser getCurrentUser(){
        UserAuthViewModel userAuthViewModel = new UserAuthViewModel();
        return userAuthViewModel.getCurrentUser();
    }

    protected Boolean isCurrentUserLogged(){ return (this.getCurrentUser() != null); }
}