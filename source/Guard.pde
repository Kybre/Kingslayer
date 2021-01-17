class Guard extends Boss{ //Guard Boss object
  String imgHeader; //string storing common image directory
  int spriteFrames, animFrame = 0; //number of frames in animation, current animation frame respectively
  
  Guard(PVector pos, PVector dim, PImage img, String name, float sdm, int diff){ //guard constructor
    super(pos, dim, img, name, sdm, diff); //super
    
    //set health and stun based on difficulty parameters
    maxHealth = 250 + 250*diff;
    health = maxHealth;
    maxStun = maxHealth/2;
    stun = maxStun;
    
    warnImages = new ArrayList<PImage>(); //reset and add relevant images to warnImages array
    for(int i = 0; i < 7; i++){
      PImage w = loadImage("guard/warn/hg_warn_a" + i + ".png");
      warnImages.add(w);
    }
    
    //set hitboxes
    hurtbox = new Entity(new PVector(pos.x, pos.y), new PVector(3*dim.x/4, 3*dim.y/4));
    jumpbox = new Entity(new PVector(pos.x, pos.y-dim.y/2+50), new PVector(dim.x, 50));
    footbox = new Entity(new PVector(pos.x, pos.y+dim.y/4), new PVector(dim.x/2-20, dim.y/8));
    
    //set jump variables
    baseJumpSpd = -20;
    jumpSpd = baseJumpSpd;
  }
  
  void update(){ //updates boss
    super.update();
  }
  
  void adjustHitboxes(){ //adjust boss hitboxes to stay on the boss
    footbox.pos.x = pos.x;
    footbox.pos.y = pos.y+7*dim.y/16;
    if(lookLeft) hurtbox.pos.x = pos.x+10;
    else hurtbox.pos.x = pos.x-10;
    hurtbox.pos.y = pos.y;
    jumpbox.pos = new PVector(pos.x, pos.y-dim.y/2+25);
  }
  
  void setFirePattern(){ //set firing pattern
    switch(phase){ //set of patterns available to boss changes based on boss phase
      case 0:
        firePattern = (int)random(0, 4);
        break;
      case 1:
        firePattern = (int)random(0, 6);
        break;
      case 2:
        firePattern = (int)random(4, 8);
        break;
      case 3:
        firePattern = (int)random(6, 10);
        break;
    }
    //firePattern = 6;
    if(debug) println("Guard Phase: " + phase + ", Pattern: " + firePattern);
  }
  
  void warn(PImage wImg, float frames){ //method to toggle boss between 2 frames to create warning animation, wImg = other image used aside from base, frames = time allotted to warning player
   state = WARN;
   if(shotFrame%10 == 0 && warnToggle == false) warnToggle = true;
   else if(shotFrame%10 == 0 && warnToggle == true) warnToggle = false;
   if(!warnToggle || shotFrame >= frames-1) img = warnImages.get(0);
   else img = wImg;
  }
  
  void fire(int pattern){ //method for boss to fire
    float prVel, projYPos, rad; //temp variables: projectile velocity, projectile Y position and orbit radius respectively
    if(shotFrame == 0) bossWarn.loop(1); //play boss warning sound at beginning
    if(shotFrame < 30){ //spend 1st 30 frames of firing state warning player of what's coming
      switch(pattern){
        case 0: //linear shot
          warn(warnImages.get(4), 30);
          break;
          
        case 1: //spread
          warn(warnImages.get(4), 30);
          break;
          
        case 2: //point converge
          warn(warnImages.get(4), 30);
          break;
          
        case 3: //wave
          warn(warnImages.get(4), 30);
          break;
          
        case 4: //orbit
          warn(warnImages.get(3), 30);
          break;
          
        case 5: //homing
          warn(warnImages.get(4), 30);
          break;
          
        case 6: //rain
          warn(warnImages.get(5), 30);
          break;
          
        case 7: //AoE
          warn(warnImages.get(6), 30);
          break;
          
        case 8: //beam on player
          warn(warnImages.get(1), 30);
          
          //indicator below player
          rectMode(CENTER);
          fill(160,80,0,225);
          rect(p.footbox.pos.x, height-100, p.footbox.dim.x, 50);
          break;
          
        case 9: //horizontal beam
          warn(warnImages.get(2), 30);
          break;
      }
    }
    else switch(pattern){ //fire projectiles
      case 0: //linear shot
        prVel = 50;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot){ //add projectiles to arraylist
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(60, 5), new PVector(prVel, 0), 1, 0, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos + 20), new PVector(60, 5), new PVector(prVel, 0), 1, 0, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos - 20), new PVector(60, 5), new PVector(prVel, 0), 1, 0, color(0,80,160)));
          bossFire.play(0);
          shot = true;
        }
        if(shotFrame >= 60){ //reset firing state
          shot = false;
          shotFrame = 0;
        }
        break;
        
      case 1: //spread
        prVel = 40;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot){ //add projectiles to arraylist
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(15, 15), new PVector(prVel, 0), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos + 20), new PVector(15, 15), new PVector(prVel, 3), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos - 20), new PVector(15, 15), new PVector(prVel, -3), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos + 40), new PVector(15, 15), new PVector(prVel, 6), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos - 40), new PVector(15, 15), new PVector(prVel, -6), 1, 1, color(0,80,160)));
          bossFire.play(0);
          shot = true;
        }
        if(shotFrame >= 60){ //reset firing state
          shot = false;
          shotFrame = 0;
        }
        break;
        
      case 2: //point converge
        prVel = 40;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot){ //add projectiles to arraylist
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(15, 15), new PVector(prVel, 0), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos + 20), new PVector(15, 15), new PVector(prVel, -3), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos - 20), new PVector(15, 15), new PVector(prVel, 3), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos + 40), new PVector(15, 15), new PVector(prVel, -6), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos - 40), new PVector(15, 15), new PVector(prVel, 6), 1, 1, color(0,80,160)));
          bossFire.play(0);
          shot = true;
        }
        if(shotFrame >= 60){ //reset firing state
          shot = false;
          shotFrame = 0;
        }
        break;
      
     case 3: //wave
        prVel = 20;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot){ //add projectiles to arraylist
          w1 = new Projectile(new PVector(pos.x, projYPos + 40), new PVector(20, 20), new PVector(prVel, 0), 1, 1, color(0,80,160));
          w2 = new Projectile(new PVector(pos.x, projYPos - 40), new PVector(20, 20), new PVector(prVel, 0), 1, 1, color(0,80,160));
          projs.add(w1);
          projs.add(w2);
          bossFire.play(0);
          shot = true;
        }
        else{ //update position based on trig curves
          w1.pos.y = projYPos + 60*cos(3*w1.angle);
          w2.pos.y = projYPos - 60*cos(3*w2.angle);
          w1.angle += PI/60;
          w2.angle += PI/60;
        }
        if(actionFrame >= 300) shot = false; //reset firing state
        break;
      
     case 4: //orbit
        prVel = 0.08;
        rad = 200;
        projYPos = pos.y - dim.y/2;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot && shotFrame > 30){ //add projectiles to arraylist
          Projectile pr = new Projectile(new PVector(pos.x, projYPos), new PVector(30, 30), new PVector(prVel, 0), 1, 1, color(160,160,0));
          projs.add(pr);
          pr.orbitTarget = this;
          pr.radius = rad;
          pr.orbit = true;
          bossOrbit.play(0);
          shot = true;
        }
        if(actionFrame >= 300) shot = false; //reset firing state
        break;
     
     case 5: //homing
        prVel = 1;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot){ //add projectiles to arraylist
          homing = new ArrayList<Projectile>();
          for(int i = 0; i < 4; i++){
            Projectile h = new Projectile(new PVector(pos.x, projYPos + (-20 + (10*i))), new PVector(15, 15), new PVector(0, -4 + (2*i)), 1, 1, color(0,80,160));
            homing.add(h);
            projs.add(h);
          }
          bossFire.play(0);
          shot = true;
        }
        if(shotFrame >= 60){ //track player
          for(int i = 0; i < homing.size(); i++){
            Projectile h = homing.get(i);
            if(h.proximity(p.hurtbox, width)){
              if(h.pos.x > p.hurtbox.pos.x && prVel > 0 || h.pos.x < p.hurtbox.pos.x && prVel < 0) prVel = -prVel; //flip movement direction if player is on other side of motion
              h.accel(new PVector(prVel, (p.hurtbox.pos.y-h.pos.y)/p.hurtbox.pos.y)); //accelerate towards player position
            }
          }
        }
        if(actionFrame >= 300) shot = false; //reset firing state
        break;
        
      case 6: //rain
        prVel = 0;
        projYPos = -60;
        if(actionFrame%8 == 0)bossFire.play(0);
        if(shotFrame > 30 && actionFrame <= 300 && actionFrame%5 == 0){ //add projectiles to arraylist in intervals of 5 frames
          Projectile pr = new Projectile(new PVector(random(100, width-100), projYPos), new PVector(30, 30), new PVector(0, prVel), 1, 1, color(160));
          projs.add(pr);
          pr.gravity = true; //toggle projectiles to be affected by gravity
        }
        break;
      
      case 7: //AoE
        prVel = 2;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot && shotFrame > 30){ //add projectile to arraylist
          if(prVel > 0)projs.add(new Projectile(new PVector(pos.x+110, projYPos), new PVector(240, 240), new PVector(prVel, 0), 1, 1, color(20, 100)));
          else projs.add(new Projectile(new PVector(pos.x-110, projYPos), new PVector(240, 240), new PVector(prVel, 0), 1, 1, color(20, 100)));
          shot = true;
          bossFire.play(0);
        }
        if(actionFrame >= 360) shot = false; //reset firing state
        break;
        
      case 8: //player converge beam
        prVel = 0.25;
        if(!shot && shotFrame > 30){ //add beam to arraylist
          beam = new Projectile(new PVector(p.pos.x, 0), new PVector(80, 10), new PVector(0, 0), 2, 0, color(160,80,0,225));
          projs.add(beam);
          bossLaser.play(0);
          shot = true;
        }
        else if(shot && actionFrame < 300 && beam.dim.y < height) beam.beamY(prVel); //extend beam downwards
        if(actionFrame >= 300){
          shot = false; //reset firing state
          beam.vel.y += 100; //have beam exit scene
        }
        break;
        
      case 9: //beam
        prVel = 0.25;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel;
        if(!shot && shotFrame > 30){ //add beam to arraylist
          if(prVel > 0) beam = new Projectile(new PVector(pos.x+5, projYPos), new PVector(10, 80), new PVector(0, 0), 2, 0, color(0,80,160,225));
          else beam = new Projectile(new PVector(pos.x-5, projYPos), new PVector(10, 80), new PVector(0, 0), 2, 0, color(0,80,160,225));
          projs.add(beam);
          bossLaser.play(0);
          shot = true;
        }
        else if(shot && actionFrame < 300 && beam.dim.x < width) beam.beamX(prVel); //extend beam horizontally
        if(actionFrame >= 300){
          shot = false; //reset firing state
          //have beam exit scene
          if(lookLeft) beam.vel.x -= 100;
          else beam.vel.x += 100;
        }
        break;
        
      default: //default setting
        prVel = 50;
        projYPos = pos.y-30;
        if(!shot){
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(60, 5), new PVector(prVel, 0), 1, 0, color(0,80,160,225)));
          shot = true;
        }
        if(shotFrame >= 60){
          shot = false;
          shotFrame = 0;
        }
        break;
    }
    shotFrame++;
  }
  
  void drawMe(){ //draws guard based on state
    switch(state){ //checks guard state and assigns images accordingly
      case IDLE: //when idle
        imgHeader = "guard/idle/hg_idle_";
        spriteFrames = 4;
        break;
      case WALK: //when moving
        imgHeader = "guard/walk/hg_walk_";
        spriteFrames = 4;
        break;
      case ATK: //when attacking
        imgHeader = "guard/warn/hg_warn_";
        spriteFrames = 4;
        break;
      case BREAK: //when break is active
        imgHeader = "guard/break/hg_break_";
        spriteFrames = 4;
        break;
    }
    
    if(frameCount%5 == 0)animFrame++; //update once per 5 frames
    if(animFrame > spriteFrames-1) animFrame = 0; //if current frame of animation exceeds total frames in animation reset
    if(state != WARN) img = loadImage(imgHeader + animFrame + ".png"); //load image if boss isn't in warning animation
    if(state == ATK && animFrame == spriteFrames-1) state = WARN; //switch to warning animation when boss is done pre-attack animation
    
    pushMatrix();
    translate(pos.x, pos.y);
    //draw the image
    if(!lookLeft) scale(-1, 1);
    imageMode(CENTER);
    image(img, 0, 0);
    popMatrix();
  }
}
