class MovingObject extends Entity{ //superclass for all moving objects
  PVector vel;
  
  MovingObject(PVector pos, PVector dim, PVector vel){
    super(pos, dim);
    this.vel = vel;
  }
  
  void update(){ //updates moving object
    move();
    handleWalls();
  }
  
  void move(){ //movement based on vel PVector
    pos.add(vel);
  }
  
  void accel(PVector force){ //adds force parameter to vel
    vel.add(force);
  }
  
  void handleWalls(){ //method to handle wall collision
  }
  
  void drawMe(){ //draws moving object
  }
}
