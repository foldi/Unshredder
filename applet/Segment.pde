class Segment implements Comparable {

  color[] leftSideColors = new color[1000];    
  color[] rightSideColors = new color[1000];
  float segmentIntervalDiff;  
  int pos;
  boolean placed = false;
  PImage img;
  ArrayList<LeftEdge> allLeftEdges = new ArrayList();
  ArrayList<RightEdge> allRightEdges = new ArrayList();

  int compareTo (Object s) { // sortable ArrayList must have this method

    Segment s2 = (Segment) s;

    return floor(segmentIntervalDiff - s2.segmentIntervalDiff);

    // returns 0 if same thing; 1 if greater; -1 if less
  }
}

