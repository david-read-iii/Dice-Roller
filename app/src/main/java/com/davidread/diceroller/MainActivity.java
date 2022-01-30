package com.davidread.diceroller;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

/**
 * {@link MainActivity} represents a user interface with dice that may be rolled by the user.
 * Controls to change the number of dice, to roll the dice, and stop rolling the dice are in this
 * activity's action bar. Each die may be long pressed to present a context menu of options for
 * that die. Each die may be double tapped to increment their value by one. Flinging the screen
 * rolls all dice.
 */
public class MainActivity extends AppCompatActivity implements RollLengthDialogFragment.OnRollLengthSelectedListener {

    /**
     * Int constant representing the maximum number of dice allowed to be shown on screen.
     */
    public static final int MAX_DICE = 3;

    /**
     * Int representing the number of dice visible on screen.
     */
    private int mVisibleDice;

    /**
     * Array of {@link Dice} to model dice values and random rolls.
     */
    private Dice[] mDice;

    /**
     * Array of {@link ImageView} that display the values of {@link #mDice} using image drawables
     * in the user interface.
     */
    private ImageView[] mDiceImageViews;

    /**
     * Array of {@link GestureDetectorCompat} that define how each {@link #mDiceImageViews}
     * view should respond to touch gestures.
     */
    private GestureDetectorCompat[] mDetectors;

    /**
     * Int representing the sum of the values of the dice visible on screen.
     */
    private int mSum;

    /**
     * {@link TextView} to display {@link #mSum} in the user interface.
     */
    private TextView mSumTextView;

    /**
     * {@link CountDownTimer} used to call {@link Dice#roll()} a few times a second to give a nice
     * dice roll animation.
     */
    private CountDownTimer mTimer;

    /**
     * Long indicating how many milliseconds {@link #mTimer} should loop before stopping.
     */
    private long mTimerLength = 2000;

    /**
     * Global reference to this activity's appbar {@link Menu} so it may be manipulated dynamically.
     */
    private Menu mMenu;

    /**
     * Int indicating which die has called
     * {@link #onCreateContextMenu(ContextMenu, View, ContextMenu.ContextMenuInfo)} last.
     */
    private int mCurrentDie;

    /**
     * Callback method invoked when the activity is created. It initializes member variables and
     * initializes the user interface.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The maximum number of die allowed on screen are initially visible.
        mVisibleDice = MAX_DICE;

        // Initialize mDice array.
        mDice = new Dice[MAX_DICE];
        for (int i = 0; i < MAX_DICE; i++) {
            mDice[i] = new Dice(i + 1);
        }

        // Initialize mDiceImageViews array.
        mDiceImageViews = new ImageView[MAX_DICE];
        mDiceImageViews[0] = findViewById(R.id.dice_1);
        mDiceImageViews[1] = findViewById(R.id.dice_2);
        mDiceImageViews[2] = findViewById(R.id.dice_3);

        for (int i = 0; i < mDiceImageViews.length; i++) {
            int innerI = i;

            // Register mDiceImageViews elements for context menus in this activity.
            registerForContextMenu(mDiceImageViews[innerI]);
            mDiceImageViews[innerI].setTag(innerI);

            /* Register mDiceImageViews elements for OnTouchListener objects. Each listener passes
             * all touch events to the appropriate mDetector element. */
            mDiceImageViews[innerI].setOnTouchListener((v, event) -> {
                mDetectors[innerI].onTouchEvent(event);
                return true;
            });
        }

        // Initialize mDetectors elements for each mDiceImageViews element.
        mDetectors = new GestureDetectorCompat[mDiceImageViews.length];
        for (int i = 0; i < mDetectors.length; i++) {
            int innerI = i;
            mDetectors[i] = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {

                // Add one to this Dice object if a double tap is detected on the view.
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    mDice[innerI].addOne();
                    calculateSum();
                    updateUI();
                    return super.onDoubleTap(e);
                }

                // Open a context menu for this Dice object if a long press is detected on the view.
                @Override
                public void onLongPress(MotionEvent motionEvent) {
                    openContextMenu(mDiceImageViews[innerI]);
                }

                // Roll all dice if a fling is detected on the view.
                @Override
                public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                    rollDice();
                    return false;
                }
            });
        }

        // Initialize mSum.
        calculateSum();

        // Initialize sum TextView.
        mSumTextView = findViewById(R.id.sum_text_view);

        // Initialize the user interface.
        updateUI();
    }

    /**
     * Callback method invoked when the action bar is created.
     *
     * @param menu {@link Menu} where the action bar menu should be inflated.
     * @return Whether the action bar should be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu_main, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Callback method invoked when an action bar button is selected.
     *
     * @param item {@link MenuItem} that is invoking this method.
     * @return False to allow normal processing to proceed. True to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // When "One" is selected, only show one die on screen.
        if (item.getItemId() == R.id.action_one) {
            changeDiceVisibility(1);
            calculateSum();
            updateUI();
            return true;
        }

        // When "Two" is selected, only show two dice on screen.
        else if (item.getItemId() == R.id.action_two) {
            changeDiceVisibility(2);
            calculateSum();
            updateUI();
            return true;
        }

        // When "Three" is selected, only show three dice on screen.
        else if (item.getItemId() == R.id.action_three) {
            changeDiceVisibility(3);
            calculateSum();
            updateUI();
            return true;
        }

        // When "Stop" is selected, stop rolling dice and hide this action bar button.
        else if (item.getItemId() == R.id.action_stop) {
            mTimer.cancel();
            item.setVisible(false);
            mMenu.findItem(R.id.action_roll).setVisible(true);
            return true;
        }

        // When "Roll" is selected, roll the dice.
        else if (item.getItemId() == R.id.action_roll) {
            rollDice();
            return true;
        }

        // When "Roll Length" is selected, show a dialog picker for roll length.
        else if (item.getItemId() == R.id.action_roll_length) {
            RollLengthDialogFragment dialog = new RollLengthDialogFragment();
            dialog.show(getSupportFragmentManager(), "rollLengthDialog");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method invoked when a context menu is built in this activity. It updates
     * {@link #mCurrentDie} and inflates a context menu.
     *
     * @param menu     The context menu being built.
     * @param v        The view for which the context menu is being built.
     * @param menuInfo Extra information about the item for which the context menu should be shown.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        mCurrentDie = (int) v.getTag();
        menu.setHeaderTitle(getString(R.string.die_options_dialog_label, mCurrentDie + 1));
        getMenuInflater().inflate(R.menu.context_menu_main, menu);
    }

    /**
     * Callback method invoked when an item in a context menu is selected in this activity.
     *
     * @param item The context menu item being selected.
     * @return False to allow normal context menu processing to proceed. True to consume it here.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        // When "Add one" is selected, add one to the appropriate die and update the UI.
        if (item.getItemId() == R.id.action_add_one) {
            mDice[mCurrentDie].addOne();
            calculateSum();
            updateUI();
            return true;
        }

        // When "Subtract one" is selected, subtract one from the appropriate die and update the UI.
        else if (item.getItemId() == R.id.action_subtract_one) {
            mDice[mCurrentDie].subtractOne();
            calculateSum();
            updateUI();
            return true;
        }

        // When "Roll" is selected, roll all dice.
        else if (item.getItemId() == R.id.action_roll_single) {
            rollDie(mCurrentDie);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    /**
     * Callback method invoked when a length is selected by a {@link RollLengthDialogFragment}. It
     * simply assigns the selected length to {@link #mTimer}.
     *
     * @param which Int representing which length was selected by {@link RollLengthDialogFragment}.
     */
    @Override
    public void onRollLengthClick(int which) {
        mTimerLength = 1000 * (which + 1);
    }

    /**
     * Changes the number of dice shown on screen to the passed argument.
     *
     * @param numVisible The number of dice to show on screen.
     */
    private void changeDiceVisibility(int numVisible) {

        // Update mVisibleDice.
        mVisibleDice = numVisible;

        // Make dice visible.
        for (int i = 0; i < numVisible; i++) {
            mDiceImageViews[i].setVisibility(View.VISIBLE);
        }

        // Hide remaining dice.
        for (int i = numVisible; i < MAX_DICE; i++) {
            mDiceImageViews[i].setVisibility(View.GONE);
        }
    }

    /**
     * Rolls the appropriate die on screen with a nice animation.
     *
     * @param which Which {@link Dice} object to call {@link Dice#roll()} on.
     */
    private void rollDie(int which) {

        // Show "Stop" and hide "Roll" action bar buttons.
        mMenu.findItem(R.id.action_stop).setVisible(true);
        mMenu.findItem(R.id.action_roll).setVisible(false);

        // Stop mTimer if it is already running.
        if (mTimer != null) {
            mTimer.cancel();
        }

        // Initialize mTimer to call roll() on all mDice elements repeatedly.
        mTimer = new CountDownTimer(mTimerLength, 100) {

            /* A few times a second, call roll() on the appropriate mDice element, calculate the
             * sum, and update the user interface. */
            public void onTick(long millisUntilFinished) {
                mDice[which].roll();
                calculateSum();
                updateUI();
            }

            /* When mTimer is finished, hide "Stop" and show "Roll" action bar buttons, and check
             * for winning and losing conditions. */
            public void onFinish() {
                mMenu.findItem(R.id.action_stop).setVisible(false);
                mMenu.findItem(R.id.action_roll).setVisible(true);
                checkForWinConditions();
                checkForLoseConditions();
            }
        }.start();
    }

    /**
     * Rolls all dice on screen with a nice animation.
     */
    private void rollDice() {

        // Show "Stop" and hide "Roll" action bar buttons.
        mMenu.findItem(R.id.action_stop).setVisible(true);
        mMenu.findItem(R.id.action_roll).setVisible(false);

        // Stop mTimer if it is already running.
        if (mTimer != null) {
            mTimer.cancel();
        }

        // Initialize mTimer to call roll() on all mDice elements repeatedly.
        mTimer = new CountDownTimer(mTimerLength, 100) {

            /* A few times a second, call roll() on all mDice elements, calculate the sum, and
             * update the user interface. */
            public void onTick(long millisUntilFinished) {
                for (int i = 0; i < mVisibleDice; i++) {
                    mDice[i].roll();
                }
                calculateSum();
                updateUI();
            }

            /* When mTimer is finished, hide "Stop" and show "Roll" action bar buttons, and check
             * for winning and losing conditions. */
            public void onFinish() {
                mMenu.findItem(R.id.action_stop).setVisible(false);
                mMenu.findItem(R.id.action_roll).setVisible(true);
                checkForWinConditions();
                checkForLoseConditions();
            }
        }.start();
    }

    /**
     * Calculate the sum of the values of {@link #mDice} that are currently visible on screen. This
     * value is put in {@link #mSum}.
     */
    private void calculateSum() {
        mSum = 0;
        for (int i = 0; i < mVisibleDice; i++) {
            mSum += mDice[i].getNumber();
        }
    }

    /**
     * Updates the user interface of this activity to match the logic of {@link #mDice} and
     * {@link #mSum}.
     */
    private void updateUI() {

        /* Update mDiceImageViews elements to have image drawables and descriptions that match their
         * corresponding models in mDice. */
        for (int i = 0; i < mVisibleDice; i++) {
            Drawable diceDrawable = ContextCompat.getDrawable(this, mDice[i].getImageId());
            mDiceImageViews[i].setImageDrawable(diceDrawable);
            mDiceImageViews[i].setContentDescription(Integer.toString(mDice[i].getNumber()));
        }

        // Update mSumTextView to match its mSum model.
        mSumTextView.setText(getString(R.string.sum_label, mSum));
    }

    /**
     * Checks {@link #mSum} for winning conditions. If one is detected, a {@link Snackbar} is popped
     * on screen.
     */
    private void checkForWinConditions() {
        if ((mVisibleDice == 2) && (mSum == 7 || mSum == 11)) {
            Snackbar.make(mSumTextView, R.string.win_message, BaseTransientBottomBar.LENGTH_SHORT).show();
        } else if ((mVisibleDice == 3) && (mSum % 7 == 0 || mSum % 11 == 0)) {
            Snackbar.make(mSumTextView, R.string.win_message, BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks {@link #mSum} for losing conditions. If one is detected, a {@link Snackbar} is popped
     * on screen.
     */
    private void checkForLoseConditions() {
        if ((mVisibleDice == 2) && (mSum == 2 || mSum == 12)) {
            Snackbar.make(mSumTextView, R.string.lose_message, BaseTransientBottomBar.LENGTH_SHORT).show();
        } else if ((mVisibleDice == 3) && (mSum == 18 || mSum == 3)) {
            Snackbar.make(mSumTextView, R.string.lose_message, BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }
}