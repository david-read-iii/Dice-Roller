package com.davidread.diceroller;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * {@link RollLengthDialogFragment} represents a user interface for a roll length dialog picker.
 */
public class RollLengthDialogFragment extends DialogFragment {

    /**
     * {@link OnRollLengthSelectedListener} is an interface that defines the
     * {@link #onRollLengthClick(int)} callback method that should be invoked when a roll length
     * is clicked in this {@link RollLengthDialogFragment}.
     */
    public interface OnRollLengthSelectedListener {
        void onRollLengthClick(int which);
    }

    /**
     * A reference to the activity that shows this {@link RollLengthDialogFragment}.
     */
    private OnRollLengthSelectedListener mListener;

    /**
     * Callback method invoked when this fragment is first attached to its context. It simply
     * initializes the member variables of this class.
     *
     * @param context {@link Context} where the fragment is being attached.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (OnRollLengthSelectedListener) context;
    }

    /**
     * Callback method invoked to build a custom {@link Dialog} container. It builds an
     * {@link AlertDialog} that allows the user to pick a roll length.
     *
     * @return An {@link AlertDialog} instance to be displayed by the fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.pick_roll_length);
        builder.setItems(R.array.length_array, (dialog, which) -> {
            mListener.onRollLengthClick(which);
        });
        return builder.create();
    }
}