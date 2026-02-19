import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;

public class Main extends PApplet
{
    int margin = 200; //set the margin around the squares
    final int padding = 50; // padding between buttons and also their width/height
    final int buttonSize = 40; // padding between buttons and also their width/height
    ArrayList<Integer> trials = new ArrayList<Integer>(); //contains the order of buttons that activate in the test
    int trialNum = 0; //the current trial number (indexes into trials array above)
    int startTime = 0; // time starts when the first click is captured
    int finishTime = 0; //records the time of the final click
    int hits = 0; //number of successful clicks
    int misses = 0; //number of missed clicks
    Robot robot; //initialized in setup

    int numRepeats = 1; //sets the number of times each button repeats in the test

    // === NEW: cache native drawing component so we can convert to screen coords ===
    java.awt.Component nativeCanvas;

    // === NEW: center of grid in sketch coordinates ===
    int gridW, gridH;
    int gridCenterX, gridCenterY;

    public static void main(String[] args) {
        PApplet.main("Main");
    }

    @Override
    public void settings() {
        size(700,700); // set the size of the window
    }

    public void setup()
    {
        //noCursor(); // keep default behavior: system cursor visible
        noStroke();
        textFont(createFont("Arial",16));
        textAlign(CENTER);
        frameRate(60);
        ellipseMode(CENTER);

        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        // Cache native canvas (Processing AWT uses a Canvas/Component)
        try {
            nativeCanvas = (java.awt.Component) surface.getNative();
        } catch (Exception e) {
            nativeCanvas = null;
            System.out.println("Warning: could not access native canvas: " + e);
        }

        // Compute grid center (sketch coords)
        gridW = 3 * (padding + buttonSize) + buttonSize;
        gridH = 3 * (padding + buttonSize) + buttonSize;
        gridCenterX = margin + gridW / 2;
        gridCenterY = margin + gridH / 2;

        //===DON'T MODIFY MY RANDOM ORDERING CODE==
        for (int i = 0; i < 16; i++) //generate list of targets and randomize the order
            for (int k = 0; k < numRepeats; k++)
                trials.add(i);

        Collections.shuffle(trials); // randomize the order of the buttons
        System.out.println("trial order: " + trials); //print out order for reference

        surface.setLocation(0,0);// put window in top left corner of screen (doesn't always work)
    }

    public void draw()
    {
        background(0);

        if (trialNum >= trials.size())
        {
            float timeTaken = (finishTime-startTime) / 1000f;
            float penalty = constrain(((95f-((float)hits*100f/(float)(hits+misses)))*.2f),0,100);
            fill(255);
            text("Finished!", width / 2, height / 2);
            text("Hits: " + hits, width / 2, height / 2 + 20);
            text("Misses: " + misses, width / 2, height / 2 + 40);
            text("Accuracy: " + (float)hits*100f/(float)(hits+misses) +"%", width / 2, height / 2 + 60);
            text("Total time taken: " + timeTaken + " sec", width / 2, height / 2 + 80);
            text("Average time for each button: " + nf((timeTaken)/(float)(hits+misses),0,3) + " sec", width / 2, height / 2 + 100);
            text("Average time for each button + penalty: " + nf(((timeTaken)/(float)(hits+misses) + penalty),0,3) + " sec", width / 2, height / 2 + 140);
            return;
        }

        fill(255);
        text((trialNum + 1) + " of " + trials.size(), 40, 20);

        for (int i = 0; i < 16; i++)
            drawButton(i);

        // default cursor indicator (scaffold)
        fill(255, 0, 0, 200);
        ellipse(mouseX, mouseY, 20, 20);
    }

    public void mousePressed()
    {
        if (trialNum >= trials.size())
            return;

        if (trialNum == 0)
            startTime = millis();

        if (trialNum == trials.size() - 1)
        {
            finishTime = millis();
            System.out.println("we're all done!");
        }

        Rectangle bounds = getButtonLocation(trials.get(trialNum));

        if ((mouseX > bounds.x && mouseX < bounds.x + bounds.width) &&
                (mouseY > bounds.y && mouseY < bounds.y + bounds.height))
        {
            System.out.println("HIT! " + trialNum + " " + (millis() - startTime));
            hits++;
        } else
        {
            System.out.println("MISSED! " + trialNum + " " + (millis() - startTime));
            misses++;
        }

        trialNum++;

        // === NEW: reset OS cursor to center of grid after each click ===
        resetMouseToGridCenter();
    }

    // Move OS cursor to the grid center (Robot uses SCREEN coords)
    private void resetMouseToGridCenter() {
        if (robot == null || nativeCanvas == null) return;

        try {
            java.awt.Point p = nativeCanvas.getLocationOnScreen(); // screen coords of sketch (0,0)
            int screenX = p.x + gridCenterX;
            int screenY = p.y + gridCenterY;
            robot.mouseMove(screenX, screenY);
        } catch (Exception e) {
            System.out.println("Cursor reset failed: " + e);
        }
    }

    public Rectangle getButtonLocation(int i)
    {
        int x = (i % 4) * (padding + buttonSize) + margin;
        int y = (i / 4) * (padding + buttonSize) + margin;
        return new Rectangle(x, y, buttonSize, buttonSize);
    }

    public void drawButton(int i)
    {
        Rectangle bounds = getButtonLocation(i);

        if (trials.get(trialNum) == i)
            fill(0, 255, 255);
        else
            fill(200);

        rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void mouseMoved() { }
    public void mouseDragged() { }
    public void keyPressed() { }
}