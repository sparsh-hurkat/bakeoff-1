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

    public static void main(String[] args) {
        PApplet.main("Main");
    }

    @Override
    public void settings() {
        size(700,700);
    }

    public void setup()
    {
        //noCursor();
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

        // default cursor dot
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

        // ONLY CHANGE: target is yellow
        if (trials.get(trialNum) == i)
            fill(255, 255, 0);
        else
            fill(200);

        rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void mouseMoved() { }
    public void mouseDragged() { }
    public void keyPressed() { }
}