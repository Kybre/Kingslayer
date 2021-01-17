class Player extends Character{ //play class
  float damp = 0.5; //movement dampener
  
  //animation
  String imgHeader = "player/idle/idle"; //common image header
  int animFrame = 0; //current animation frame
  int spriteFrames = 6; //total animation frames
  int gunFrame = 0; //gun animation frame
  boolean dashHeld = false; //tracks if dash button is being held
  float iFrames = 60, iTimer = -1; //number of invincibility frames after being hit, invincibility timer
  
  //core functionality
  //movement
  boolean dash, shot, moving, sword, hitOnce, swordOnce; //dash = is player dashing, shot = is player shooting, moving = is player moving, sword = is player melee attacking, hitOnce = if player has been hitOnce, swordOnce 
  float maxStamina = 250, spdBase = 2; //max stamina, base speed
  float stamina = maxStamina; //current stamine
  
  //jumping
  int maxJumps = 1, jumps = maxJumps; //max number of jumps, current number of jumps
  int jumpFrames; //frames player hasbeen jumping for
  boolean gJump = true; //checks if player is juming from the ground
  float jumpSpd = -20; //jump speed
  
  //player states
  final int IDLE = 0;
  final int WALK = 1;
  final int RUN = 2;
  final int CROUCH = 3;
  final int JUMP = 4;
  final int SLASH = 5;
  final int SHOOT = 6;
  int state = IDLE; //initial state
  
  //hitboxes
  Entity hurtbox = new Entity(new PVector(pos.x, pos.y), new PVector(40, 45)); //hitbox that allows player to take damage
  Entity hitbox = new Entity(new PVector(pos.x+50, pos.y), new PVector(60, 120)); //sword hitbox
  
  //upgrades
  float stun = 1, swordDmg = 5, gunDmg = 1, stunDmgMult = 2; //stun applied per attack, sword damage, gun damage and stun damage mutiplier when boss is in break state
  
  Player(PVector pos, PVector dim, PImage img){ //player constructor
    super(pos, dim, new PVector(0, 0), img);
    maxHealth = 10;
    health = maxHealth;
    spd = spdBase;
  }
  
  void setUpgrades(int fire, float water, float earth, int wind, float lightning, float light, float dark){ //sets variables to match global upgrade variables
    //fire upgrade
    gunDmg = 1 + fire; //increases gun damage
    
    //water upgrade
    maxHealth = 10 + 2*water; //increases max health
    health = maxHealth;
    
    //earth upgrade
    stun = 1 + earth; //increases stun amount
    
    //wind upgrade
    maxStamina = 250 + 50*wind; //increases max stamina
    maxJumps = 1 + wind; //increases max number of jumps
    jumps = maxJumps;
    
    //lightning upgrade
    spdBase = 2 + 0.5*lightning; //increases base speed
    spd = spdBase;
    
    //light upgrade
    swordDmg = 5 + 5*light; //increases sword damage
    
    //dark upgrade
    stunDmgMult = 2 + dark; //increases stun damage multiplier
  }
  
  void update(){ //updates player obejct
    super.update();
    
    //movement controller
    //jumping
    if(u && jumps > 0){ //jump handling
      if(jumpFrames < 15) accel(new PVector(0, jumpSpd)); //accelerate player upwards if jump button is being held down
      jumpFrames++;
      jumpSpd += 1;
      gJump = false; //disable ground jump
    }
    else if(onGround || !u){ //reset jump variables if on ground or if up button is not held
      if(onGround) jumps = maxJumps;
      jumpFrames = 0;
      jumpSpd = -20;
      if(onGround) gJump = true; 
    }
    if(!onGround && gJump){ //reset variables for midair jump
      gJump = false;
      if(p.jumps > 0) jumps--;
    }
    
    //crouching
    if(d && onGround) state = CROUCH;
    
    //horizontal movement
    if(l){
      if(!sword || sword && !onGround) accel(new PVector(0-spd, 0)); //move left if button is being held, except when player is on ground and melee attacking
      lookLeft = true;
      if(!sword){
        if(dash)state = RUN; //initiate dashing
        else state = WALK;
      }
    }
    if(r){
      if(!sword || sword && !onGround) accel(new PVector(spd, 0)); //move right if button is being held, except when player is on ground and melee attacking
      lookLeft = false;
      if(!sword){
        if(dash)state = RUN; //initiate dashing
        else state = WALK;
      }
    }
    
    //idle checker
    if(abs(vel.x) <= 0.125 && vel.y <= 0.125 && !d) state = IDLE;
    
    //attack
    if(sword){
      playerSword.play(0);
      state = SLASH;
    }
    
    //dash system
    if(stamina < maxStamina && !dash) stamina++; //refill stamina when not dahing
    if(dash && stamina > 0){
      dashHeld = true;
      spd = 2*spdBase; //increase speed when dashing
      stamina -= 2.5; //reduce stamina by 5 every frame when in dash state
      if(stamina <= 0){ //if stamina reaches below 0, player is forced out of dash state and stamina resets to 0
        stamina = 0;
        dash = false;
      }
    }
    else spd = spdBase; //revert to base speed if not dashing

    //hurtbox state control
    if(state == CROUCH) hurtbox.pos.y = pos.y + 30;
    else hurtbox.pos.y = pos.y;
    if(state == SLASH && !lookLeft) hurtbox.pos.x = pos.x - 20;
    else if(state == SLASH && lookLeft) hurtbox.pos.x = pos.x + 20;
    else hurtbox.pos.x = pos.x;
    
    //hitbox position control
    if(lookLeft) hitbox.pos.x = pos.x-50;
    else hitbox.pos.x = pos.x+50;
    hitbox.pos.y = pos.y-20;
    
    //footbox position control
    footbox.pos.x = pos.x;
    footbox.pos.y = pos.y+7*dim.y/16;
    
    //boss damage checking
    //player hurtbox
    if(hurtbox.intersects(b.hurtbox) && !hitOnce && b.isAlive){
      p.damage(1);
      hitOnce = true;
    }
    else if(!hurtbox.intersects(b.hurtbox)) hitOnce = false;
    
    //melee attacks
    if(sword && hitbox.intersects(b.hurtbox) && !swordOnce && b.isAlive){
      b.damage(swordDmg);
      swordOnce = true;
    }
    else if(state != SLASH) swordOnce = false;
    
    //projectiles
    checkProjectiles();
  }
  
  void checkProjectiles(){ //checks all projectile associated with the player
    //bullet checking
    for(int i = 0; i<projs.size(); i++){
      Projectile pr = projs.get(i);
      pr.update();
      pr.drawMe(1);
      if(pr.intersects(b.hurtbox) && b.isAlive){ //if bullet intersects boss
        b.damage(gunDmg);
        projs.remove(pr);
      }
      if(pr.pos.x-pr.dim.x > width || pr.pos.x+pr.dim.x < 0) projs.remove(pr); //if projectiles exit screen remove from arraylist
    }
  }
  
  void drawMe(){ //draws player based on state
    switch(state){ //checks player state and assigns images accordingly
      case IDLE: //when player is idle
        imgHeader = "player/idle/idle";
        spriteFrames = 6;
        break;
      case WALK: //when player is walking
        imgHeader = "player/walk/walk";
        spriteFrames = 4;
        break;
      case RUN: //when player is running
        imgHeader = "player/run/run";
        spriteFrames = 6;
        break;
      case CROUCH: //when player is crouching
        imgHeader = "player/crouch/crouch";
        spriteFrames = 6;
        break;
      case SLASH: //when player is melee attacking
        imgHeader = "player/sword/sword";
        spriteFrames = 4;
        break;
    }
    if(frameCount%5 == 0)animFrame++; //update once per 5 frames
    if(state == SLASH && animFrame == spriteFrames-1) sword = false; //disable sword state if sword animation ends
    if(animFrame > spriteFrames-1) animFrame = 0; //if current frame of animation exceeds total frames in animation reset
    img = loadImage(imgHeader + '_' + animFrame + ".png"); //load image
    
    pushMatrix();
    translate(pos.x, pos.y);
    if(lookLeft) scale(-1, 1);
    imageMode(CENTER); 
    
    if(shot){ //if player is shooting
      float gunX = 0, gunY = 0;
      if(state == CROUCH) { //change gun arm position if player is crouching
        gunX = 10;
        gunY = 35;
      }
      image(loadImage("player/gun/gun_" + gunFrame + ".png"), gunX, gunY); //loads arm behind player
      if (gunFrame == 0){ //fire when gun is at start of animation
        fire();
      }
      if(gunFrame >= 5){ //if gun animation is or exceeds 6 frame (number of frames in gun animation)
        gunFrame = 0;
      }
      else gunFrame++;
    }
    
    if(iTimer >= 0 && iTimer%6 != 0 || iTimer == -1) image(img, 0, 0); //draws image, blinks if player is currently invincible
    if(iTimer >= 0) iTimer--; //reduce invincibility frame timer if timer is above 0
    popMatrix();
    
    //debug
    if(debug){
      hurtbox.drawMe(color(0, 0, 255), 75, 0);
      if(sword)hitbox.drawMe(color(255, 0, 0), 75, 0);
      footbox.drawMe(color(160, 160, 0), 75, 0);
    }
  }
  
  void fire(){ //method for player to fire
    float prVel = 50;
    float projYPos = pos.y-30;
    if(lookLeft) prVel = -prVel;
    if(state == CROUCH) projYPos += 30; //change projectile position if player is crouching
    projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(60, 5), new PVector(prVel, 0), 1, 0, color(160,80,0))); //fires projectile
    playerFire.play(0);
  }
  
  void damage(float d){ //method to damage player
    if(iTimer < 0){ //if player is not invincible
      health -= d;
      playerHit.play(0);
      iTimer = iFrames;
    }
    if(health <= 0) isAlive = false; //kill player if health drops below 0
  }
  
  void drawHUD(){ //draws HUD relevant to player info
    //stamina bar
    rectMode(CORNER);
    fill(0);
    textSize(20);
    text("SP", 50, height-25-(25*maxHealth));
    stroke(0);
    strokeWeight(3);
    fill(0, 0);
    rect(50, height-20-maxStamina, 20, maxStamina);
    fill(160,160,0);
    rect(50, height-20-stamina, 20, stamina);
    
    //health bar
    fill(0);
    text("HP", 15, height-25-(25*maxHealth));
    stroke(0);
    strokeWeight(3);
    fill(0,0);
    rect(20, height-20-(25*maxHealth), 20, 25*(maxHealth));
    fill(160, 0, 0);
    for(int i = 0; i < health; i++){
      rect(20, height-45-(25*i), 20, 25); //draw health indicator blocks based on how much hp player has
    }
  }
}
