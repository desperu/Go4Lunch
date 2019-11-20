package org.desperu.go4lunch.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;
import icepick.Icepick;

public abstract class BaseFragment extends Fragment {

    // --------------
    // BASE METHODS
    // --------------

    protected abstract int getFragmentLayout();
    protected abstract void configureDesign();

    // -----------------
    // METHODS OVERRIDE
    // -----------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayout(), container, false);
        ButterKnife.bind(this, view);
        Icepick.restoreInstanceState(this, savedInstanceState);
        this.configureDesign();
        return(view);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }
}
