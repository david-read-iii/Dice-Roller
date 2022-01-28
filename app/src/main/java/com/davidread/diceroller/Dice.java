package com.davidread.diceroller;

import java.util.Random;

/**
 * {@link Dice} is a model class for a six-sided die.
 */
public class Dice {

    /**
     * Int constants for this die's largest and smallest possible value.
     */
    public static int LARGEST_NUM = 6;
    public static int SMALLEST_NUM = 1;

    /**
     * Int representing the value of this die.
     */
    private int mNumber = SMALLEST_NUM;

    /**
     * Int representing the ID of the image resource that corresponds to this die's value.
     */
    private int mImageId;

    /**
     * {@link Random} used to generate random values for this die.
     */
    private final Random mRandomGenerator;

    /**
     * Constructs a new {@link Dice}.
     *
     * @param number Value to initialize on this die.
     */
    public Dice(int number) {
        setNumber(number);
        mRandomGenerator = new Random();
    }

    /**
     * Returns the value of this die.
     */
    public int getNumber() {
        return mNumber;
    }

    /**
     * Sets the value of this die.
     */
    public void setNumber(int number) {
        if (number >= SMALLEST_NUM && number <= LARGEST_NUM) {
            mNumber = number;
            switch (number) {
                case 1:
                    mImageId = R.drawable.dice_1;
                    break;
                case 2:
                    mImageId = R.drawable.dice_2;
                    break;
                case 3:
                    mImageId = R.drawable.dice_3;
                    break;
                case 4:
                    mImageId = R.drawable.dice_4;
                    break;
                case 5:
                    mImageId = R.drawable.dice_5;
                    break;
                case 6:
                    mImageId = R.drawable.dice_6;
                    break;
            }
        }
    }

    /**
     * Returns an ID for the image resource corresponding to this die's value.
     */
    public int getImageId() {
        return mImageId;
    }

    /**
     * Increments this die's value by one.
     */
    public void addOne() {
        setNumber(mNumber + 1);
    }

    /**
     * Decrements this die's value by one.
     */
    public void subtractOne() {
        setNumber(mNumber - 1);
    }

    /**
     * Assigns a random value to this die.
     */
    public void roll() {
        setNumber(mRandomGenerator.nextInt(LARGEST_NUM) + 1);
    }
}