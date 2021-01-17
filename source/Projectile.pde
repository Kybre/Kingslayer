class Projectile extends MovingObject{ //projectile class

  boolean isAlive, hitOnce, orbit = false, gravity = false; //if projectile exists, if projectile has hit once, whether projectile orbits character and whether projectile is affected by gravity respectively
  float timer = -1, dmg, radius, angle = 0; //timer tracking how long projectile has been on screen, projectile damage, orbit radius and orbit angle respectively
  
  //projectile shape
  int shape;
  final int RECTANGLE = 0;
  final int CIRCLE = 1;
  color c; //projectile color
  Character orbitTarget; //orbit target for projectile
  
  Projectile(PVector pos, PVector dim, PVector vel, float damage, int shape, color c){ //projectile constructor
    super(pos, dim, vel);
    dmg = damage;
    this.shape = shape;
    this.c = c;
  }
  
  void update(){ //updates projectile object
    super.update();
    timer++;
  }
  
  void move(){ //moves projectile
    if(!orbit){ //if projectile is not orbitting character
      super.move();
      if(gravity)vel.add(0, 0.6); //adds gravity if projectile is affected
    }
    else orbit(orbitTarget, radius, vel.x); //orbits character if projectile is set to orbit
  }
  
  void orbit(Character c, float r, float v){ //method for projectile to orbit character
    orbit = true; //set orbit state to true
    //position rotates around center of character
    pos.x = c.pos.x + r*cos(angle);
    pos.y = c.pos.y + r*sin(angle);
    angle += abs(v);
    //after projectile has been orbitting for 360 frames have it leave the screen and remove itself
    if(timer > 360){
      pos.x = width+2*dim.x;
      pos.y = height+2*dim.y;
    }
  }
  
  boolean proximity(Entity other, float range){ //checks if projectile is in distance 'range' of an entity 'other'
    return abs(pos.x-other.pos.x) < dim.x/2+other.dim.x/2 + range && abs(other.pos.y-pos.y) < dim.y/2+other.dim.y/2 + range;
  }
  
  void beamX(float bVel){ //method to extend horizontal beam
    if(bVel > 0){ //if projectile is to be shot to the right, extend rightwards
      dim.x += bVel*dim.x;
    }
    else{ //if projectile is to be shot to the left, extend leftwards
      dim.x -= bVel*dim.x;
    }
    pos.x += (bVel*dim.x)/(2*abs(bVel)+2); //adjust beam position so that it only extends in 1 direction
  }
  
  void beamY(float bVel){ //method to extend vertical beam
    if(bVel > 0){ //if projectile is to be shot down, extend down
      dim.y += bVel*dim.y;
    }
    else{ //if projectile is to be shot up, extend up
      dim.y -= bVel*dim.y;
    }
    pos.y += (bVel*dim.y)/(2*abs(bVel)+2); //adjust beam position so that it only extends in 1 direction
  }
  
  void drawMe(float sw){ //draws projectile, sw = stroke weight
    pushMatrix();
    translate(pos.x, pos.y);
    rectMode(CENTER);
    stroke(0);
    strokeWeight(sw);
    fill(c);
    if(shape == RECTANGLE) rect(0, 0, dim.x, dim.y);
    else if (shape == CIRCLE) ellipse(0, 0, dim.x, dim.y);
    popMatrix();
  }
}
