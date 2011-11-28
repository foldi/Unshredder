import processing.core.*; 
import processing.xml.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class unshred extends PApplet {

SourceImage simg; // shredded image
int sWidth = 0; // width in pixels of image segements
int verticalSamples = 0; // number of vertical samples
int verticalSamplesFactor = 4; // lower the value for more verical sample
int[] diffs = new int[2]; // used when determining segment interval; holds two segments w largest color difference
ArrayList<Segment> initialSegments = new ArrayList(); // list of initial Segment objects
ArrayList<LeftEdge> initLeftSegments = new ArrayList(); // list of segments for determining initial left edge Segment
ArrayList<Segment> finalSegments = new ArrayList(); // list of final Segment objects
Message m;
Menu menu;
Title title;
String drawState = null;
int progress = 0;

float totalRed;
float totalGreen;
float totalBlue;
float interval;
int loc;

public void setup() {
  size(640, 480);
  noStroke();
  colorMode(RGB, 255);
  background(0);

  loadSourceImage("http://www.vinceallen.com/processing/unshred/data/shredded.png", false); // begin with city image
  
}

public void draw() {
  
  if (drawState == "load image") {

    if (simg != null) {

      if (simg.loading && simg.img.width > 0) {
        m = null;
        verticalSamples = simg.img.height/verticalSamplesFactor; // base vertical samples on image height
        drawState = "get segment interval";
        simg.doneLoading();
        menu = null;
      }
    }
  } 
  else if (drawState == "get segment interval") {

    simg.img.loadPixels(); // load pixels

      for (int i = 0; i < simg.img.width; i++) {

      Segment s = new Segment();
      s.pos = i;
      s.img = simg.img.get(i, 0, 1, simg.img.height);   

      for (int a = 0; a < verticalSamples; a++) {

        totalRed = 0;
        totalGreen = 0;
        totalBlue = 0;
        loc = 0;
        interval = (1000/verticalSamples) * .001f;   

        for (int y = floor(simg.img.height * (interval * a)); y < simg.img.height * (interval * (a + 1)); y++) { // Loop over every row

          loc = i + (y * simg.img.width); // Use the formula to find the 1D location
          int c = simg.img.pixels[loc]; // get the color at loc

          totalRed += red(c);
          totalGreen += green(c);
          totalBlue += blue(c);
        }

        s.leftSideColors[a] = color(totalRed/(simg.img.height/verticalSamples), totalGreen/(simg.img.height/verticalSamples), totalBlue/(simg.img.height/verticalSamples));
      }

      initialSegments.add(s);

      if (i != 0) {
        Segment prevSegment = (Segment) initialSegments.get(i - 1);
        float diff = 0;
        for (int x = 0; x < s.leftSideColors.length; x++) {
          diff += colorDifference(s.leftSideColors[x], prevSegment.leftSideColors[x]);
        }
        s.segmentIntervalDiff = diff;
      }
    }

    Collections.sort(initialSegments); // sort

    /* GET 2 SEGMENTS WITH LARGEST DIFFERENCE IN EDGE COLOR VALUE */
    int x = 0; 
    for (int i = initialSegments.size() - 1; i > initialSegments.size() - 3; i--) { // loop thru last 3 segments
      Segment segment = (Segment) initialSegments.get(i);
      diffs[x] = segment.pos;
      x++;
    }

    /* FIND LARGEST COMMON DENOMINATOR BW TWO SEGEMENTS' POSITION */
    int[] a = new int[diffs[0] + 1];

    for (int i = 1; i < diffs[0] + 1; i++) { // loop up to diff value
      if (diffs[0] % i == 0) {
        a[i] = i; // create array of values that divide evenly
      }
    }


    int[] b = new int[diffs[1] + 1];

    for (int i = 1; i < diffs[1] + 1; i++) {  // loop up to diff value
      if (diffs[1] % i == 0) {
        b[i] = i; // create array of values that divide evenly
      }
    }


    a = sort(a); // sort the arrays of common denominators
    b = sort(b);

    int[] intervals = new int[a.length];

    for (int i = a.length - 1; i > 0; i--) { // find the highest shared denominator
      if (a[i] != 0) {
        if (inArray(a[i], b)) {
          intervals[i] = a[i];
        }
      }
    }

    // the lowest interval in the last half of the initialSegments arrayList

    Segment segmentMax = (Segment) initialSegments.get(initialSegments.size() - 1); // get most different segment
    float maxDiff = segmentMax.segmentIntervalDiff; // get the diff value

    int[] segIntervals = new int[intervals.length];

    for (int i = 0; i < intervals.length; i++) { // loop thru intervals
      if (intervals[i] != 0) {
        int ind = getObjectIndexByPos(intervals[i]);
        Segment segment = (Segment) initialSegments.get(ind);

        if ((maxDiff - segment.segmentIntervalDiff) < maxDiff / 1.5f) { // find the breakpoint bw segments and LCDs
          segIntervals[i] = segment.pos;
        }
      }
    }

    segIntervals = sort(segIntervals);

    for (int i = 0; i < segIntervals.length; i++) { // loop thru intervals
      if (segIntervals[i] != 0) {
        sWidth = segIntervals[i];
        break;
      }
    }

    if (sWidth == 0) {
      sWidth = 1;
    }

    println("sWidth: " + sWidth);

    initialSegments.clear();
    
    String msg;
    
    if (sWidth > 8) {
      msg = sWidth + " px wide; press 'u' to unshred";
    } else {
      msg = sWidth + " px wide... may take a few seconds! press 'u' to unshred";
    }
    
    m = new Message(msg);

    drawState = "unshred input";
  }
  else if (drawState == "unshred input") {
  } 
  else if (drawState == "create segments") {

    // LEFT EDGES

    simg.img.loadPixels(); // load pixels

      for (int i = 0; i < simg.img.width; i += sWidth) {

      Segment s = new Segment();
      s.pos = i/sWidth;
      s.img = simg.img.get(i, 0, sWidth, simg.img.height);   

      for (int a = 0; a < verticalSamples; a++) {

        totalRed = 0;
        totalGreen = 0;
        totalBlue = 0;
        loc = 0;
        interval = (1000/verticalSamples) * .001f;

        for (int y = floor(simg.img.height * (interval * a)); y < simg.img.height * (interval * (a + 1)); y++) { // Loop over every row

          loc = i + (y * simg.img.width); // Use the formula to find the 1D location
          int c = simg.img.pixels[loc]; // get the color at loc

          totalRed += red(c);
          totalGreen += green(c);
          totalBlue += blue(c);
        }
        s.leftSideColors[a] = color(totalRed/(simg.img.height/verticalSamples), totalGreen/(simg.img.height/verticalSamples), totalBlue/(simg.img.height/verticalSamples));
      }

      // RIGHT EDGES

      for (int a = 0; a < verticalSamples; a++) {

        totalRed = 0;
        totalGreen = 0;
        totalBlue = 0;
        loc = 0;
        interval = (1000/verticalSamples) * .001f;

        for (int y = floor(simg.img.height * (interval * a)); y < simg.img.height * (interval * (a + 1)); y++) { // Loop over every row

          loc = i + (sWidth-1) + (y * simg.img.width); // Use the formula to find the 1D location
          int c = simg.img.pixels[loc]; // get the color at loc

          totalRed += red(c);
          totalGreen += green(c);
          totalBlue += blue(c);
        }
        s.rightSideColors[a] = color(totalRed/(simg.img.height/verticalSamples), totalGreen/(simg.img.height/verticalSamples), totalBlue/(simg.img.height/verticalSamples));
      }
      initialSegments.add(s);
    }

    drawState = "get left segment";
  } 
  else if (drawState == "get left segment") {

    compareLeftEdgesToRight(); // build allLeftEdges array list for each initialSegment
    compareRightEdgesToLeft(); // build allRightEdges array list for each initialSegment

      // GET INITIAL LEFT EDGE SEGMENT

    for (int i = 0; i < initialSegments.size(); i++) { // loop thru all segments
      Segment segment = (Segment) initialSegments.get(i);

      LeftEdge lft = new LeftEdge(); // create new LeftEdge for initLeftSegments array list
      lft.pos = segment.pos; // copy this segment's position

      LeftEdge lft2 = segment.allLeftEdges.get(0); // allLeftEdges has been sorted; the first entry is the lowest value
      lft.colorValDiff = lft2.colorValDiff; // copy the colorValDiff
      initLeftSegments.add(lft); // add the segment to the initLeftSegments array list
    }

    Collections.sort(initLeftSegments); // sort the initLeftSegments

      LeftEdge l1 = (LeftEdge) initLeftSegments.get(initLeftSegments.size() - 1); // the segment with the highest value (end of the list) has the largest left edge value difference
    // l1.pos holds the initial position of the intial segment

    Segment leftEdge = (Segment) initialSegments.get(getObjectIndexByPos(l1.pos)); // get left border from initialSegments

      Segment initSeg = new Segment(); // make a new segment; copy pos and img
    initSeg.pos = 0;
    initSeg.img = leftEdge.img;
    finalSegments.add(initSeg);

    // recursive function: matches leftmost segment's right edge w a left edge; sends that left edge back to the function
    placeSegments(0, initialSegments.get(getObjectIndexByPos(l1.pos)));

    drawState = "render segments";
  } 
  else if (drawState == "render segments") {

    Segment segment = (Segment) initialSegments.get(progress);
    float interval = ((1000/verticalSamples) * .001f);

    for (int a = 0; a < verticalSamples; a++) {
      float x = map(segment.pos, 0, initialSegments.size(), 0, simg.img.width);
      float h = simg.img.height;
      fill(segment.leftSideColors[a]);
      rect(x, simg.img.height * (interval * a), (float) simg.img.width/initialSegments.size()/8, -h * interval);
    }

    for (int a = 0; a < verticalSamples; a++) {
      float x = map(segment.pos, 0, initialSegments.size(), 0, simg.img.width);
      float h = simg.img.height;
      fill(segment.rightSideColors[a]);
      rect(x + sWidth - (float) simg.img.width/initialSegments.size()/8, simg.img.height * (interval * a), (float) simg.img.width/initialSegments.size()/8, -h * interval);
    }

    if (progress + 1 < initialSegments.size()) {
      progress++;
    } 
    else {
      progress = 0;
      drawState = "unshred";
    }
  } 
  else if (drawState == "unshred") {

    Segment segment = (Segment) finalSegments.get(progress);
    image(segment.img, segment.pos * sWidth, 0);

    if (progress + 1 < finalSegments.size()) {
      progress++;
    } 
    else {
      progress = 0;
      menu = new Menu(); // create menu
      drawState = "done";
    }
  } 
  else if (drawState == "done") {
  }

  if (m != null) { // update message if it exists
    m.update();
    m.render();
  }

  if (menu != null) { // update menu if it exists
    menu.update();
    menu.render();
  }
}

public void loadSourceImage (String url, Boolean local) {
  background(0);
  initialSegments.clear();
  initLeftSegments.clear();
  finalSegments.clear();
  verticalSamples = 0;
  m = new Message("loading...");
  drawState = "load image";
  simg = new SourceImage(url, local); // create a new source image
}

public int getObjectIndexByPos (int p) {

  for (int i = 0; i < initialSegments.size(); i++) { // loop thru all segments
    Segment segment = (Segment) initialSegments.get(i);
    if (segment.pos == p) {
      return i;
    }
  }

  return initialSegments.size();
}

public void compareLeftEdgesToRight () {

  /* compare left edges to right edges */
  for (int i = 0; i < initialSegments.size(); i++) { // loop thru all segments
    Segment segment = (Segment) initialSegments.get(i);

    segment.allLeftEdges.clear();
    for (int x = 0; x < initialSegments.size(); x++) { // loop thru all segments

      Segment s2 = (Segment) initialSegments.get(x);

      if (i != x && !s2.placed) {
        LeftEdge e = new LeftEdge();
        e.pos = s2.pos;
        e.colorValDiff = 0;
        for (int a = 0; a < verticalSamples; a++) {
          e.colorValDiff += colorDifference(segment.leftSideColors[a], s2.rightSideColors[a]);
        }
        segment.allLeftEdges.add(e);
      }
    }
    Collections.sort(segment.allLeftEdges); // sort
  }
}

public void compareRightEdgesToLeft () {

  /* compare right edges to left edges */
  for (int i = 0; i < initialSegments.size(); i++) { // loop thru all segments
    Segment segment = (Segment) initialSegments.get(i);

    segment.allRightEdges.clear();
    for (int x = 0; x < initialSegments.size(); x++) { // loop thru all segments

      Segment s2 = (Segment) initialSegments.get(x);

      if (i != x && !s2.placed) {
        RightEdge e = new RightEdge();
        e.pos = s2.pos;
        e.colorValDiff = 0;
        for (int a = 0; a < verticalSamples; a++) {
          e.colorValDiff += colorDifference(segment.rightSideColors[a], s2.leftSideColors[a]);
        }        
        segment.allRightEdges.add(e);
      }
    }
    Collections.sort(segment.allRightEdges); // sort
  }
}

public float colorDifference (int c1, int c2) {
  return abs(hue(c1) - hue(c2)) + abs(saturation(c1) - saturation(c2))  + abs(brightness(c1) - brightness(c2)) + abs(red(c1) - red(c2)) + abs(green(c1) - green(c2))  + abs(blue(c1) - blue(c2));
}

public void placeSegments (int ind, Segment segment) { // recursive

  compareRightEdgesToLeft(); // update edge info

  RightEdge e = (RightEdge) segment.allRightEdges.get(0); // get edges

  Segment newSeg = (Segment) initialSegments.get(getObjectIndexByPos(e.pos));
  newSeg.placed = true;

  Segment finalSeg = new Segment(); // make a new segment; copy pos and img
  finalSeg.pos = ind + 1;
  finalSeg.img = newSeg.img;
  finalSegments.add(finalSeg);

  if (ind < initialSegments.size()-1) {
    placeSegments(ind + 1, initialSegments.get(getObjectIndexByPos(e.pos)));
  }
}

public boolean inArray (int a, int[] b) {
  for (int i = 0; i < b.length; i++) {
    if (b[i] == a) {
      return true;
    }
  }
  return false;
}

public void keyPressed() {
  /*
    to load local images, place them in the /data folder;
    load them via loadSourceImage(imageName, true);
  */
  if (key == '1') loadSourceImage("http://www.vinceallen.com/processing/unshred/data/shredded.png", false);
  if (key == '2') loadSourceImage("http://www.vinceallen.com/processing/unshred/data/mountains64.jpg", false);
  if (key == '3') loadSourceImage("http://www.vinceallen.com/processing/unshred/data/tree40.jpg", false);  
  if (key == '4') loadSourceImage("http://www.vinceallen.com/processing/unshred/data/kitties32.jpg", false);
  if (key == '5') loadSourceImage("http://www.vinceallen.com/processing/unshred/data/kitchen16.jpg", false);
  if (key == '6') loadSourceImage("http://www.vinceallen.com/processing/unshred/data/bunnies8.jpg", false); 
  if (key == '7') loadSourceImage("http://www.vinceallen.com/processing/unshred/data/beach4.jpg", false);
  if (key == 'u') {
    if (drawState == "unshred input") {
      m = null;
      image(simg.img, 0, 0);
      drawState = "create segments";
    }
  }
}

class LeftEdge implements Comparable {
  
  int pos;
  float colorValDiff;
  
  public int compareTo (Object s) { // sortable ArrayList must have this method

    LeftEdge s2 = (LeftEdge) s;
 
    return floor(colorValDiff - s2.colorValDiff);
   
    // returns 0 if same thing; 1 if greater; -1 if less
  }
}

class Menu {

  int w = width;
  int h = 50;  
  int a = 0;
  int ta = 100;
  
  Menu () {
   
  }

  public void update() {
    a += (ta - a) * 0.1f;
  }

  public void render() {
    fill(100, a);
    rect(0, height - h, w, h);
    stroke(255, a);
    line(0, height - h, width, height - h);   
     noStroke(); 
    fill(255);
    text("Images: 1 = city  2 = mtns  3 = tree  4 = kitties  5 = kitchen  6 = bunnies  7 = beach", 10, height - 20);
  }
}
class Message {
  
  int w = 200;
  int h = 50;
  int s = 14;
  int a = 0;
  int ta = 100;
  String m;
  
  Message (String msg) {
   m = msg;
  }

  public void update() {
    a += (ta - a) * 0.1f;
  }

  public void render() {
    
    textSize(s);
    float sw = textWidth(m);

    fill(100, a);
    rect(width/2 - (sw * 2)/2, height/2 - h/2, sw * 2, h);
    
    fill(255);
    text(m, width/2 - sw/2, height/2 + s/2);
  }
}

class RightEdge implements Comparable {
  
  int pos;
  float colorValDiff;
  
  public int compareTo (Object s) { // sortable ArrayList must have this method

    RightEdge s2 = (RightEdge) s;
 
    return floor(colorValDiff - s2.colorValDiff);
   
    // returns 0 if same thing; 1 if greater; -1 if less
  }
}

class Segment implements Comparable {

  int[] leftSideColors = new int[1000];    
  int[] rightSideColors = new int[1000];
  float segmentIntervalDiff;  
  int pos;
  boolean placed = false;
  PImage img;
  ArrayList<LeftEdge> allLeftEdges = new ArrayList();
  ArrayList<RightEdge> allRightEdges = new ArrayList();

  public int compareTo (Object s) { // sortable ArrayList must have this method

    Segment s2 = (Segment) s;

    return floor(segmentIntervalDiff - s2.segmentIntervalDiff);

    // returns 0 if same thing; 1 if greater; -1 if less
  }
}

class SourceImage {
  
  PImage img;
  boolean loading;
  
  SourceImage (String url, Boolean local) {
    if (local) {
      img = loadImage(url);
    } else {
      img = requestImage(url);
    }
    loading = true;
  }
  
  public void doneLoading () {
    loading = false;
    image(img, 0, 0);
  }

}
class Title {
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--present", "--bgcolor=#666666", "--stop-color=#cccccc", "unshred" });
  }
}
