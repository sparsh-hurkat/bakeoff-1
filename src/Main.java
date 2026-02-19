import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;

public class Main extends PApplet
{
    //when in doubt, consult the Processsing reference: https://processing.org/reference/

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

    // === NEW: larger cursor hitbox ===
    final int cursorRadius = 35;      // visual + hit radius (pixels)
    final int overlapStep = 2;        // sampling step (1 = more accurate, 2 = faster; 2 is plenty here)

    public static void main(String[] args) {
        PApplet.main("Main");
    }

    @Override
    public void settings() {
        size(700,700); // set the size of the window
    }

    public void setup()
    {
        //noCursor(); // hides the system cursor if you want
        noStroke(); //turn off all strokes, we're just using fills here (can change this if you want)
        textFont(createFont("Arial",16)); //sets the font to Arial size 16
        textAlign(CENTER);
        frameRate(60); //normally you can't go much higher than 60 FPS.
        ellipseMode(CENTER); //ellipses are drawn from the center (BUT RECTANGLES ARE NOT!)
        //rectMode(CENTER); //enabling will break the scaffold code, but you might find it easier to work with centered rects

        try {
            robot = new Robot(); //create a "Java Robot" class that can move the system cursor
        } catch (AWTException e) {
            e.printStackTrace();
        }

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
        background(0); //set background to black

        if (trialNum >= trials.size()) //check to see if test is over
        {
            float timeTaken = (finishTime-startTime) / 1000f;
            float penalty = constrain(((95f-((float)hits*100f/(float)(hits+misses)))*.2f),0,100);
            fill(255); //set fill color to white
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

        // === NEW: bigger cursor + visible hitbox ===
        drawBigCursor(mouseX, mouseY);
    }

    public void mousePressed() // test to see if hit was in target!
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

        // === NEW: choose the button covered MOST by the cursor hitbox ===
        int chosen = getMostCoveredButton(mouseX, mouseY);

        // If cursor doesn't overlap any button at all, count as a miss
        if (chosen == -1) {
            System.out.println("MISSED! (no overlap) " + trialNum + " " + (millis() - startTime));
            misses++;
            trialNum++;
            return;
        }

        int target = trials.get(trialNum);

        if (chosen == target)
        {
            System.out.println("HIT! (chosen=" + chosen + ") " + trialNum + " " + (millis() - startTime));
            hits++;
        } else
        {
            System.out.println("MISSED! (chosen=" + chosen + ", target=" + target + ") " + trialNum + " " + (millis() - startTime));
            misses++;
        }

        trialNum++;
        //robot.mouseMove(width/2, (height)/2); // optional
    }

    //probably shouldn't have to edit this method
    public Rectangle getButtonLocation(int i) //for a given button ID, what is its location and size
    {
        int x = (i % 4) * (padding + buttonSize) + margin;
        int y = (i / 4) * (padding + buttonSize) + margin;

        return new Rectangle(x, y, buttonSize, buttonSize);
    }

    //you can edit this method to change how buttons appear
    public void drawButton(int i)
    {
        Rectangle bounds = getButtonLocation(i);

        if (trials.get(trialNum) == i)
            fill(0, 255, 255);
        else
            fill(200);

        rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    // === NEW: draw a large cursor hitbox (circle + outline)
    private void drawBigCursor(int cx, int cy) {
        // translucent fill to show the hit area
        noStroke();
        fill(255, 0, 0, 60);
        ellipse(cx, cy, cursorRadius * 2, cursorRadius * 2);

        // ring outline
        noFill();
        stroke(255, 0, 0, 200);
        strokeWeight(3);
        ellipse(cx, cy, cursorRadius * 2, cursorRadius * 2);

        // small center dot
        noStroke();
        fill(255, 0, 0, 220);
        ellipse(cx, cy, 8, 8);

        // restore for other drawing
        noStroke();
        strokeWeight(1);
    }

    // === NEW: find which button has the largest overlap with the cursor circle
    // Returns button index 0..15, or -1 if overlap is zero for all.
    private int getMostCoveredButton(int cx, int cy) {
        int bestIdx = -1;
        int bestScore = 0;

        // sample points in cursor bounding box
        int r = cursorRadius;
        int r2 = r * r;

        int minX = cx - r;
        int maxX = cx + r;
        int minY = cy - r;
        int maxY = cy + r;

        for (int i = 0; i < 16; i++) {
            Rectangle b = getButtonLocation(i);

            // quick reject: circle bbox doesn't intersect rect => can't overlap
            if (maxX < b.x || minX > b.x + b.width || maxY < b.y || minY > b.y + b.height)
                continue;

            int score = 0;

            // sample only the overlapping region between circle bbox and rect bbox
            int sx0 = max(minX, b.x);
            int sx1 = min(maxX, b.x + b.width);
            int sy0 = max(minY, b.y);
            int sy1 = min(maxY, b.y + b.height);

            for (int x = sx0; x <= sx1; x += overlapStep) {
                int dx = x - cx;
                int dx2 = dx * dx;
                for (int y = sy0; y <= sy1; y += overlapStep) {
                    int dy = y - cy;
                    if (dx2 + dy * dy <= r2) {
                        score++;
                    }
                }
            }

            if (score > bestScore) {
                bestScore = score;
                bestIdx = i;
            }
        }

        // if nothing overlapped at all
        if (bestScore == 0) return -1;

        return bestIdx;
    }

    public void mouseMoved() { }
    public void mouseDragged() { }
    public void keyPressed() { }
}