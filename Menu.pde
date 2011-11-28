class Menu {

  int w = width;
  int h = 50;  
  int a = 0;
  int ta = 100;
  
  Menu () {
   
  }

  void update() {
    a += (ta - a) * 0.1;
  }

  void render() {
    fill(100, a);
    rect(0, height - h, w, h);
    stroke(255, a);
    line(0, height - h, width, height - h);   
     noStroke(); 
    fill(255);
    text("Images: 1 = city  2 = mtns  3 = tree  4 = kitties  5 = kitchen  6 = bunnies  7 = beach", 10, height - 20);
  }
}
