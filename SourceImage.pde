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
  
  void doneLoading () {
    loading = false;
    image(img, 0, 0);
  }

}
