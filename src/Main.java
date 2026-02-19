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
    final int cursorRadius = 35;
    final int overlapStep = 2;

    // === Virtual cursor clamped inside area ===
    float vMouseX, vMouseY;
    int prevRawMouseX, prevRawMouseY;

    // === Clamp area around grid ===
    final int clampPad = 25;
    Rectangle clampRect;

    // === Halo styling (does NOT change button hit size) ===
    final int haloPad = 10;        // how far halo extends beyond the square visually
    final int haloStroke = 6;      // thickness of the halo outline
    final int haloAlpha = 180;     // transparency of halo

    public static void main(String[] args) {
        PApplet.main("Main");
    }

    @Override
    public void settings() {
        size(700,700);
    }

    public void setup()
    {
        noCursor();
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

        //=== RANDOM ORDERING CODE ===
        for (int i = 0; i < 16; i++)
            for (int k = 0; k < numRepeats; k++)
                trials.add(i);

        Collections.shuffle(trials);
        System.out.println("trial order: " + trials);

        surface.setLocation(0,0);

        // Compute clamp rectangle around 4x4 grid
        int gridW = 3 * (padding + buttonSize) + buttonSize;
        int gridH = 3 * (padding + buttonSize) + buttonSize;

        clampRect = new Rectangle(
                margin - clampPad,
                margin - clampPad,
                gridW + 2 * clampPad,
                gridH + 2 * clampPad
        );

        // Start virtual cursor at center of grid
        vMouseX = margin + gridW / 2f;
        vMouseY = margin + gridH / 2f;

        prevRawMouseX = mouseX;
        prevRawMouseY = mouseY;
    }

    public void draw()
    {
        background(0);

        // Update virtual cursor using real mouse deltas
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
            text("Average time per button: " + nf((timeTaken)/(float)(hits+misses),0,3) + " sec", width / 2, height / 2 + 100);
            text("Average + penalty: " + nf(((timeTaken)/(float)(hits+misses) + penalty),0,3) + " sec", width / 2, height / 2 + 140);
            return;
        }

        fill(255);
        text((trialNum + 1) + " of " + trials.size(), 60, 20);
        text("Click mouse or press 'A'. Target has halo (visual only).", width / 2, 20);

        // Draw halo first so it appears behind the target square
        drawTargetHalo();

        // Draw buttons
        for (int i = 0; i < 16; i++)
            drawButton(i);

        drawClampBorder();
        drawBigCursor((int)vMouseX, (int)vMouseY);
    }

    // Draw a halo around the current target square (visual only)
    private void drawTargetHalo() {
        if (trialNum >= trials.size()) return;

        int targetIdx = trials.get(trialNum);
        Rectangle b = getButtonLocation(targetIdx);

        noFill();
        stroke(255, 255, 0, haloAlpha); // yellow halo
        strokeWeight(haloStroke);

        // draw slightly larger rectangle around the button (does not change hitbox)
        rect(b.x - haloPad, b.y - haloPad, b.width + 2 * haloPad, b.height + 2 * haloPad);

        noStroke();
        strokeWeight(1);
    }

    public void mousePressed() {
        performClick();
    }

    public void keyPressed() {
        if (key == 'a' || key == 'A')
            performClick();
    }

    private void performClick()
    {
        if (trialNum >= trials.size())
            return;

        if (trialNum == 0)
            startTime = millis();

        if (trialNum == trials.size() - 1)
            finishTime = millis();

        int chosen = getMostCoveredButton((int)vMouseX, (int)vMouseY);
        int target = trials.get(trialNum);

        if (chosen == target)
            hits++;
        else
            misses++;

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

        // keep target square yellow (no flashing)
        if (trialNum < trials.size() && trials.get(trialNum) == i)
            fill(255, 255, 0);
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
        noStroke();
        fill(255, 0, 0, 60);
        ellipse(cx, cy, cursorRadius * 2, cursorRadius * 2);

        noFill();
        stroke(255, 0, 0, 220);
        strokeWeight(3);
        ellipse(cx, cy, cursorRadius * 2, cursorRadius * 2);

        noStroke();
        fill(255, 0, 0);
        ellipse(cx, cy, 8, 8);
    }

    private int getMostCoveredButton(int cx, int cy) {
        int bestIdx = -1;
        int bestScore = 0;

        int r = cursorRadius;
        int r2 = r * r;

        for (int i = 0; i < 16; i++) {
            Rectangle b = getButtonLocation(i);
            int score = 0;

            for (int x = b.x; x < b.x + b.width; x += overlapStep) {
                for (int y = b.y; y < b.y + b.height; y += overlapStep) {
                    int dx = x - cx;
                    int dy = y - cy;
                    if (dx*dx + dy*dy <= r2)
                        score++;
                }
            }

            if (score > bestScore) {
                bestScore = score;
                bestIdx = i;
            }
        }

        return bestIdx;
    }

    public void mouseMoved() {}
    public void mouseDragged() {}
}