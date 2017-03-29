package com.honours.genar.myapplication2.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import datamanipulation.ServerDataSource;

public class CreateAccountDialog extends DialogFragment{

    public interface AccountDialogListener {
        void onDialogPositiveClick(String pass);
        void onDialogNegativeClick();
    }

    static AccountDialogListener mListener;

    ///////////////////////////////////////////////////////////////////////////////

    EditText edtEmail;
    EditText edtPassword;
    EditText edtConfirmPassword;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View v = inflater.inflate(R.layout.create_account_dialog, null);

        edtEmail = (EditText)v.findViewById(R.id.edtEmail);
        edtPassword = (EditText)v.findViewById(R.id.edtPassword);
        edtConfirmPassword = (EditText)v.findViewById(R.id.edtConfirmPassword);
        edtConfirmPassword.requestFocus();

        Bundle bundle = getArguments();

        if (!bundle.isEmpty()){

            edtEmail.setText(bundle.getString("email"));
            edtPassword.setText(bundle.getString("password"));

            if (bundle.containsKey("error")){
                edtEmail.setError(bundle.getString("error"));
                edtEmail.requestFocus();

                edtEmail.setText("");
                edtPassword.setText("");
            }
        }

        builder.setView(v)
                // Add action buttons
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CreateAccountDialog.this.getDialog().cancel();
                    }
                });

        builder.setTitle("Create Account");



        return builder.create();
    }

    @Override
    public void onStart()
    {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Boolean wantToCloseDialog = false;

                    String email = edtEmail.getText().toString();
                    String password = edtPassword.getText().toString();
                    String confirmPassword = edtConfirmPassword.getText().toString();

                    if (confirmPassword.equals(password)){
                        //TODO: Check Email format, password format before creating account

                        if (!LoginActivity.isValidEmailAddress(email)){
                            edtEmail.setError("Invalid Email Address");
                            edtEmail.requestFocus();
                            edtConfirmPassword.setText("");
                        }
                        else if (!(password.length() > 4)){
                            edtPassword.setText("");
                            edtConfirmPassword.setText("");
                            edtPassword.requestFocus();
                            edtPassword.setError("Password too short");
                        }
                        else {
                            email = edtEmail.getText().toString();
                            password = edtPassword.getText().toString();

                            ServerDataSource.createAccount(email, password);
                            wantToCloseDialog = true;
                        }
                    }
                    else{
                        //TODO: SHOW ERROR
                        edtPassword.setText("");
                        edtConfirmPassword.setText("");
                        edtPassword.requestFocus();
                        edtPassword.setError("Passwords did not match");
                    }

                    if(wantToCloseDialog)
                        dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }



}
