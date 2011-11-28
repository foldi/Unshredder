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

  void update() {
    a += (ta - a) * 0.1;
  }

  void render() {
    
    textSize(s);
    float sw = textWidth(m);

    fill(100, a);
    rect(width/2 - (sw * 2)/2, height/2 - h/2, sw * 2, h);
    
    fill(255);
    text(m, width/2 - sw/2, height/2 + s/2);
  }
}

