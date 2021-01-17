class Boss extends Character{
  int phase, firePattern; //boss phase and boss fire pattern respectively
  float stun, maxStun; //how much stun boss has and the max amount of stun boss has respectively
  float actionFrame = 0, shotFrame = 0; //boss ai tracking timer and firing pattern tracking timer respectively
  float jumpFrames = 0, jumpSpd, baseJumpSpd; //frames boss has been jumping, boss current jump acceleration speed and default jump acceleration speed respectively
  float stunDmgMult, diff, moneyEarned; //stun damage multiplier, boss difficulty and money earned from boss respectively
  String name; //boss name
  Entity hurtbox, jumpbox; //hitboxes for damaging boss and intersection box to determine whether there is a platform above boss respectively
  boolean shot = false, warnToggle = false, stunBreak = false, jump; //boolean for boss states: has boss shot, warning image toggle, is boss stunned, and is boss jumping respectively
  Projectile w1, w2; //placeholder objects for wave pattern
  Projectile h1, h2, h3, h4; //placeholder objects for homing pattern
  Projectile beam; //placeholder object for beam pattern
  ArrayList<Projectile> homing = new ArrayList<Projectile>(); //arraylist storing homing projectiles
  ArrayList<PImage> warnImages = new ArrayList<PImage>(); //arraylist storing warning images
  
  //boss states
  final int IDLE = 0;
  final int WALK = 1;
  final int ATK = 2;
  final int WARN = 3;
  final int BREAK = 4;
  int state = IDLE; //default state
  
  Boss(PVector pos, PVector dim, PImage img, String name, float sdm, int diff){ //boss constructor, parameter clarification: sdm = stun damage multiplier
    super(pos, dim, new PVector(0, 0), img);
    this.diff = diff;
    
    //set health and stun based on difficulty parameter
    maxHealth = 250 + 250*diff;
    health = maxHealth;
    maxStun = maxHealth/2;
    stun = maxStun;
    
    //set other variables
    this.name = name;
    stunDmgMult = sdm;
    
    //default image constructor (demo boss was shop crystals, updated in individual boss constuctors
    for(int i = 1; i < 8; i++){
      PImage w = loadImage("shop/shop_" + i + ".png");
      w.resize(w.width/2, w.height/2);
      warnImages.add(w);
    }
    
    //set boss hurtboxes
    hurtbox = new Entity(new PVector(pos.x, pos.y), new PVector(3*dim.x/4, 3*dim.y/4));
    jumpbox = new Entity(new PVector(pos.x, pos.y-dim.y/2+50), new PVector(dim.x, 50));
    
    //set jump variables
    baseJumpSpd = -20;
    jumpSpd = baseJumpSpd;
  }
  
  void update(){ //updates boss object
    super.update();
    
    //update boss phase
    if(health/maxHealth <= 0.25) phase = 3;
    else if(health/maxHealth <= 0.5) phase = 2;
    else if(health/maxHealth <= 0.75) phase = 1;
    else phase = 0;
    
    //stops falling if boss is on the ground
    if(onGround) vel.y = 0;
    
    //update footbox position
    adjustHitboxes();
    
    if(!stunBreak){ //if boss isn't stunned
      //boss ai
      //every 5 seconds, spend the first 3.5 seconds moving, then the last 1.5 seconds firing
      
      //movement section
      if(actionFrame < 205 && !shot){
        
        //random chance for boss to turn around
        if(actionFrame == 0 && random(0, 10) > 5) spd = -spd;
        else if(actionFrame%15 == 0 && random(0, 10) > 9) spd = -spd;
      
        //determine direction boss is facing
        if(intersects(p.hurtbox)) turnAway(p);
        if(spd > 0) lookLeft = false;
        else lookLeft = true;
        
        //move boss
        accel(new PVector(spd, 0));
        state = WALK;
      }
      else if(actionFrame >= 205 && actionFrame <= 210){ //turns boss towards character before firing, sets animation state
        if(actionFrame == 205 && p.pos.x < pos.x) lookLeft = true;
        else if(actionFrame == 205) lookLeft = false;
        setFirePattern();
        state = ATK; 
      }
      else if (actionFrame >= 210) fire(firePattern); //fire
      if(actionFrame >= 300 && !shot){ //reset ai to beginning
        actionFrame = 0;
        shotFrame = 0;
        state = IDLE;
      }
      checkProjectiles();
    }
    else { //if boss is stunned
      state = BREAK; //change animation/boss state
      if(actionFrame >= 300){ //when boss has been in break state for 5 seconds
        actionFrame = 0; //reset ai timer
        stun = maxStun; //reset stun
        turnAway(p); //turn boss away from player
        stunBreak = false; //remove stun effect
      }
    }
    
    //random jump if there is platform near boss
    for(int i = 0; i < s.plats.size(); i++){
        Platform plat = s.plats.get(i);
        float rand = random(0, 100);
        if(jumpbox.intersects(plat) && onGround && plat.floor && rand >= 98.5 && !shot && !stunBreak && actionFrame < 205) jump = true; //random chance for boss to jump if there is a floored platform above it, it is currently on the ground, and isn't shooting or stunned
    }
    if(jump){ //if boss has been triggered to jump
      accel(new PVector(0, jumpSpd));
      jumpSpd++;
      jumpFrames++;
      if(jumpFrames >= 15) jump = false;
    }
    else if(onGround && !jump){ //reset jumping values if boss is on the grounds
      jumpSpd = baseJumpSpd;
      jumpFrames = 0;
    }
    actionFrame++; //increase boss action ai timer
    
    //debug (draws hitboxes)
    if(debug){
      footbox.drawMe(color(160, 160, 0), 75, 0);
      hurtbox.drawMe(color(0, 0, 200), 75, 0);
      jumpbox.drawMe(color(160, 0, 0), 75, 0);
    }
  }
  
  void adjustHitboxes(){ //adjust boss hitboxes to stay on the boss
    footbox.pos.x = pos.x;
    footbox.pos.y = pos.y+7*dim.y/16;
    hurtbox.pos = pos;
    jumpbox.pos = new PVector(pos.x, pos.y-dim.y/2+50);
  }
  
  void setFirePattern(){ //sets boss firing pattern
    firePattern = (int)random(0, 10);
  }
  
  void drawMe(){ //draws boss
    pushMatrix();
    translate(pos.x, pos.y);
    //draw the image
    if(lookLeft) scale(-1, 1);
    imageMode(CENTER);
    image(img, 0, 0);
    popMatrix();
  }
  
  void damage(float d){ //method to damage boss
    if(isAlive && health <= 0){ //if boss is alive and is damaged beyond 0, start death animation
      isAlive = false;
      deathTimer = 180;
    }
    stun -= 2d; //reduce stun
    if(stun <= 0 && !stunBreak){ //if stun is below 0, have boss enter break state
      health -= d*stunDmgMult; //multiply damage done based on player's damage multiplier
      actionFrame = 0; //reset boss ai
      projs = new ArrayList<Projectile>(); //reset projectile array
      bossBreak.play(0);
      stunBreak = true;
    }
    else health -= d; //reduce health
  }
  
  void turnAway(Player player){ //turn boss away from player
    if(player.pos.x > this.pos.x && spd > 0 || player.pos.x < this.pos.x && spd < 0) spd = -spd;
  }
  
  void drawDeath(){ //draw shrinking death animation
    if(deathTimer == 180){
      img = warnImages.get(0);
      moneyEarned = 50 + 25*diff; //give money to player based on difficulty
      money += moneyEarned;
    }
    pushMatrix();
    translate(pos.x, pos.y);
    scale(deathTimer/180);
    //draw the image
    imageMode(CENTER);
    image(img, 0, 0);
    popMatrix();
    deathTimer--;
  }
  
  void fire(int pattern){ //method to fire pattern
  }
  
  void warn(PImage wImg, float frames){ //method to toggle boss between 2 frames to create warning animation, wImg = other image used aside from base, frames = time allotted to warning player
   if(shotFrame%10 == 0 && warnToggle == false) warnToggle = true;
   else if(shotFrame%10 == 0 && warnToggle == true) warnToggle = false;
   if(!warnToggle || shotFrame >= frames-1){
     img = warnImages.get(3);
   }
   else {
     img = wImg;
   }
 }
 void checkProjectiles(){ //checks boss's projectiles
    //bullet checking
    for(int i = 0; i<projs.size(); i++){
      Projectile pr = projs.get(i);
      pr.update();
      pr.drawMe(1.5);
      if(pr.intersects(p.hurtbox) && !pr.hitOnce){ //if a projectile intersects the player's hurtbox and hasn't before
        p.damage(pr.dmg);
        pr.hitOnce = true;
      }
      if(pr.timer%60 == 0){ //if projectile has been intersecting with player hitbox for a second, toggle so that player can be damaged again
        pr.hitOnce = false;
      }
      if(pr.pos.x-pr.dim.x > width*1.5 || pr.pos.x+pr.dim.x < -0.5*width || pr.pos.y-pr.dim.y > height*1.5 || pr.pos.y+pr.dim.y < -0.5*height) projs.remove(pr); //if projectile leaves the screen*1.5
    }
  }
  
  void drawHUD(){ //draws HUD for boss elements
    //health bar
    fill(0);
    textAlign(LEFT);
    textSize(20);
    
    //text
    text(name + ":", 120, 45);
    textSize(15);
    text("STUN:", 120, 100);
    stroke(0);
    strokeWeight(3);
    fill(255, 0, 0);
    rectMode(CORNER);
    
    //bars
    if(health > 0) rect(120, 50, health, 20); //health bar
    fill(200, 160, 0);
    if(stun > 0) rect(120, 75, stun, 10); //stun bar
    fill(0, 0);
    rect(120, 50, maxHealth, 20); //outline for max HP
    rect(120, 75, maxStun, 10); //outline for max Stun
  }
}
