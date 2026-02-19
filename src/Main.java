/*
IDEA 3 — PULSING TARGET (WITH METRICS + RED CIRCLE CURSOR + GRID CENTER RESET)
*/

import java.awt.*;
import java.util.*;
import processing.core.PApplet;

public class Main extends PApplet {

    int margin = 200;
    final int padding = 50;
    final int buttonSize = 40;

    boolean useCustomCursor = true;

    ArrayList<Integer> trials = new ArrayList<>();
    int trialNum = 0;

    int hits = 0;
    int misses = 0;

    long startTime = 0;
    long finishTime = 0;

    boolean finished = false;

    Robot robot;

    public static void main(String[] args){ PApplet.main("Idea3"); }
    public void settings(){ size(700,700); }

    public void setup(){
        noStroke();

        try { robot = new Robot(); }
        catch(Exception e){ e.printStackTrace(); }

        for(int i=0;i<16;i++) trials.add(i);
        Collections.shuffle(trials);

        textAlign(CENTER, CENTER);

        if(useCustomCursor) noCursor();

        moveCursorToGridCenter(); // start centered
    }

    public void draw(){
        background(0);

        if(!finished){
            for(int i=0;i<16;i++) drawButton(i);
            drawTopStats();
        } else {
            drawResults();
        }

        drawCustomCursor();
    }

    public void mousePressed(){
        if(finished) return;

        if(hits == 0 && misses == 0){
            startTime = millis();
        }

        Rectangle target = getButtonLocation(trials.get(trialNum));

        boolean hit =
                mouseX > target.x && mouseX < target.x + target.width &&
                        mouseY > target.y && mouseY < target.y + target.height;

        if(hit){
            hits++;
            trialNum++;

            if(hits == 16){
                finishTime = millis();
                finished = true;
            } else {
                moveCursorToGridCenter(); // reset after correct click
            }
        } else {
            misses++;
            moveCursorToGridCenter(); // reset after miss too
        }
    }

    void drawTopStats(){
        fill(255);
        textAlign(LEFT, TOP);
        textSize(16);
        text("Correct: " + hits + " / 16", 20, 20);
        text("Misses: " + misses, 20, 45);
    }

    void drawResults(){
        fill(255);
        textAlign(CENTER, CENTER);
        textSize(18);

        float rawTime = (finishTime - startTime) / 1000.0f;
        float penaltyTime = misses * 0.5f;
        float finalTime = rawTime + penaltyTime;
        float avgTimePerButton = rawTime / 16.0f;

        text("Finished!", width/2, height/2 - 120);

        text("Correct Clicks: " + hits, width/2, height/2 - 80);
        text("Misclicks: " + misses, width/2, height/2 - 50);

        text("Raw Time: " + nf(rawTime,1,2) + " s", width/2, height/2 - 10);
        text("Penalty Time: " + nf(penaltyTime,1,2) + " s", width/2, height/2 + 20);
        text("Final Adjusted Time: " + nf(finalTime,1,2) + " s", width/2, height/2 + 50);
        text("Avg Time per Button: " + nf(avgTimePerButton,1,2) + " s",
                width/2, height/2 + 90);
    }

    Rectangle getButtonLocation(int i){
        int x = (i % 4) * (padding + buttonSize) + margin;
        int y = (i / 4) * (padding + buttonSize) + margin;
        return new Rectangle(x, y, buttonSize, buttonSize);
    }

    void drawButton(int i){
        Rectangle b = getButtonLocation(i);

        if(!finished && trials.get(trialNum) == i){
            float pulse = 6 * sin(frameCount * 0.2f);

            stroke(255);
            strokeWeight(4);

            fill(255,255,0);
            rect(b.x - pulse, b.y - pulse,
                    b.width + pulse * 2, b.height + pulse * 2);
        } else {
            noStroke();
            fill(120);
            rect(b.x, b.y, b.width, b.height);
        }
    }

    // RESET TO TRUE GRID CENTER
    void moveCursorToGridCenter(){
        int gridCenterX = margin + (4 * buttonSize + 3 * padding) / 2;
        int gridCenterY = margin + (4 * buttonSize + 3 * padding) / 2;

        Point windowLoc = ((java.awt.Canvas) surface.getNative()).getLocationOnScreen();
        int screenX = windowLoc.x + gridCenterX;
        int screenY = windowLoc.y + gridCenterY;

        robot.mouseMove(screenX, screenY);
    }

    // RED CIRCLE CURSOR
    void drawCustomCursor(){
        if(!useCustomCursor) return;

        noStroke();
        fill(255,0,0,60);
        ellipse(mouseX, mouseY, 40, 40);

        fill(255,0,0);
        ellipse(mouseX, mouseY, 16, 16);

        fill(255);
        ellipse(mouseX, mouseY, 4, 4);
    }
}
