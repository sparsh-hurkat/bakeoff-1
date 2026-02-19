import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;

public class Main extends PApplet
{
    int margin = 200;              // margin around the squares
    final int padding = 50;        // space between buttons
    final int buttonSize = 40;     // button width/height

    ArrayList<Integer> trials = new ArrayList<Integer>();
    int trialNum = 0;
    int startTime = 0;
    int finishTime = 0;
    int hits = 0;
    int misses = 0;

    Robot robot; // initialized in setup
    int numRepeats = 1;

    // --- NEW: keyboard selection state (0..15)
    int selectedIndex = 0;

    public static void main(String[] args) {
        PApplet.main("Main");
    }

    @Override
    public void settings() {
        size(700, 700);
    }

    public void setup()
    {
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

        //===DON'T MODIFY MY RANDOM ORDERING CODE==
        for (int i = 0; i < 16; i++)
            for (int k = 0; k < numRepeats; k++)
                trials.add(i);

        Collections.shuffle(trials);
        System.out.println("trial order: " + trials);

        surface.setLocation(0,0);
    }

    public void draw()
    {
        background(0);

        if (trialNum >= trials.size())
        {
            float timeTaken = (finishTime - startTime) / 1000f;
            float penalty = constrain(((95f - ((float)hits * 100f / (float)(hits + misses))) * .2f), 0, 100);

            fill(255);
            text("Finished!", width / 2, height / 2);
            text("Hits: " + hits, width / 2, height / 2 + 20);
            text("Misses: " + misses, width / 2, height / 2 + 40);
            text("Accuracy: " + (float)hits * 100f / (float)(hits + misses) + "%", width / 2, height / 2 + 60);
            text("Total time taken: " + timeTaken + " sec", width / 2, height / 2 + 80);
            text("Average time for each button: " + nf((timeTaken)/(float)(hits + misses), 0, 3) + " sec", width / 2, height / 2 + 100);
            text("Average time for each button + penalty: " + nf(((timeTaken)/(float)(hits + misses) + penalty), 0, 3) + " sec", width / 2, height / 2 + 140);
            return;
        }

        fill(255);
        text((trialNum + 1) + " of " + trials.size(), 60, 20);
        text("Selected: " + selectedIndex + " (WASD to move, click anywhere to select)", width / 2, 20);

        // Draw buttons
        for (int i = 0; i < 16; i++)
            drawButton(i);

        // NEW: draw selection outline (on top)
        drawSelectionOutline(selectedIndex);

        // Optional: keep the red cursor indicator if you like
        fill(255, 0, 0, 200);
        ellipse(mouseX, mouseY, 20, 20);
    }

    public void mousePressed()
    {
        if (trialNum >= trials.size())
            return;

        if (trialNum == 0)
            startTime = millis();

        if (trialNum == trials.size() - 1) {
            finishTime = millis();
            System.out.println("we're all done!");
        }

        // --- NEW: clicking anywhere "clicks" the selected square
        int target = trials.get(trialNum);

        if (selectedIndex == target) {
            System.out.println("HIT! " + trialNum + " " + (millis() - startTime));
            hits++;
        } else {
            System.out.println("MISSED! " + trialNum + " " + (millis() - startTime)
                    + " (target=" + target + ", selected=" + selectedIndex + ")");
            misses++;
        }

        trialNum++;
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
            fill(0, 255, 255); // target
        else
            fill(200); // non-target

        rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    // --- NEW: selection outline
    private void drawSelectionOutline(int i)
    {
        Rectangle b = getButtonLocation(i);

        // draw an outline without affecting other shapes
        noFill();
        stroke(255, 255, 0);   // yellow
        strokeWeight(4);
        rect(b.x - 2, b.y - 2, b.width + 4, b.height + 4);

        // restore for rest of drawing
        noStroke();
        strokeWeight(1);
    }

    public void keyPressed()
    {
        // WASD movement in a 4x4 grid (clamped)
        int col = selectedIndex % 4;
        int row = selectedIndex / 4;

        char k = Character.toLowerCase(key);
        if (k == 'w') row--;
        if (k == 's') row++;
        if (k == 'a') col--;
        if (k == 'd') col++;

        // clamp to [0..3]
        col = constrain(col, 0, 3);
        row = constrain(row, 0, 3);

        selectedIndex = row * 4 + col;
    }
}