package com.honours.genar.myapplication2.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class AccessDialog extends DialogFragment{

    public interface AccessDialogListener {
        void onDialogPositiveClick(String pass);
        void onDialogNegativeClick();
    }

    static AccessDialogListener mListener;

    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View v = inflater.inflate(R.layout.access_dialog, null);

        builder.setView(v)
                // Add action buttons
                .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText edtPass = (EditText)v.findViewById(R.id.edtPass);
                        mListener.onDialogPositiveClick(edtPass.getText().toString());

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AccessDialog.this.getDialog().cancel();
                        mListener.onDialogNegativeClick();
                    }
                });
        builder.setCancelable(false);
        return builder.create();
    }

}
