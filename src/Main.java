import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;

public class Main extends PApplet
{
    int margin = 200;
    final int padding = 50;
    final int buttonSize = 40;

    ArrayList<Integer> trials = new ArrayList<Integer>();
    int trialNum = 0;
    int startTime = 0;
    int finishTime = 0;
    int hits = 0;
    int misses = 0;
    Robot robot;

    int numRepeats = 1;

    // === Enlarged cursor hitbox ===
    final int cursorRadius = 35;   // visual + hit radius (pixels)
    final int overlapStep = 2;     // sampling step (smaller = more accurate, slower)

    // === NEW: virtual cursor we clamp inside the allowed area ===
    float vMouseX, vMouseY;
    int prevRawMouseX, prevRawMouseY;

    // === NEW: clamp area (border around grid) ===
    final int clampPad = 25; // how much extra space around the 4x4 grid we allow
    Rectangle clampRect;

    public static void main(String[] args) {
        PApplet.main("Main");
    }

    @Override
    public void settings() {
        size(700,700);
    }

    public void setup()
    {
        noCursor();          // hide system cursor so our clamped virtual cursor is what you “feel”
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

        // Compute clamp rectangle around the 4x4 grid
        int gridW = 3 * (padding + buttonSize) + buttonSize;
        int gridH = 3 * (padding + buttonSize) + buttonSize;
        clampRect = new Rectangle(margin - clampPad, margin - clampPad,
                gridW + 2 * clampPad, gridH + 2 * clampPad);

        // Start virtual cursor at center of grid
        vMouseX = margin + gridW / 2f;
        vMouseY = margin + gridH / 2f;

        prevRawMouseX = mouseX;
        prevRawMouseY = mouseY;
    }

    public void draw()
    {
        background(0);

        // Update virtual cursor using real mouse deltas, then clamp inside clampRect
        int dx = mouseX - prevRawMouseX;
        int dy = mouseY - prevRawMouseY;
        prevRawMouseX = mouseX;
        prevRawMouseY = mouseY;

        vMouseX += dx;
        vMouseY += dy;

        vMouseX = constrain(vMouseX, clampRect.x, clampRect.x + clampRect.width);
        vMouseY = constrain(vMouseY, clampRect.y, clampRect.y + clampRect.height);

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
        text((trialNum + 1) + " of " + trials.size(), 60, 20);
        text("Click mouse or press 'A' to click. Cursor is clamped near grid.", width / 2, 20);

        // Draw buttons
        for (int i = 0; i < 16; i++)
            drawButton(i);

        // Draw the clamp border so it’s visible
        drawClampBorder();

        // Draw enlarged cursor at virtual cursor position
        drawBigCursor((int)vMouseX, (int)vMouseY);
    }

    public void mousePressed()
    {
        performClick();
    }

    public void keyPressed()
    {
        // Allow using the "A" key to click
        if (key == 'a' || key == 'A') {
            performClick();
        }
    }

    // Shared click logic for mouse and 'A' key
    private void performClick()
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

        // Choose button covered MOST by the cursor hitbox (using virtual cursor)
        int chosen = getMostCoveredButton((int)vMouseX, (int)vMouseY);

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

        // === Yellow Target Square (instead of blue) ===
        if (trials.get(trialNum) == i)
            fill(255, 255, 0); // yellow
        else
            fill(200);

        rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private void drawClampBorder() {
        noFill();
        stroke(255);
        strokeWeight(2);
        rect(clampRect.x, clampRect.y, clampRect.width, clampRect.height);
        noStroke();
        strokeWeight(1);
    }

    private void drawBigCursor(int cx, int cy) {
        // translucent fill showing hit area
        noStroke();
        fill(255, 0, 0, 60);
        ellipse(cx, cy, cursorRadius * 2, cursorRadius * 2);

        // ring outline
        noFill();
        stroke(255, 0, 0, 220);
        strokeWeight(3);
        ellipse(cx, cy, cursorRadius * 2, cursorRadius * 2);

        // center dot
        noStroke();
        fill(255, 0, 0, 230);
        ellipse(cx, cy, 8, 8);

        noStroke();
        strokeWeight(1);
    }

    // Returns button index 0..15, or -1 if overlap is zero for all.
    private int getMostCoveredButton(int cx, int cy) {
        int bestIdx = -1;
        int bestScore = 0;

        int r = cursorRadius;
        int r2 = r * r;

        int minX = cx - r;
        int maxX = cx + r;
        int minY = cy - r;
        int maxY = cy + r;

        for (int i = 0; i < 16; i++) {
            Rectangle b = getButtonLocation(i);

            // quick reject
            if (maxX < b.x || minX > b.x + b.width || maxY < b.y || minY > b.y + b.height)
                continue;

            int score = 0;

            // sample only overlapping bbox region
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
            } else if (score == bestScore && score > 0) {
                // Optional tie-breaker: choose the one whose center is closer to cursor center
                // (makes edge cases feel more stable)
                Rectangle best = getButtonLocation(bestIdx);
                float bestCx = best.x + best.width / 2f;
                float bestCy = best.y + best.height / 2f;
                float curBestD = sq(bestCx - cx) + sq(bestCy - cy);

                float thisCx = b.x + b.width / 2f;
                float thisCy = b.y + b.height / 2f;
                float thisD = sq(thisCx - cx) + sq(thisCy - cy);

                if (thisD < curBestD) bestIdx = i;
            }
        }

        return (bestScore == 0) ? -1 : bestIdx;
    }

    public void mouseMoved() { }
    public void mouseDragged() { }
}