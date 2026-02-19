/*
IDEA 2 — HOVER + PRESS 'A' TO SELECT (WITH METRICS + GRID CENTER RESET)

User aims by hovering, then presses 'A' to select.
Misses ONLY happen when user presses 'A' on the wrong square (or not on any square).

Tracks:
- 16 correct selections
- misses
- 0.5s penalty per miss
- raw time, penalty time, final adjusted time
- avg time per correct selection
*/

import java.awt.*;
import java.util.*;
import processing.core.PApplet;

public class Main extends PApplet {

    int margin = 200;
    final int padding = 50;
    final int buttonSize = 40;

    ArrayList<Integer> trials = new ArrayList<>();

    int trialNum = 0;
    int hits = 0;
    int misses = 0;

    long startTime = 0;
    long finishTime = 0;

    boolean finished = false;

    Robot robot;

    public static void main(String[] args){ PApplet.main("Main"); }
    public void settings(){ size(700,700); }

    public void setup(){
        noStroke();
        try{ robot = new Robot(); } catch(Exception e){ e.printStackTrace(); }

        for(int i=0;i<16;i++) trials.add(i);
        Collections.shuffle(trials);

        moveCursorToGridCenter();
    }

    public void draw(){
        background(0);

        if(!finished){
            drawButtons();
            drawTopStats();
            drawHint();
        } else {
            drawResults();
        }
    }

    // Press A to select whatever you are currently hovering
    public void keyPressed(){
        if(finished) return;

        // only count selections when user presses 'A' or 'a'
        if(key != 'a' && key != 'A') return;

        // start timer on first selection attempt
        if(hits == 0 && misses == 0){
            startTime = millis();
        }

        int hoveredIndex = getHoveredButtonIndex();

        // If not hovering any button, that's a miss
        if(hoveredIndex == -1){
            misses++;
            moveCursorToGridCenter();
            return;
        }

        int targetIndex = trials.get(trialNum);

        if(hoveredIndex == targetIndex){
            hits++;
            trialNum++;

            if(hits == 16){
                finishTime = millis();
                finished = true;
            } else {
                moveCursorToGridCenter();
            }
        } else {
            // hovered a wrong square when pressing A
            misses++;
            moveCursorToGridCenter();
        }
    }

    int getHoveredButtonIndex(){
        for(int i=0;i<16;i++){
            Rectangle b = getButtonLocation(i);
            boolean inside =
                    mouseX > b.x && mouseX < b.x + b.width &&
                            mouseY > b.y && mouseY < b.y + b.height;
            if(inside) return i;
        }
        return -1;
    }

    void drawTopStats(){
        fill(255);
        textAlign(LEFT, TOP);
        textSize(16);
        text("Correct: " + hits + " / 16", 20, 20);
        text("Misses: " + misses, 20, 45);
    }

    void drawHint(){
        fill(200);
        textAlign(LEFT, TOP);
        textSize(14);
        text("Hover the highlighted square, then press 'A' to select.", 20, 70);
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

        text("Correct Selections: " + hits, width/2, height/2 - 80);
        text("Misses: " + misses, width/2, height/2 - 50);

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

    void drawButtons(){
        for(int i=0;i<16;i++){
            Rectangle b = getButtonLocation(i);

            if(!finished && trials.get(trialNum) == i) fill(0,255,255);
            else fill(120);

            rect(b.x,b.y,b.width,b.height);
        }
    }

    // Proper GRID CENTER reset (not window center)
    void moveCursorToGridCenter(){
        int gridCenterX = margin + (4 * buttonSize + 3 * padding) / 2;
        int gridCenterY = margin + (4 * buttonSize + 3 * padding) / 2;

        Point windowLoc = ((java.awt.Canvas) surface.getNative()).getLocationOnScreen();
        int screenX = windowLoc.x + gridCenterX;
        int screenY = windowLoc.y + gridCenterY;

        robot.mouseMove(screenX, screenY);
    }
}
