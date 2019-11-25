package org.desperu.go4lunch.activities;

import android.content.Intent;
import android.widget.Toast;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.api.UserHelper;
import org.desperu.go4lunch.base.BaseActivity;

import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends BaseActivity {

    //FOR DATA
    private static final int RC_SIGN_IN = 1234;

    // --------------------
    // BASE METHODS
    // --------------------

    @Override
    protected int getActivityLayout() { return R.layout.activity_login; }

    @Override
    protected void configureDesign() { this.isAlreadyLogged(); }

    // --------------------
    // METHODS OVERRIDE
    // --------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle SignIn Activity response on activity result.
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
        this.finish(); // TODO another way??
    }

    // --------------------
    // NAVIGATION
    // --------------------

    /**
     * If already logged, start main activity, else start sign in activity.
     */
    private void isAlreadyLogged() {
        if (this.isCurrentUserLogged()) {
            startMainActivity();
            this.finish();
        }
        else startSignInActivity();
    }

    /**
     * Launch sign in activity with firesbase ui.
     */
    private void startSignInActivity(){
        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout.Builder(R.layout.activity_login)
                                .setEmailButtonId(R.id.activity_login_button_email)
                                .setGoogleButtonId(R.id.activity_login_button_google)
                                .setFacebookButtonId(R.id.activity_login_button_facebook)
                                .setTwitterButtonId(R.id.activity_login_button_twitter)
                                .build();

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(Arrays.asList(
                                // Email authentication
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                // Google authentication
                                new AuthUI.IdpConfig.GoogleBuilder().build(),
                                // Facebook authentication
                                new AuthUI.IdpConfig.FacebookBuilder().build(),
                                // Twitter authentication
                                new AuthUI.IdpConfig.TwitterBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.ic_login_logo_white)
                        .setAuthMethodPickerLayout(customLayout)
                        .build(),
                RC_SIGN_IN);
    }

    // --------------------
    // REST REQUEST
    // --------------------

    /**
     * Create user in firestore.
     */
    private void createUserInFirestore(){

        if (this.getCurrentUser() != null) {

            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ?
                    this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();

            UserHelper.createUser(uid, username, urlPicture).addOnFailureListener(this.onFailureListener());
        }
    }

    // --------------------
    // UI
    // --------------------

    /**
     * Show Toast with corresponding message.
     * @param message Message to show.
     */
    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // --------------------
    // UTILS
    // --------------------

    //TODO not good not show
    /**
     * Method that handles response after SignIn Activity close.
     * @param requestCode Code of the request.
     * @param resultCode Code result from sign in activity.
     * @param data Data from sign in activity.
     */
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                this.createUserInFirestore();
                showToast(getString(R.string.connection_succeed));
                this.startMainActivity();
            } else { // ERRORS
                if (response == null) {
                    showToast(getString(R.string.error_authentication_canceled));
                } else if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showToast(getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showToast(getString(R.string.error_unknown_error));
                }
            }
        }
    }

    // --------------------
    // ACTIVITY
    // --------------------

    /**
     * Start Main Activity.
     */
    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}