package com.honours.genar.myapplication2.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import datamanipulation.ServerDataSource;


public class CreateAccountDialogActivity extends ActionBarActivity {

    EditText edtEmail;
    EditText edtPassword;
    EditText edtConfirmPassword;

    String email = "";
    String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_dialog);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View v = inflater.inflate(R.layout.create_account_dialog, null);


        edtEmail = (EditText)v.findViewById(R.id.edtEmail);
        edtPassword = (EditText)v.findViewById(R.id.edtPassword);
        edtConfirmPassword = (EditText)v.findViewById(R.id.edtConfirmPassword);

        Intent i = getIntent();

        email =  i.getStringExtra("email");
        password = i.getStringExtra("password");


        edtEmail.setText(email);
        edtPassword.setText(password);

        edtConfirmPassword.requestFocus();

        builder.setView(v)
                // Add action buttons
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (edtConfirmPassword.getText().toString().equals(edtPassword.getText().toString())){
                            //TODO: Check Email format, password format before creating account

                            String email = edtEmail.getText().toString();
                            String password = edtPassword.getText().toString();

                            ServerDataSource.createAccount(email, password);

                            dialog.dismiss();
                        }
                        else{
                            //TODO: SHOW ERROR
                            edtPassword.setText("");
                            edtConfirmPassword.setText("");
                            edtPassword.requestFocus();
                            edtPassword.setError("Passwords do not match");
                            edtConfirmPassword.setError("Passwords do not match");
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onBackPressed();
                    }
                });


        builder.setTitle("Create Account");
        builder.show();

    }

    class DialogClickListener implements View.OnClickListener {
        private final Dialog dialog;
        public DialogClickListener(Dialog dialog) {
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            if (edtConfirmPassword.getText().toString().equals(edtPassword.getText().toString())){
                //TODO: Check Email format, password format before creating account

                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();

                ServerDataSource.createAccount(email,password);

                dialog.dismiss();
            }
            else{
                //TODO: SHOW ERROR
                edtPassword.setText("");
                edtConfirmPassword.setText("");
                edtPassword.requestFocus();
                edtPassword.setError("Passwords do not match");
                edtConfirmPassword.setError("Passwords do not match");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    /*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_account_dialog, menu);
        return true;
    }*/

/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
