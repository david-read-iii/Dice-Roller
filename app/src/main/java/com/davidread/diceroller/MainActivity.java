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
 * {@link MainActivity} represents a user interface with three dice that may be rolled by the user.
 * Controls to change the number of dice, to roll the dice, and stop rolling the dice are in this
 * activity's action bar.
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
     * Array of {@link ImageView} that hold the dice image drawables in the user interface.
     */
    private ImageView[] mDiceImageViews;

    /**
     * Global reference to this activity's {@link Menu}.
     */
    private Menu mMenu;

    /**
     * {@link CountDownTimer} used to re-roll die a few times a second to give a dice roll
     * animation.
     */
    private CountDownTimer mTimer;

    /**
     * Int representing the sum of the values of the dice visible on screen.
     */
    private int sum;

    /**
     * {@link TextView} to hold the sum in the user interface.
     */
    private TextView sumTextView;

    /**
     * Long representing how long {@link #mTimer} should loop.
     */
    private long mTimerLength = 2000;

    /**
     * Int representing which die is currently displaying a {@link ContextMenu}.
     */
    private int mCurrentDie;

    /**
     * Array of {@link GestureDetectorCompat} that define what each {@link #mDiceImageViews}
     * element should do in response to touch gestures.
     */
    private GestureDetectorCompat[] mDetectors;

    /**
     * Callback method invoked when the activity is created. It initializes member variables and
     * initializes the user interface.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

            // Register mDiceImageViews elements for context menus.
            registerForContextMenu(mDiceImageViews[innerI]);
            mDiceImageViews[innerI].setTag(innerI);

            /* Register mDiceImageViews elements for OnTouchListeners. Each listener passes each
             * touch event to the appropriate mDetectors element. */
            mDiceImageViews[innerI].setOnTouchListener((v, event) -> {
                mDetectors[innerI].onTouchEvent(event);
                return true;
            });
        }

        // Initialize mDetectors for each element in mDiceImageViews.
        mDetectors = new GestureDetectorCompat[mDiceImageViews.length];
        for (int i = 0; i < mDetectors.length; i++) {
            int innerI = i;
            mDetectors[i] = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {

                // Add one to the die if a double tap is detected on the view.
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    mDice[innerI].addOne();
                    updateUI();
                    return super.onDoubleTap(e);
                }

                // Open a context menu if a long press is detected on the view.
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

        // Set mVisibleDice to MAX_DICE since all dice are initially visible.
        mVisibleDice = MAX_DICE;

        // Initialize sum TextView.
        sumTextView = findViewById(R.id.sum_text_view);

        // Call updateUI() to initialize user interface.
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
            updateUI();
            return true;
        }

        // When "Two" is selected, only show two dice on screen.
        else if (item.getItemId() == R.id.action_two) {
            changeDiceVisibility(2);
            updateUI();
            return true;
        }

        // When "Three" is selected, only show three dice on screen.
        else if (item.getItemId() == R.id.action_three) {
            changeDiceVisibility(3);
            updateUI();
            return true;
        }

        // When "Stop" is selected, stop rolling dice and hide this action bar button.
        else if (item.getItemId() == R.id.action_stop) {
            mTimer.cancel();
            item.setVisible(false);
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
        if (item.getItemId() == R.id.add_one) {
            mDice[mCurrentDie].addOne();
            updateUI();
            return true;
        }

        // When "Subtract one" is selected, subtract one from the appropriate die and update the UI.
        else if (item.getItemId() == R.id.subtract_one) {
            mDice[mCurrentDie].subtractOne();
            updateUI();
            return true;
        }

        // When "Roll" is selected, roll all dice.
        else if (item.getItemId() == R.id.roll) {
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

        // Show "Stop" action bar button.
        mMenu.findItem(R.id.action_stop).setVisible(true);

        // Stop mTimer if it is already running.
        if (mTimer != null) {
            mTimer.cancel();
        }

        // Initialize mTimer to call roll() on the appropriate Dice object to display a dice roll animation.
        mTimer = new CountDownTimer(mTimerLength, 100) {
            public void onTick(long millisUntilFinished) {
                mDice[which].roll();
                updateUI();
            }

            public void onFinish() {
                mMenu.findItem(R.id.action_stop).setVisible(false);
                checkForWinConditions();
                checkForLoseConditions();
            }
        }.start();
    }

    /**
     * Rolls all dice on screen with a nice animation.
     */
    private void rollDice() {

        // Show "Stop" action bar button.
        mMenu.findItem(R.id.action_stop).setVisible(true);

        // Stop mTimer if it is already running.
        if (mTimer != null) {
            mTimer.cancel();
        }

        // Initialize mTimer to call roll() on all Dice objects to display a dice roll animation.
        mTimer = new CountDownTimer(mTimerLength, 100) {
            public void onTick(long millisUntilFinished) {
                for (int i = 0; i < mVisibleDice; i++) {
                    mDice[i].roll();
                }
                updateUI();
            }

            public void onFinish() {
                mMenu.findItem(R.id.action_stop).setVisible(false);
                checkForWinConditions();
                checkForLoseConditions();
            }
        }.start();
    }

    /**
     * Updates the user of interface of this activity to match the logic of {@link #mDice}.
     */
    private void updateUI() {

        // Display only the number of dice visible.
        for (int i = 0; i < mVisibleDice; i++) {
            Drawable diceDrawable = ContextCompat.getDrawable(this, mDice[i].getImageId());
            mDiceImageViews[i].setImageDrawable(diceDrawable);
            mDiceImageViews[i].setContentDescription(Integer.toString(mDice[i].getNumber()));
        }

        // Display the sum.
        sum = 0;
        switch (mVisibleDice) {
            case 1:
                sum = mDice[0].getNumber();
                break;
            case 2:
                sum = mDice[0].getNumber() + mDice[1].getNumber();
                break;
            case 3:
                sum = mDice[0].getNumber() + mDice[1].getNumber() + mDice[2].getNumber();
                break;
        }
        sumTextView.setText(getString(R.string.sum_label, sum));
    }

    /**
     * Checks {@link #sum} for winning conditions. If one is detected, a {@link Snackbar} is popped
     * on screen.
     */
    private void checkForWinConditions() {
        if ((mVisibleDice == 2) && (sum == 7 || sum == 11)) {
            Snackbar.make(sumTextView, R.string.win_message, BaseTransientBottomBar.LENGTH_SHORT).show();
        } else if ((mVisibleDice == 3) && (sum % 7 == 0 || sum % 11 == 0)) {
            Snackbar.make(sumTextView, R.string.win_message, BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks {@link #sum} for losing conditions. If one is detected, a {@link Snackbar} is popped
     * on screen.
     */
    private void checkForLoseConditions() {
        if ((mVisibleDice == 2) && (sum == 2 || sum == 12)) {
            Snackbar.make(sumTextView, R.string.lose_message, BaseTransientBottomBar.LENGTH_SHORT).show();
        } else if ((mVisibleDice == 3) && (sum == 18 || sum == 3)) {
            Snackbar.make(sumTextView, R.string.lose_message, BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }
}