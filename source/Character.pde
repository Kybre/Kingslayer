class Character extends MovingObject{ //character superclass
  float maxHealth, health = maxHealth, damp = 0.5, deathTimer, spd = 2; //health variables, movement dampener, death timer and movement speed respectively
  PImage img; //character image
  ArrayList<Projectile> projs = new ArrayList<Projectile>(); //projectiles stored under character
  Entity footbox; //hitbox for floor/wall collision detection
  boolean onGround, isAlive = true, lookLeft = false; //boolean checking states of: is player on solid ground, is player alive, and is player looking left respectively
  
  Character(PVector pos, PVector dim, PVector vel, PImage img){ //character constructor
    super(pos, dim, vel);
    this.img = img;
    footbox = new Entity(new PVector(pos.x, pos.y+dim.y/4), new PVector(dim.x/3, dim.y/8));
  }
  
  void update(){ //updates character
    super.update();
    if(!onGround) vel.add(0, 15); //gravity
    vel.mult(damp); //movement dampening
  }
  
  void fire(){ //fire method
  }
  
  void damage(float d){ //damage method
    health -= d;
    if(health <= 0) isAlive = false;
  }
}
