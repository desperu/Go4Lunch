package org.desperu.go4lunch.base;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseUser;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.viewmodel.UserAuthViewModel;
import org.jetbrains.annotations.NotNull;

import butterknife.ButterKnife;

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
        ButterKnife.bind(this); //Configure ButterKnife
        this.configureDesign();
    }

    // --------------------
    // ERROR HANDLER
    // --------------------

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
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

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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