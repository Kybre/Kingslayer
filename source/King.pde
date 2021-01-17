class King extends Boss{ //king class
  String imgHeader; //string storing common image directory
  int spriteFrames, animFrame = 0, frameDelay = 0, homingCount = 0;
  Projectile source; //placeholder projectile for projectile emitting other projectiles
  Projectile beam1, beam2; //placeholder projectile for beam objects
  King(PVector pos, PVector dim, PImage img, String name, float sdm, int diff){
    super(pos, dim, img, name, sdm, diff);
    
    //set health and stun based on difficulty parameters
    maxHealth = 300 + 300*diff;
    health = maxHealth;
    maxStun = maxHealth/2;
    stun = maxStun;
    
    warnImages = new ArrayList<PImage>(); //reset and add relevant images to warnImages array
    for(int i = 0; i < 7; i++){
      PImage w = loadImage("king/warn/king_warn_a" + i + ".png");
      warnImages.add(w);
    }
    
    //sets hitboxes
    hurtbox = new Entity(new PVector(pos.x, pos.y), new PVector(3*dim.x/4, 3*dim.y/4));
    jumpbox = new Entity(new PVector(pos.x, pos.y-dim.y/2+50), new PVector(dim.x, 50));
    footbox = new Entity(new PVector(pos.x, pos.y+dim.y/4), new PVector(dim.x/2-20, dim.y/8));
    
    //set jump variables
    baseJumpSpd = -22;
    jumpSpd = baseJumpSpd;
  }
  
  void update(){
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
        firePattern = (int)random(0, 3);
        break;
      case 1:
        firePattern = (int)random(0, 5);
        break;
      case 2:
        firePattern = (int)random(5, 8);
        break;
      case 3:
        firePattern = (int)random(6, 10);
        break;
    }
    if(debug) println("King Phase: " + phase + ", Pattern: " + firePattern);
    //firePattern = 9;
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
        case 0: //triple homing shot
          warn(warnImages.get(4), 30);
          break;
          
        case 1: //controlled rain
          warn(warnImages.get(4), 30);
          break;
          
        case 2: //point converge
          warn(warnImages.get(4), 30);
          break;
          
        case 3: //projectile from projectile
          warn(warnImages.get(4), 30);
          break;
          
        case 4: //growing projectile
          warn(warnImages.get(3), 30);
          break;
          
        case 5: //converging vertical lasers
          warn(warnImages.get(1), 30);
          
          //indicator below beam location
          rectMode(CENTER);
          fill(160,80,0,225);
          rect(120, height-100, p.footbox.dim.x, 50);
          rect(width-120, height-100, p.footbox.dim.x, 50);
          break;
          
        case 6: //diverging vertical lasers
          warn(warnImages.get(1), 30);
          
          //indicator below beam location
          rectMode(CENTER);
          fill(160,80,0,225);
          rect(width/2, height-100, p.footbox.dim.x, 50);
          break;
          
        case 7: //long vertical projectile
          warn(warnImages.get(6), 30);
          break;
          
        case 8: //floor burst
          warn(warnImages.get(5), 30);
          break;
          
        case 9: //cross beam
          warn(warnImages.get(2), 30);
          
          //indicator for beam location
          fill(0,0,160,125);
          if(lookLeft) rect(0, p.pos.y, 50, p.footbox.dim.x);
          else rect(width, p.pos.y, 50, p.footbox.dim.x);
          rect(p.pos.x, height-100, p.footbox.dim.x, 50);
          break;
      }
    }
    else switch(pattern){ //fire projectiles
      case 0: //triple homing shot
        prVel = 1;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot){ //add projectiles to arraylists
          homingCount = 0;
          frameDelay = 0;
          homing = new ArrayList<Projectile>();
          for(int i = 0; i < 3; i++){
            Projectile h;
            if(lookLeft) h = new Projectile(new PVector(pos.x - 100, projYPos + (-30 + (30*i))), new PVector(15, 15), new PVector(0, 0), 1, 1, color(0,80,160));
            else h = new Projectile(new PVector(pos.x + 100, projYPos + (-30 + (30*i))), new PVector(15, 15), new PVector(0, 0), 1, 1, color(0,80,160));
            homing.add(h);
            projs.add(h);
          }
          bossFire.play(0);
          shot = true;
        }
        if(shotFrame >= 60){ //track player, init to 1 new projectile in the homing arraylist every 10 frames
          for(int i = 0; i < homingCount; i++){
            Projectile h = homing.get(i);
            if(h.pos.x > p.hurtbox.pos.x && prVel > 0 || h.pos.x < p.hurtbox.pos.x && prVel < 0) prVel = -prVel;
            h.accel(new PVector(prVel, (p.hurtbox.pos.y-h.pos.y)/p.hurtbox.pos.y));
          }
          if(frameDelay%10 == 0) homingCount++;
        }
        if(actionFrame >= 300) shot = false; //reset firing state
        frameDelay++;
        break;
        
      case 1: //rain from above
        prVel = 0;
        projYPos = p.pos.y;
        if(!shot){ //add projectile to arraylist
          Projectile pr = new Projectile(new PVector(p.pos.x, -25), new PVector(25, 25), new PVector(0, prVel), 1, 1, color(0,80,160));
          projs.add(pr);
          pr.gravity = true; //toggle gravity for projectile
          shot = true;
        }
        if(actionFrame%8 == 0){ //reset firing state every 8 frames (to add new projectile)
          shot = false;
          bossFire.play(0);
        }
        println(shotFrame);
        break;
        
      case 2: //large wave
        prVel = 30;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot){ //add projectiles to arraylist
          w1 = new Projectile(new PVector(pos.x, projYPos), new PVector(30, 30), new PVector(prVel, 0), 1, 1, color(0,80,160));
          w2 = new Projectile(new PVector(pos.x, projYPos), new PVector(30, 30), new PVector(prVel, 0), 1, 1, color(0,80,160));
          projs.add(w1);
          projs.add(w2);
          shot = true;
          bossFire.play(0);
        }
        else{ //update position based on trig curves
          w1.pos.y = projYPos + 240*sin(3*w1.angle);
          w2.pos.y = projYPos - 240*sin(3*w2.angle);
          w1.angle += PI/60;
          w2.angle += PI/60;
        }
        if(actionFrame >= 300) shot = false; //reset firing state
        break;
      
     case 3: //projectiles from projectiles
        prVel = 20;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot){ //add projectiles to arraylist
          source = new Projectile(new PVector(pos.x, projYPos), new PVector(30, 30), new PVector(prVel, 0), 1, 1, color(0,80,160));
          projs.add(source);
          shot = true;
          bossFire.play(0);
        }
        else if(actionFrame == 280){ //have more projectiles burst from original projectile in 8 cardinal directions, add them to arraylist
          prVel = 30;
          projs.add(new Projectile(new PVector(source.pos.x, source.pos.y), new PVector(20, 20), new PVector(prVel, 0), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(source.pos.x, source.pos.y), new PVector(20, 20), new PVector(prVel/1.5, prVel/1.5), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(source.pos.x, source.pos.y), new PVector(20, 20), new PVector(prVel/1.5, -prVel/1.5), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(source.pos.x, source.pos.y), new PVector(20, 20), new PVector(-prVel, 0), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(source.pos.x, source.pos.y), new PVector(20, 20), new PVector(-prVel/1.5, prVel/1.5), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(source.pos.x, source.pos.y), new PVector(20, 20), new PVector(-prVel/1.5, -prVel/1.5), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(source.pos.x, source.pos.y), new PVector(20, 20), new PVector(0, prVel), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(source.pos.x, source.pos.y), new PVector(20, 20), new PVector(0, -prVel), 1, 1, color(0,80,160)));
          bossFire.play(0);
        }
        if(actionFrame >= 300) shot = false; //reset firing state
        break;
      
     case 4: //growing projectile
        prVel = 10;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot){ //add projectile to arraylist
          source = new Projectile(new PVector(pos.x, projYPos), new PVector(30, 30), new PVector(prVel, 0), 1, 1, color(160,160,0));
          projs.add(source);
          bossFire.play(0);
          shot = true;
        }
        else if(actionFrame >= 260){ //start projectile growth after set amount of time
          source.dim.mult(1.05);
          source.vel.mult(1.05);
          bossFire.play(0);
        }
        if(actionFrame >= 300) shot = false; //reset firing state
        break;
     
     case 5: //converging vertical lasers
        prVel = 0.25;
        if(!shot && shotFrame > 30){ //add beams to arraylist
          beam1 = new Projectile(new PVector(120, 0), new PVector(80, 10), new PVector(1, 0), 2, 0, color(160,80,0,225));
          beam2 = new Projectile(new PVector(width-120, 0), new PVector(80, 10), new PVector(-1, 0), 2, 0, color(160,80,0,225));
          projs.add(beam1);
          projs.add(beam2);
          bossLaser.play(0);
          shot = true;
        }
        else if(shot && actionFrame < 300 && beam1.dim.y < height && beam2.dim.y < height){ //extend both beams vertically downwards
          beam1.beamY(prVel);
          beam2.beamY(prVel);
        }
        if(actionFrame >= 260){ //time gated horizontal acceleration to give player enough time to react
          beam1.vel.x += 0.5;
          beam2.vel.x -= 0.5;
        }
        if(actionFrame >= 300){ 
          shot = false; //reset firing state
          //stops horizontal movement
          beam1.vel.x = 0;
          beam2.vel.x = 0;
          //have beam exit scene vertically
          beam1.vel.y += 100;
          beam2.vel.y += 100;
        }
        break;
        
      case 6: //diverging vertical lasers
        prVel = 0.25;
        if(!shot && shotFrame > 30){ //add beams to arraylist
          beam1 = new Projectile(new PVector(width/2, 0), new PVector(80, 10), new PVector(1, 0), 2, 0, color(160,80,0,225));
          beam2 = new Projectile(new PVector(width/2, 0), new PVector(80, 10), new PVector(-1, 0), 2, 0, color(160,80,0,225));
          projs.add(beam1);
          projs.add(beam2);
          bossLaser.play(0);
          shot = true;
        }
        else if(shot && actionFrame < 300 && beam1.dim.y < height && beam2.dim.y < height){ //extend both beams vertically downwards
          beam1.beamY(prVel);
          beam2.beamY(prVel);
        }
        if(actionFrame >= 260){ //time gated horizontal acceleration to give player enough time to react
          beam1.vel.x += 0.5;
          beam2.vel.x -= 0.5;
        }
        if(actionFrame >= 300){
          shot = false; //reset firing state
          //stops horizontal movement
          beam1.vel.x = 0;
          beam2.vel.x = 0;
          //have beam exit scene vertically
          beam1.vel.y += 100;
          beam2.vel.y += 100;
        }
        break;
      
      case 7: //large vertical projectile
        prVel = 10;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel;
        if(!shot){ //add projectile to arraylist
          source = new Projectile(new PVector(pos.x, projYPos), new PVector(30, 400), new PVector(prVel, 0), 1, 0, color(160,0,160,180));
          projs.add(source);
          bossFire.play(0);
          shot = true;
        }
        else if(actionFrame >= 260){ //time-gated acceleration to give player time to react
          source.vel.mult(1.1);
          playerSword.play(0);
        }
        if(actionFrame >= 300) shot = false; //reset firing state
        break;
        
      case 8: //floor burst
        prVel = 0.25;
        if(!shot && shotFrame > 30){ //add beam to arraylist
          beam = new Projectile(new PVector(width/2, height), new PVector(width-120, 10), new PVector(0, 0), 2, 0, color(255,255,255,180));
          projs.add(beam);
          bossLaser.play(0);
          shot = true;
        }
        else if(shot && actionFrame < 300 && beam.dim.y < 240){ //extend beam vertically upwards
          beam.beamY(-prVel);
        }
        if(actionFrame >= 300){
          shot = false; //reset firing state
          beam.vel.y += 50; //have beam exit scene vertically
        }
        break;
        
      case 9: //cross beam
        prVel = 0.25;
        projYPos = p.pos.y;
        if(lookLeft) prVel = -prVel;
        if(!shot && shotFrame > 30){ //add beams to arraylist
          //horizontal beam
          if(prVel > 0) beam1 = new Projectile(new PVector(-10, projYPos), new PVector(10, 80), new PVector(0, 0), 2, 0, color(0,80,160,225));
          else beam1 = new Projectile(new PVector(width+10, projYPos), new PVector(10, 80), new PVector(0, 0), 2, 0, color(0,80,160,225));
          projs.add(beam1);
          
          //vertical beam
          beam2 = new Projectile(new PVector(p.pos.x, 0), new PVector(80, 10), new PVector(0, 0), 2, 0, color(0,80,160,225));
          projs.add(beam2);
          
          bossLaser.play(0);
          shot = true;
        }
        else if(shot && actionFrame < 300){
          if(beam1.dim.x < width) beam1.beamX(prVel); //extend beam horizontally
          if(beam2.dim.y < height) beam2.beamY(abs(prVel)); //extend beam vertically downwards
        }
          
        if(actionFrame >= 300){
          shot = false; //reset firing state
          //have horizontal beam exit scene
          if(lookLeft) beam1.vel.x -= 100;
          else beam1.vel.x += 100;
          beam2.vel.y += 100; //have vertical beam exit scene
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
  
  void drawMe(){ //draws king based on state 
    switch(state){
      case IDLE: //when idle
        imgHeader = "king/idle/king_idle_";
        spriteFrames = 4;
        break;
      case WALK: //when moving
        imgHeader = "king/walk/king_walk_";
        spriteFrames = 4;
        break;
      case ATK: //when attacking
        imgHeader = "king/warn/king_warn_";
        spriteFrames = 4;
        break;
      case BREAK: //when in break state
        imgHeader = "king/break/king_break_";
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
