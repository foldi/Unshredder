class RightEdge implements Comparable {
  
  int pos;
  float colorValDiff;
  
  int compareTo (Object s) { // sortable ArrayList must have this method

    RightEdge s2 = (RightEdge) s;
 
    return floor(colorValDiff - s2.colorValDiff);
   
    // returns 0 if same thing; 1 if greater; -1 if less
  }
}

