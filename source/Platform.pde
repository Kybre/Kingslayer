class Platform extends Entity{ //platform class
  PImage img;
  boolean hasChara = false, wall = false, floor = false; //whether platform currently contains a character, whether is has wall collision, and whether it has floor collision respectively
  
  Platform(PVector pos, PVector dim, PImage img, boolean wall, boolean floor){ //platform constructor, boolean values determine whether platform has wall/floor collision
    super(pos, dim);
    this.img = img;
    this.wall = wall;
    this.floor = floor;
  }
  
  void checkCollision(Character c){ //checks platform collision with a character
    //floor collision
    if(floor && intersects(c.footbox) && c.footbox.pos.y+c.footbox.dim.y/2 - (pos.y-dim.y/2) <= c.dim.y/8){ //checks whether character footbox intersects with platform
      hasChara = true;
      c.pos.y = pos.y - dim.y/2 - c.dim.y/2 + 1; //place character above platform
    }
    else hasChara = false;
    
    //wall collision
    if(wall && intersects(c.footbox) && c.footbox.pos.x > pos.x && c.footbox.pos.y - (pos.y - dim.y/2) > c.footbox.dim.y/2){ //checks whether character footbox intersects with right side of a wall
      c.pos.x = pos.x + dim.x/2 + c.footbox.dim.x/2; //places character directly right of the wall
    }
    else if(wall && intersects(c.footbox) && c.footbox.pos.x < pos.x && c.footbox.pos.y - (pos.y - dim.y/2) > c.footbox.dim.y/2){ //checks whether character footbox intersects with left side of a wall
      c.pos.x = pos.x - dim.x/2 - c.footbox.dim.x/2; //places character directly left of the wall
    }
  }
  
  void checkRebound(Character c){ //method to check for boss rebounding from wall
    if(wall && intersects(c.footbox) && c.footbox.pos.x > pos.x && c.footbox.pos.y - (pos.y - dim.y/2) > c.footbox.dim.y/2){ //checks whether character intersects with right side of wall
      c.pos.x = pos.x + dim.x/2 + c.footbox.dim.x/2 - c.spd; //places character directly right of wall
      c.spd = -c.spd; //reverse movement direction
    }
    if(wall && intersects(c.footbox) && c.footbox.pos.x < pos.x && c.footbox.pos.y - (pos.y - dim.y/2) > c.footbox.dim.y/2){ //checks whether character footbox intersects with left side of a wall
      c.pos.x = pos.x - dim.x/2 - c.footbox.dim.x/2 - c.spd; //places character directly left of the wall
      c.spd = -c.spd; //reverse movement direction
    }
  }
  
  void drawMe(){ //draws platform
    pushMatrix();
    translate(pos.x, pos.y);
    //draw the image
    imageMode(CENTER);
    image(img, 0, 0);
    popMatrix();
  }
}
