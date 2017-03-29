package com.honours.genar.myapplication2.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import datamanipulation.ServerDataSource;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements  ServerDataSource.LoginListener {

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check initialise the location manager
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check whether the GPS provider is enabled
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // If not enabled, then we need to display a dialog leading user to turn on GPS
            buildAlertMessageNoGps();
        }
        else {
            String email = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("email", null);
            String password = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("password", null);

            if (email != null && password != null) {
                Account acc = new Account(email, password);
                acc.setRemember(true);
                ARData.setCurrentAccount(acc);
                accountCreated(true);
            }

            // Set up the login form.
            mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

            mPasswordView = (EditText) findViewById(R.id.password);
            mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == R.id.login || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                    return false;
                }
            });

            Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
            mEmailSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });

            mLoginFormView = findViewById(R.id.login_form);
            mProgressView = findViewById(R.id.login_progress);

            ServerDataSource.applicationConext = getApplicationContext();
            ServerDataSource.loginListener = this;
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            ServerDataSource.verifyAccount(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return isValidEmailAddress(email);
    }

    private boolean isPasswordValid(String password) {
        //TODO: Create Better Password Check
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void userAuthentication(Boolean authentic, Boolean credError) {
        showProgress(false);
        if (authentic) {
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
            finish();
        } else if (credError) {
            mPasswordView.setError("Incorrect Password");
            mPasswordView.setText("");
            mPasswordView.requestFocus();
        } else {

            new AlertDialog.Builder(this)
                    .setTitle("Details not found")
                    .setMessage("Do you want to create an account?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            CreateAccountDialog cad = new CreateAccountDialog();
                            Bundle b = new Bundle();
                            b.putString("email", mEmailView.getText().toString());
                            b.putString("password", mPasswordView.getText().toString());
                            cad.setArguments(b);
                            cad.show(getFragmentManager(), "CreateAccount");
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .show();
        }
    }

    @Override
    public void accountCreated(Boolean success) {
        if (success) {
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
            finish();
        }
        else{
            CreateAccountDialog cad = new CreateAccountDialog();
            Bundle b = new Bundle();
            b.putString("email", mEmailView.getText().toString());
            b.putString("password", mPasswordView.getText().toString());
            b.putString("error","Email already exists");
            cad.setArguments(b);
            cad.show(getFragmentManager(), "CreateAccount");
        }
    }

    // Method to validate email address
    public static boolean isValidEmailAddress(String email) {

        boolean stricterFilter = true;
        String stricterFilterString = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
        String laxString = ".+@.+\\.[A-Za-z]{2}[A-Za-z]*";
        String emailRegex = stricterFilter ? stricterFilterString : laxString;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(emailRegex);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();

    }

    /**
     * This method is used to display a prompting dialog, providing the user with the option of enabling GPS location services
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("No GPS detected. Activate GPS in local settings?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

}
