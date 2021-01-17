class Stage{
  ArrayList<Platform> plats = new ArrayList<Platform>();
  PImage floorImg, platImg;
  color bg;
  
  Stage(int preset, color background){
    bg = background;
    switch(preset){
      case 0:
        platImg = loadImage("stage/stage_0_0.png");
        floorImg = loadImage("stage/stage_0_1.png");
        //ground
        plats.add(new Platform(new PVector(width/2, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2-floorImg.width+5, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2+floorImg.width-5, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2-2*(floorImg.width-5), height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2+2*(floorImg.width-5), height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        
        //platforms
        plats.add(new Platform(new PVector(width/2+1.5*platImg.width, height-5*platImg.height), new PVector(platImg.width, platImg.height), platImg, false, true));
        plats.add(new Platform(new PVector(width/2-1.5*platImg.width, height-5*platImg.height), new PVector(platImg.width, platImg.height), platImg, false, true));
        
        //walls
        //right
        for(int i = 0; i < 6; i++){
          plats.add(new Platform(new PVector(width+floorImg.width/4-5, height-i*(floorImg.height-60)), new PVector(floorImg.width, floorImg.height), floorImg, true, false));
          plats.add(new Platform(new PVector(-floorImg.width/4+5, height-i*(floorImg.height-60)), new PVector(floorImg.width, floorImg.height), floorImg, true, false));
        }
        break;
      case 1:
        platImg = loadImage("stage/stage_1_0.png");
        floorImg = loadImage("stage/stage_1_1.png");
        //ground
        plats.add(new Platform(new PVector(width/2, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2-floorImg.width+5, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2+floorImg.width-5, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2-2*(floorImg.width-5), height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2+2*(floorImg.width-5), height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        
        //platforms
        plats.add(new Platform(new PVector(width/2+1.5*platImg.width, height-5*platImg.height), new PVector(platImg.width, platImg.height), platImg, false, true));
        plats.add(new Platform(new PVector(width/2-1.5*platImg.width, height-5*platImg.height), new PVector(platImg.width, platImg.height), platImg, false, true));
        
        //walls
        //right
        for(int i = 0; i < 6; i++){
          plats.add(new Platform(new PVector(width+floorImg.width/4-5, height-i*(floorImg.height-50)), new PVector(floorImg.width, floorImg.height), floorImg, true, false));
          plats.add(new Platform(new PVector(-floorImg.width/4+5, height-i*(floorImg.height-50)), new PVector(floorImg.width, floorImg.height), floorImg, true, false));
        }
        break;
      case 2:
        platImg = loadImage("stage/stage_2_0.png");
        floorImg = loadImage("stage/stage_2_1.png");
        //ground
        plats.add(new Platform(new PVector(width/2, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2-floorImg.width+5, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2+floorImg.width-5, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2-2*(floorImg.width-5), height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2+2*(floorImg.width-5), height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        
        //platforms
        plats.add(new Platform(new PVector(width/2+1.5*platImg.width, height-5*platImg.height), new PVector(platImg.width, platImg.height), platImg, false, true));
        plats.add(new Platform(new PVector(width/2-1.5*platImg.width, height-5*platImg.height), new PVector(platImg.width, platImg.height), platImg, false, true));
        
        //walls
        //right
        for(int i = 0; i < 6; i++){
          plats.add(new Platform(new PVector(width+floorImg.width/4-5, height-i*(floorImg.height-50)), new PVector(floorImg.width, floorImg.height), floorImg, true, false));
          plats.add(new Platform(new PVector(-floorImg.width/4+5, height-i*(floorImg.height-50)), new PVector(floorImg.width, floorImg.height), floorImg, true, false));
        }
        break;
      default:
        platImg = loadImage("stage/stage_0_0.png");
        plats.add(new Platform(new PVector(width/2, height-platImg.height/2), new PVector(platImg.width, platImg.height), platImg, true, true));
        break;
    }
  }
  
  void checkCollision(Character c){ //check stage collision for a character
    int platsTouched = 0;
    for(int i = 0; i < plats.size(); i++){ //check collision for all platforms
      Platform plat = plats.get(i);
      plat.checkCollision(c);
      if(plat.hasChara) platsTouched++;
    }
    if (platsTouched>0) c.onGround = true; //if player is touching a platform set onGround to true
    else c.onGround = false;
  }
  
  void checkRebound(Character c){ //check boss rebound for all platforms
    for(int i = 0; i < plats.size(); i++){
      Platform plat = plats.get(i);
      plat.checkRebound(c);
    }
  }
  
  void drawMe(){ //draws stage
    background(bg);
    for(int i = 0; i < plats.size(); i++){ //draws all individual platforms
      Platform plat = plats.get(i);
      plat.drawMe();
    }
  }
}
