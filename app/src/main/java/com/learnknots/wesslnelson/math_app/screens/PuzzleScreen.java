package com.learnknots.wesslnelson.math_app.screens;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;

import com.learnknots.wesslnelson.math_app.Draw;
import com.learnknots.wesslnelson.math_app.GameMath;
import com.learnknots.wesslnelson.math_app.R;
import com.learnknots.wesslnelson.math_app.model.CircleHole;
import com.learnknots.wesslnelson.math_app.model.Coin;
import com.learnknots.wesslnelson.math_app.model.DiceManager;
import com.learnknots.wesslnelson.math_app.model.Die;
import com.learnknots.wesslnelson.math_app.model.SquareHole;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wesslnelson on 6/18/16.
 */
public class PuzzleScreen {

    private static final String TAG = PuzzleScreen.class.getSimpleName();

    private Draw draw;                      // used for easy access to drawing functions
    private CircleHole firstCHole;          // a single hole, will be deprecated
    private List<Die> diceList;             // a list of dice
    private List<Coin> coinList;            // a list of coins
    private List<SquareHole> sHoleList;     // a list of square holes
    private DiceManager diceManager;        // a class with useful functions for managing dice
    private GameMath gameMath;              // a class with math related functions
    private Context context;                // a context so that resources can be referenced

    private int goal;                       // the number the player wants to get close to
    private int result;                     // the result of the die and operator combo
    private int closestPossible;            // the closest possible result based off of whats given

    private Boolean isCarrying;             // true if something is being carried/touched
    private Boolean hasWon;                 // true if player has entered a winning solution

    public PuzzleScreen( Context context) {
        this.context = context;
        isCarrying = false;
        hasWon = false;
        gameMath = new GameMath();
        diceManager = new DiceManager(context);
        result = -9999;
        goal = gameMath.rndInt(1,15);

        draw = new Draw();

        diceList = makeDice();
        coinList = makeCoins();

        sHoleList = makeSHoles();


        firstCHole = new CircleHole( BitmapFactory.decodeResource(context.getResources(),
                R.drawable.circle_hole), 200, 500);


        //just trying something out
        //List<Coin> cList = new ArrayList<Coin>();
        String[] ops = {"+", "-", "*"};
        //int[] diceNums = {1,3,5};
        int[] diceNums = diceManager.getDiceNumbers(diceList);
        closestPossible  = gameMath.getClosestSolution(ops, diceNums, goal );
        Log.d(TAG, Integer.toString(closestPossible));
    }

    public void render(Canvas canvas) {
        // draw holes first, otherwise dice would be under the hole
        for (SquareHole shole: sHoleList) {
            shole.render(canvas);
        }
        firstCHole.render(canvas);



        for (Coin coin: coinList) {
            coin.render(canvas);
        }
        for (Die die: diceList) {
            die.render(canvas);
        }




        // just for some looksie  ie not permanent
        if (sHoleList.get(0).hasMessage()) {
            draw.displayText(canvas, sHoleList.get(0).getContainedMessage(), 50, 200);
        }
        if (firstCHole.hasMessage()) {
            draw.displayText(canvas, firstCHole.getContainedMessage(), 50, 210);
        }
        if (sHoleList.get(1).hasMessage()) {
            draw.displayText(canvas, sHoleList.get(1).getContainedMessage(), 50, 220);
        }
        if (result != -9999 ) {
            draw.displayText(canvas, "=", 50, 230);
            draw.displayText(canvas, Integer.toString(result), 50, 240);
        }

        draw.displayText(canvas, Integer.toString(goal), 50, 280);

        if (hasWon) {
            draw.displayTextbyWidth(canvas, "WINNER", 125, 350, 250);
        }
    }

    public void update() {

        // sets a hole to empty if necessary
        for (SquareHole shole: sHoleList) {

            int numDiceInHole = 0;

            for (Die die : diceList) {
                if (shole.dieOverlaps(die)) {
                    numDiceInHole += 1;
                }
            }
            if (numDiceInHole == 0) { // means no dice are in it
                shole.setEmpty(true);
                shole.setContainedMessage(null);
            }
        }
        int numCoinsInHole = 0;
        for (Coin coin : coinList) {
            if (firstCHole.coinOverlaps(coin)) {
                numCoinsInHole += 1;
            }
        }
        if (numCoinsInHole == 0) { // means no coins in it
            firstCHole.setEmpty(true);
            firstCHole.setContainedMessage(null);
        }

        //calculates the result, only works if all holes are full
        if (areAllHolesFilled()) {
            int num1 = Integer.parseInt(sHoleList.get(0).getContainedMessage());
            int num2 = Integer.parseInt(sHoleList.get(1).getContainedMessage());
            // figure out the result of the first bin op
            result = gameMath.doBinaryOperation(firstCHole.getContainedMessage(), num1, num2);
        }

        if (Math.abs(goal-result) == closestPossible) {
            hasWon = true;
        }
    }
    

    public void onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (Die die : diceList) {
                if (!isCarrying) {
                    die.handleActionDown((int) event.getX(), (int) event.getY());
                    if (die.isTouched()) {
                        isCarrying = true;
                    }
                }
            }
            for (Coin coin : coinList) {
                if (!isCarrying) {
                    coin.handleActionDown((int) event.getX(), (int) event.getY());
                    if (coin.isTouched()) {
                        isCarrying = true;
                    }
                }
            }

        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            for (Coin coin: coinList) {
                if (coin.isTouched()) {
                    coin.setCenterCoord((int) event.getX(), (int) event.getY());
                }
            }

            for (Die die: diceList) {
                if (die.isTouched()) {
                    die.setCenter((int)event.getX(), (int)event.getY());
                }
            }

        } if (event.getAction() == MotionEvent.ACTION_UP) {
            // touch was released
            for (Coin coin: coinList) {
                if (coin.isTouched()) {
                    coin.setTouched(false);
                    firstCHole.snapIfClose(coin);
                }
            }
            for (Die die: diceList) {
                if (die.isTouched()) {
                    die.setTouched(false);
                    for (SquareHole shole : sHoleList) {
                        shole.snapIfClose(die);
                    }
                }
            }

            isCarrying = false;

        }
    }


    private List<Die> makeDice() {
        List<Die> dList = new ArrayList<Die>();
        dList.add(diceManager.getRandomDie());
        dList.add(diceManager.getRandomDie());
        dList.add(diceManager.getRandomDie());
        dList.get(0).setCenter(100,100);
        dList.get(1).setCenter(200,100);
        dList.get(2).setCenter(300,100);

        return dList;

    }

    private List<Coin> makeCoins() {
        List<Coin> cList = new ArrayList<Coin>();
        cList.add( new Coin(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.coin_plus), 100,200, "+") );
        cList.add( new Coin(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.coin_mult), 200,200, "*"));
        cList.add( new Coin(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.coin_minus), 300,200, "-"));

        return cList;
    }

    private List<SquareHole> makeSHoles() {
        List<SquareHole> shList = new ArrayList<SquareHole>();
        shList.add(new SquareHole( BitmapFactory.decodeResource(context.getResources(),
                R.drawable.square_hole64), 100, 500));
        shList.add(new SquareHole( BitmapFactory.decodeResource(context.getResources(),
                R.drawable.square_hole64), 300, 500));

        return shList;
    }

    // returns true if all holes have an object in it
    private boolean areAllHolesFilled() {
        // this counter will be added to whenever a hole is empty
        // if at the end of everything its still 0 then all holes are filled
        int countEmpty = 0;
        for (SquareHole squareHole: sHoleList) {
            if (squareHole.isEmpty()) {
                countEmpty += 1;
            }
        }
        if (firstCHole.isEmpty()) {
            countEmpty += 1;
        }

        if (countEmpty == 0) {
            return true;
        } else {
            return false;
        }
    }




}
