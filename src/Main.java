/*
FINAL VERSION — Fitts Law Cursor Reset + Metrics + RED CIRCLE Cursor
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

    public static void main(String[] args){ PApplet.main("Idea1"); }
    public void settings(){ size(700,700); }

    public void setup(){
        noStroke();
        try { robot = new Robot(); } catch(Exception e){ e.printStackTrace(); }

        for(int i=0;i<16;i++) trials.add(i);
        Collections.shuffle(trials);

        if(useCustomCursor) noCursor();
        moveCursorToGridCenter();
    }

    public void draw(){
        background(0);

        if(!finished){
            drawButtons();
            drawTopScore();
        } else {
            drawResults();
        }

        drawCustomCursor();
    }

    void drawButtons(){
        for(int i=0;i<16;i++){
            Rectangle b = getButtonLocation(i);

            if(trials.get(trialNum) == i) fill(0,255,255);
            else fill(120);

            rect(b.x,b.y,b.width,b.height);
        }
    }

    void drawTopScore(){
        fill(255);
        textAlign(LEFT, TOP);
        textSize(16);
        text("Correct: " + hits + " / 16", 20, 20);
        text("Misses: " + misses, 20, 45);
    }

    public void mousePressed(){

        if(finished) return;

        // start timer on first click
        if(hits == 0 && misses == 0){
            startTime = millis();
        }

        Rectangle b = getButtonLocation(trials.get(trialNum));

        if(mouseX > b.x && mouseX < b.x + b.width &&
                mouseY > b.y && mouseY < b.y + b.height){

            hits++;
            trialNum++;

            if(hits == 16){
                finishTime = millis();
                finished = true;
            }

        } else {
            misses++;
        }

        if(!finished) moveCursorToGridCenter();
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
        text("Avg Time per Button: " + nf(avgTimePerButton,1,2) + " s", width/2, height/2 + 90);
    }

    Rectangle getButtonLocation(int i){
        int x = (i % 4) * (padding + buttonSize) + margin;
        int y = (i / 4) * (padding + buttonSize) + margin;
        return new Rectangle(x, y, buttonSize, buttonSize);
    }

    void moveCursorToGridCenter() {
        int gridCenterX = margin + (4 * buttonSize + 3 * padding) / 2;
        int gridCenterY = margin + (4 * buttonSize + 3 * padding) / 2;

        Point windowLoc = ((java.awt.Canvas) surface.getNative()).getLocationOnScreen();
        int screenX = windowLoc.x + gridCenterX;
        int screenY = windowLoc.y + gridCenterY;

        robot.mouseMove(screenX, screenY);
    }

    // BIG RED CIRCLE CURSOR
    void drawCustomCursor(){
        if(!useCustomCursor) return;

        noStroke();
        fill(255,0,0,60);   // glow ring
        ellipse(mouseX, mouseY, 40, 40);

        fill(255,0,0);      // main cursor
        ellipse(mouseX, mouseY, 16, 16);

        fill(255);          // precision center dot
        ellipse(mouseX, mouseY, 4, 4);
    }
}
