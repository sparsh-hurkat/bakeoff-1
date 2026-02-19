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

    // === NEW: mouse speed gain (control–display gain)
    // 1.25f = 25% faster than baseline (baseline would be 1.0f)
    final float MOUSE_GAIN = 1.25f;

    // === Virtual cursor clamped inside area ===
    float vMouseX, vMouseY;
    int prevRawMouseX, prevRawMouseY;

    // === Clamp area around grid ===
    final int clampPad = 25;
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

        // Raw mouse deltas
        int dxRaw = mouseX - prevRawMouseX;
        int dyRaw = mouseY - prevRawMouseY;
        prevRawMouseX = mouseX;
        prevRawMouseY = mouseY;

        // Apply gain: cursor moves MOUSE_GAIN times faster than baseline
        float dx = dxRaw * MOUSE_GAIN;
        float dy = dyRaw * MOUSE_GAIN;

        vMouseX += dx;
        vMouseY += dy;

        // Clamp virtual cursor
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
        text("Gain: " + nf(MOUSE_GAIN,0,2) + "x (" + (int)((MOUSE_GAIN-1f)*100f) + "% faster). Click or press 'A'.",
                width / 2, 20);

        // Draw halo (optional visual aid) — comment out if you don't want it
        // drawTargetHalo();

        for (int i = 0; i < 16; i++)
            drawButton(i);

        drawClampBorder();
        drawBigCursor((int)vMouseX, (int)vMouseY);
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

        // yellow target, gray others
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

        noStroke();
        strokeWeight(1);
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