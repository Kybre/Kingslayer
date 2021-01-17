class Entity{ //entity superclass
  PVector pos, dim;
  
  Entity(PVector pos, PVector dim){ //entity constructor
    this.pos = pos;
    this.dim = dim;
  }
  
  void update(){ //updates entity
  }
  
  boolean intersects(Entity e){ //check for intersection between 2 entities
    if(abs(pos.x - e.pos.x) < (dim.x/2 + e.dim.x/2) && abs(pos.y-e.pos.y)< (dim.y/2 + e.dim.y/2)) return true; //if character hitboxes intersect return true;
    else return false;
  }
  
  void drawMe(color c, float op, float st){ //draws entity. parameters: c = entity color, op = opacity, st = stroke weight
    pushMatrix();
    rectMode(CENTER);
    fill(c, op);
    translate(pos.x, pos.y);
    stroke(0, op);
    strokeWeight(st);
    rect(0, 0, dim.x, dim.y);
    popMatrix();
  }
}
