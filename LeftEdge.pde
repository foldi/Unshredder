class LeftEdge implements Comparable {
  
  int pos;
  float colorValDiff;
  
  int compareTo (Object s) { // sortable ArrayList must have this method

    LeftEdge s2 = (LeftEdge) s;
 
    return floor(colorValDiff - s2.colorValDiff);
   
    // returns 0 if same thing; 1 if greater; -1 if less
  }
}

