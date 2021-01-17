class Queen extends Boss{ //queen class
  String imgHeader; //string storing common image directory
  int spriteFrames, animFrame = 0; //number of total frames in animation, current animation frame respectively
  ArrayList<Projectile> orbitProjs = new ArrayList<Projectile>(); //dummy arraylist for all orbitting projectiles
  ArrayList<Projectile> beams = new ArrayList<Projectile>(); //dummy arraylist for all beams
  ArrayList<Projectile> orbs = new ArrayList<Projectile>(); //dummy arraylist for all orbs
  
  Queen(PVector pos, PVector dim, PImage img, String name, float sdm, int diff){ //queen constructor
    super(pos, dim, img, name, sdm, diff);
    
    //set health and stun based on difficulty parameters
    maxHealth = 275 + 275*diff;
    health = maxHealth;
    maxStun = maxHealth/2;
    stun = maxStun;
    
    
    warnImages = new ArrayList<PImage>(); //reset and add relevant images to warnImages array
    for(int i = 0; i < 7; i++){
      PImage w = loadImage("queen/warn/qn_warn_a" + i + ".png");
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
  
  void update(){ //updates queen object
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
        firePattern = (int)random(3, 7);
        break;
      case 3:
        firePattern = (int)random(5, 10);
        break;
    }
    //firePattern = 5;
    if(debug) println("Queen Phase: " + phase + ", Pattern: " + firePattern);
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
        case 0: //single homing
          warn(warnImages.get(4), 30);
          break;
          
        case 1: //8 directions
          warn(warnImages.get(4), 30);
          break;
          
        case 2: //slow peristing vertical projectile
          warn(warnImages.get(4), 30);
          break;
          
        case 3: //orbit out
          warn(warnImages.get(3), 30);
          break;
          
        case 4: //orbit in
          warn(warnImages.get(3), 30);
          break;
          
        case 5: //double horizontal line orbs
          warn(warnImages.get(6), 30);
          break;
          
        case 6: //horizontal -> vertical line orb
          warn(warnImages.get(6), 30);
          break;
          
        case 7: //moving horizontal beam
          warn(warnImages.get(2), 30);
          break;
          
        case 8: //spaced vertical beams
          warn(warnImages.get(1), 30);
          
          //indicator below beam locations
          rectMode(CENTER);
          fill(160,80,0,225);
          for(int i = 0; i < 8; i++){
            rect(100+i*200, height-100, p.footbox.dim.x, 50);
          }
          break;
          
        case 9: //horizontal rain
          warn(warnImages.get(5), 30);
          break;
      }
    }
    else switch(pattern){    
      case 0: //single homing shot
        prVel = 0.75;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel;
        if(!shot){ //add projectile to arraylist
          h1 = new Projectile(new PVector(pos.x, projYPos-20), new PVector(25, 25), new PVector(0, 2), 1, 1, color(0,80,160));
          projs.add(h1);
          bossFire.play(0);
          shot = true;
        }
        if(shotFrame >= 30){ //track player
          Projectile h = projs.get(projs.size()-1);
          if(h.proximity(p.hurtbox, width)){
            if(h.pos.x > p.hurtbox.pos.x && prVel > 0 || h.pos.x < p.hurtbox.pos.x && prVel < 0) prVel = -prVel;
            h.accel(new PVector(prVel, (p.hurtbox.pos.y-h.pos.y)/p.hurtbox.pos.y)); //accelerate towards player
          }
        }
        if(actionFrame >= 300) shot = false; //reset firing state
        break;
        
      case 1: //8 cardinal directions
        prVel = 30;
        projYPos = pos.y;
        if(lookLeft) prVel = -prVel;
        if(!shot){ //add projectiles to arraylist
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(25, 25), new PVector(prVel, 0), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(25, 25), new PVector(prVel/1.5, prVel/1.5), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(25, 25), new PVector(prVel/1.5, -prVel/1.5), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(25, 25), new PVector(-prVel, 0), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(25, 25), new PVector(-prVel/1.5, prVel/1.5), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(25, 25), new PVector(-prVel/1.5, -prVel/1.5), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(25, 25), new PVector(0, prVel), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(25, 25), new PVector(0, -prVel), 1, 1, color(0,80,160)));
          bossFire.play(0);
          shot = true;
        }
        if(shotFrame >= 60){ //reset firing state
          shot = false;
          shotFrame = 0;
        }
        break;
        
      case 2: //slow peristing vertical projectile
        prVel = 2;
        projYPos = 0;
        if(!shot && shotFrame > 30){ //add projectile to arraylist
          projs.add(new Projectile(new PVector(random(150, width-150), projYPos), new PVector(300, 300), new PVector(0, prVel), 1, 1, color(0, 160, 0, 100)));
          bossFire.play(0);
          shot = true;
        }
        if(actionFrame >= 360) shot = false; //reset firing state
        break;
      
     case 3: //orbit out
        prVel = 0.04;
        rad = 50;
        projYPos = pos.y - dim.y/2;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot && shotFrame > 30){ //add projectiles to arraylist
          for(int i = 0; i < 8; i++){
            Projectile pr = new Projectile(new PVector(pos.x, rad), new PVector(30, 30), new PVector(prVel, 0), 1, 1, color(160,160,0));
            projs.add(pr);
            orbitProjs.add(pr);
            pr.angle = i*(PI/4);
            pr.orbitTarget = this; //set orbit target to queen
            pr.radius = rad;
            pr.orbit = true; //initiate orbit
            bossOrbit.play(0);
            shot = true;
          }
        }
        if(shot && actionFrame < 300){ //increase orbit radius over time
          for(int i = 0; i < orbitProjs.size(); i++){
            Projectile h = orbitProjs.get(i);
            h.radius += 10;
          }
        }
        if(actionFrame >= 300) shot = false; //reset firing state
        break;
      
     case 4: //orbit in
        prVel = 0.04;
        rad = height/2;
        projYPos = pos.y - dim.y/2;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot && shotFrame > 30){ //add projectiles to arraylist
          for(int i = 0; i < 8; i++){
            Projectile pr = new Projectile(new PVector(pos.x, rad), new PVector(30, 30), new PVector(prVel, 0), 1, 1, color(160,160,0));
            projs.add(pr);
            orbitProjs.add(pr);
            pr.angle = i*(PI/4);
            pr.orbitTarget = this; //set orbit target to queen
            pr.radius = rad;
            pr.orbit = true; //initiate orbit
            bossOrbit.play(0);
            shot = true;
          }
        }
        if(shot && actionFrame < 300){ //decrease orbit radius over time
          for(int i = 0; i < orbitProjs.size(); i++){
            Projectile h = orbitProjs.get(i);
            h.radius -= 10;
          }
        }
        if(actionFrame >= 300) shot = false; //reset firing state
        break;
     
     case 5: //orbs firing horizontal lines at player
       prVel = 20;
       if(!shot && shotFrame > 30){ //add orbs to projectile arraylist
         Projectile orb;
         for(int i = 0; i < 7; i++){
           orb = new Projectile(new PVector(200+i*200, -100), new PVector(30, 30), new PVector(0, prVel), 1, 1, color(50,0,50,225));
           projs.add(orb);
           orbs.add(orb);
         }
         shot = true;
         bossLaser.play(0);
       }
      if(shot){ //action sequence for orbs
          for(int i = 0; i < orbs.size(); i++){
            Projectile o = orbs.get(i);
            if(o.vel.y > 0) o.vel.y -= 0.25; //slow projectile down
            if(actionFrame >= 280){
              if(actionFrame == 280){//fire horizontal laser, add to projectile arraylist
                projs.add(new Projectile(new PVector(o.pos.x, o.pos.y), new PVector(width*2, 25), new PVector(200, 0), 1, 0, color(50,0,50,200)));
                projs.add(new Projectile(new PVector(o.pos.x, o.pos.y), new PVector(width*2, 25), new PVector(-200, 0), 1, 0, color(50,0,50,200)));
                bossFire.play(0);
              }
              o.vel.y += 2;
            }
            if(actionFrame >= 290){
              if(actionFrame == 290){//fire horizontal laser, add to projectile arraylist
                projs.add(new Projectile(new PVector(o.pos.x, o.pos.y), new PVector(width*2, 25), new PVector(200, 0), 1, 0, color(50,0,50,200)));
                projs.add(new Projectile(new PVector(o.pos.x, o.pos.y), new PVector(width*2, 25), new PVector(-200, 0), 1, 0, color(50,0,50,200)));
                bossFire.play(0);
              }
              o.vel.y -= 1;
            }
          }
        }
        if(actionFrame >= 300){
          shot = false; //reset firing state
          for(int i = 0; i < orbs.size(); i++){ //have orbs leave the screen
            Projectile o = orbs.get(i);
            o.vel.y += 20;
          }
        }
       break;
        
      case 6: //orbs firing lines horizontally then vertically
        prVel = -20;
        if(!shot && shotFrame > 30){ //add orbs to projectile arraylist
          Projectile orb;
          for(int i = 0; i < 7; i++){
            orb = new Projectile(new PVector(200+i*200, height-75), new PVector(30, 30), new PVector(0, prVel), 1, 1, color(50,0,50,225));
            projs.add(orb);
            orbs.add(orb);
          }
          bossLaser.play(0);
          shot = true;
        }
        if(shot){ //action sequence for orbs
          for(int i = 0; i < orbs.size(); i++){
            Projectile o = orbs.get(i);
            if(o.vel.y < 0 && i%2 == 0) o.vel.y++;
            else if(o.vel.y < 0) o.vel.y += 0.5; //slow projectile down
            if(o.vel.y == 0) println(actionFrame);
            if(actionFrame == 280){ //fire horizontal laser, add to projectile arraylist
              projs.add(new Projectile(new PVector(o.pos.x, o.pos.y), new PVector(width*2, 25), new PVector(100, 0), 1, 0, color(50,0,50,200)));
              projs.add(new Projectile(new PVector(o.pos.x, o.pos.y), new PVector(width*2, 25), new PVector(-100, 0), 1, 0, color(50,0,50,200)));
              bossFire.play(0);
            }
            if(actionFrame == 290){ //fire vertical laser, add to projectile arraylist
              projs.add(new Projectile(new PVector(o.pos.x, o.pos.y), new PVector(25, height*2), new PVector(0, 100), 1, 0, color(50,0,50,200)));
              projs.add(new Projectile(new PVector(o.pos.x, o.pos.y), new PVector(25, height*2), new PVector(0, -100), 1, 0, color(50,0,50,200)));
              bossFire.play(0);
            }
          }
        }
        if(actionFrame >= 300){
          shot = false; //reset firing state
          for(int i = 0; i < orbs.size(); i++){ //have orbs leave the screen
            Projectile o = orbs.get(i);
            o.vel.y -= 20;
          }
        }
        break;
      
      case 7: //moving horizontal beam
        prVel = 0.25;
        projYPos = height-100;
        if(lookLeft) prVel = -prVel; //determine firing direction
        if(!shot && shotFrame > 30){ //add beam to projectile arraylist
          Projectile beam;
          if(prVel > 0) beam = new Projectile(new PVector(0, projYPos), new PVector(10, 80), new PVector(0, 0), 2, 0, color(0,80,160,225));
          else beam = new Projectile(new PVector(width, projYPos), new PVector(10, 80), new PVector(0, 0), 2, 0, color(0,80,160,225));
          projs.add(beam);
          bossLaser.play(0);
          shot = true;
        }
        else if(shot){
          Projectile bm = projs.get(projs.size()-1);
          if(bm.dim.x < width && actionFrame < 300)bm.beamX(prVel); //extend horizontal beam
          bm.pos.y -= 5; //move horizontal beam
        }
        
        if(actionFrame >= 300){
          shot = false; //reset firing state
          Projectile bm = projs.get(projs.size()-1);
          
          //have beam leave the screen horizontally
          if(prVel < 0) bm.vel.x -= 100;
          else bm.vel.x += 100;
        }
        break;
        
      case 8: //spaced vertical lasers
        prVel = 0.25;
        if(!shot && shotFrame > 30){ //add beams to projectile arraylist
          Projectile beam;
          for(int i = 0; i < 8; i++){
            beam = new Projectile(new PVector(100+i*200, 0), new PVector(80, 10), new PVector(0, 0), 2, 0, color(160,80,0,225));
            projs.add(beam);
            beams.add(beam);
          }
          bossLaser.play(0);
          shot = true;
        }
        else if(shot && actionFrame < 300){
          for(int i = 0; i < beams.size(); i++){
            Projectile b = beams.get(i);
            if(b.dim.y < height) b.beamY(prVel); //extend vertical beam
          }
        }
        if(actionFrame >= 300){
          shot = false; //reset firing state
          for(int i = 0; i < beams.size(); i++){ //have beams exit the screen vertically
            Projectile b = beams.get(i);
            b.vel.y += 100;
          }
        }
        break;
        
      case 9: //horizontal rain
        prVel = -10;
        if(lookLeft) prVel = -prVel; //determine firing direction
        projYPos = -60;
        if(shotFrame > 30 && actionFrame <= 300 && actionFrame%5 == 0){ //add projectiles to arraylist
          Projectile pr;
          if(lookLeft) pr = new Projectile(new PVector(0, random(50, height-200)), new PVector(20, 20), new PVector(prVel, 0), 1, 1, color(160));
          else pr = new Projectile(new PVector(width, random(50, height-200)), new PVector(20, 20), new PVector(prVel, 0), 1, 1, color(160));
          projs.add(pr);
        }
        if(actionFrame%8 == 0) bossFire.play(0);
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
  
  void drawMe(){ //draws queen based on state 
    switch(state){
      case IDLE: //when idle
        imgHeader = "queen/idle/qn_idle_";
        spriteFrames = 4;
        break;
      case WALK: //when moving
        imgHeader = "queen/walk/qn_walk_";
        spriteFrames = 4;
        break;
      case ATK: //when attacking
        imgHeader = "queen/warn/qn_warn_";
        spriteFrames = 4;
        break;
      case BREAK: //when in break state
        imgHeader = "queen/break/qn_break_";
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
