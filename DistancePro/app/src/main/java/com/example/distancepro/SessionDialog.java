package com.example.distancepro;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class SessionDialog extends AppCompatDialogFragment {
    private EditText enterSessionId;
    private Button upload;
    public String sessionID;
    private SessionDialogListener listener;

    //DBhelper DB;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //DB = new DBhelper(this);

        LayoutInflater inflater= getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog,null);
        builder.setView(view)
                .setTitle("Location")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //sessionID = enterSessionId.getText().toString();
                        //value =



                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sessionID = enterSessionId.getText().toString();
                        listener.applyTexts(sessionID);


                    }
                });
        enterSessionId = view.findViewById(R.id.sessionId);
        //upload = view.findViewById(R.id.uploadID);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (SessionDialogListener) context;
        } catch (Exception e){
            throw new ClassCastException(context.toString()+"must implement SessionDialogListener");
        }


    }

    public interface  SessionDialogListener{
        void applyTexts(String sessionId);
    }
}
