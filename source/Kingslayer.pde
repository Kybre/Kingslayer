import ddf.minim.*;

//default variables
Player p; 
Platform plat;
Stage s;
Boss b;

PImage pImg, bImg, platImg, shopImg; //images used in menus
boolean firstLoad = true; //checks for first time loading the game
boolean textBlink = true; //flashing text effect on lose screen
Entity gameButton, cursor, selButton, homeButton, shopButton, bossButton1, bossButton2, bossButton3, buyButton, tutButton; //menu buttons
ArrayList<Entity> shopButtons = new ArrayList<Entity>(); //arraylist containing shop buttons
ArrayList<Entity> diffButtons = new ArrayList<Entity>(); //arraylist containing difficulty setting buttons
final int HOME = 0, SELECT = 1, SHOP = 2, GAME = 3; //menu states
final int NONE = 0, GUARD = 1, QUEEN = 2, KING = 3; //bosses
final int EASY = 1, NORM = 2, HARD = 3; //difficulty settings
int infoPanel = 0, shopPanel = -1, diffState = NORM, gameState = HOME; //trackers: boss info panel, shop item panel, difficulty setting and menu/game state respectively
boolean guardCleared = false, queenCleared = false; //booleans to check if bosses have been cleared

//shop
ArrayList<Boolean> soldOut = new ArrayList<Boolean>(); //arraylist tracking whether shop items are sold out
int fire = 0, water = 0, earth = 0, wind = 0, lightning = 0, light = 0, dark = 0; //shop values
ArrayList<Integer> prices = new ArrayList<Integer>(); //arraylist of shop prices
float money = 100;

//audio
Minim minim;
AudioPlayer bgm, playerFire, playerSword, playerHit, bossFire, bossLaser, bossWarn, bossOrbit, bossBreak, buy, buttonClick;

boolean debug = false; //switch to true to activate debug functions
/*
New features since Assignment 3:

- Milestone 1:
  - Hitboxes
    - Player Hurtbox, changes with position and shifts down if crouching
    - Player Melee hitbox, linked to sword attack
  - Platformer Logic
    - Walls and surfaces (see Platform class)
    - Gravity, Jump Control
  - Different Boss firing patterns
    - Orbitting Projectiles
    - Beams
    - Homing Projectiles
    - Projctiles travelling in waves
  - Boss Warning Triggers
  
- Milestone 2:
  - Shop/Upgrade System
    - Multiple Jumps (default is 1 jump)
  - Objects for different bosses
  - More Boss firing patterns
    - Spiral In/Out orbits
    - Spaced/Moving Beams
    - Projectiles as Source for more Projectiles
  - Boss Animations
  - Stun/Break System
    - Damage multiplier depending on enemy State.
    
- Milestone 3:
  - Implemented Boss 3 (King), New Patterns:
    - Targetted Rain (follows player xpos)
    - Converging and Diverging Lasers
    - Projectiles changing size and velocity mid-attack
  - Tutorial Screen
  - QoL Changes:
    - Boss turns the other way when hit
    - Boss walks away from player after being stunned
    - Gun buttons can be held to fire
    - Reduced Stamina drain
    - Disable ESC to exit program
  - Bug Fixes:
    - Array Exception on Homing Shot (Guard)
    - Fixed Shop taking money even if item is sold out
    
- Final Product
  - Added BGM and Sound Effects
  - Added Level Progression
  - Changed Fire Upgrade to Raw Gun Damage instead of number of projectiles
*/

/*
Sprites Created with the help of Sirius Li:
- https://www.instagram.com/shadowneer/?hl=en

Music by Jonathan Newman
- https://twitter.com/kwiper01

Sound Effects and Sprite Animation by Herman Chan (me)
*/

void setup(){ //sets up program
  size(1600, 900);
  if(debug){
    money = 1000;
    guardCleared = true;
    queenCleared = true;
  }
  
  //initialize soldOut arraylist (tracks whether shop items are sold out)
  for(int i = 0; i < 7; i++){
    soldOut.add(false);
  }
  
  //font creation
  PFont font = createFont("manaspc.ttf", 32);
  textFont(font);
  textSize(20);
  
  loadAudio(); //loads game audio
  loadButtons(); //loads button entities
  
  //loads cursor Entity to test for intersection
  cursor = new Entity(new PVector(mouseX, mouseY), new PVector(10, 10));
  
  //starts instance of game to load assets
  startGame(GUARD, EASY);
}

void loadAudio(){ //loads all the audio used in the game
  //audio handling
  minim = new Minim(this);
  bgm = minim.loadFile("thing_loop.mp3"); //background music
  bgm.setGain(bgm.getGain()-45);
  
  playerFire = minim.loadFile("ShootFX.mp3"); //player shooting sound
  playerFire.setGain(playerFire.getGain()-15);
  
  playerSword = minim.loadFile("SwordFX.mp3"); //player sword sound
  playerSword.setGain(playerSword.getGain()-10);
  
  playerHit = minim.loadFile("PlayerHit.mp3"); //sound that plays when player gets hit
  playerHit.setGain(playerHit.getGain()-20);
  
  bossFire = minim.loadFile("BossShootFX.mp3"); //boss shooting sound
  bossFire.setGain(bossFire.getGain()-15);
  
  bossLaser = minim.loadFile("BossLaserFX.mp3"); //boss shooting sound when firing a laser
  bossLaser.setGain(bossLaser.getGain()-15);
  
  bossOrbit = minim.loadFile("BossOrbitFX.mp3"); //boss shooting sound when firing an orbitting projectile
  bossOrbit.setGain(bossOrbit.getGain()-20);
  
  bossWarn = minim.loadFile("BossWarnFX.mp3"); //boss warning sound
  bossWarn.setGain(bossWarn.getGain()-15);
  
  bossBreak = minim.loadFile("BossBreakFX.mp3"); //boss break sound
  bossBreak.setGain(bossBreak.getGain()-20);
  
  buy = minim.loadFile("BuyFX.mp3"); //shop buy item sound
  buy.setGain(buy.getGain()-25);
  
  buttonClick = minim.loadFile("ClickFX.mp3"); //button clicking sound
  buttonClick.setGain(buttonClick.getGain()-25);
  
  playBGM();
}

void playBGM(){ //plays background music
   AudioPlayer sound = null;
   sound = bgm; 
   sound.loop();
}

void loadButtons(){ //loads button Entities
  gameButton = new Entity(new PVector(width/2, height/2+120), new PVector(400, 100)); //button that loads game state
  selButton = new Entity(new PVector(width/2, height/2+60), new PVector(400, 100)); //button that loads select screen
  bossButton1 = new Entity(new PVector(width/6+25, 90), new PVector(500, 100)); //button that loads the 1st boss
  bossButton2 = new Entity(new PVector(width/6+25, 210), new PVector(500, 100)); //button that loads the 2nd boss
  bossButton3 = new Entity(new PVector(width/6+25, 330), new PVector(500, 100)); //button that loads the 3rd boss
  shopButton = new Entity(new PVector(width/6+25, height-70), new PVector(400, 100)); //button that loads the shop menu
  buyButton = new Entity(new PVector(2*width/3+40, height-90), new PVector(400, 100)); //button that loads the buy button under the shop menu
  for(int i = 0; i < 7; i++){ //buttons that load shop items (left of shop screen)
    shopButtons.add(new Entity(new PVector(width/6+25, (i*90)+200), new PVector(400, 70)));
    prices.add(0);
  }
  for(int i = 0; i < 3; i++){ //buttons that toggle difficulty settings
    diffButtons.add(new Entity(new PVector(width/6-150+(180*i), height-220), new PVector(160, 100)));
  }
  tutButton = new Entity(new PVector(width/2, 675), new PVector(400, 100)); //button that deactivates tutorial screen
}

void draw(){ //updates main portion of game
  
  //update cursor position
  cursor.pos.x = mouseX;
  cursor.pos.y = mouseY;
  
  switch(gameState){ //game state control
   case HOME:
     drawHome();
     break;
   case SELECT:
     drawSelect();
     break;
   case SHOP:
     drawShop();
     break;
   case GAME:
     drawGame();
     break;
  }
}

void drawGame(){ //draws the main game
  background(220);  
  
  //stage
  s.checkCollision(p);
  s.checkCollision(b);
  s.drawMe();
  
  if(!firstLoad){ //if not first time loading the game OR if tutorial screen is cleared play game
    //boss
    if(b.isAlive){ 
      s.checkRebound(b);
      b.update();
      b.drawMe();
    }
    else if (b.deathTimer > 0) b.drawDeath(); //switch to death animation if boss has been defeated
  
    //player
    if(p.isAlive){
      p.update();
      p.drawMe();
    }
  
    //HUD
    p.drawHUD();
    b.drawHUD();
  
    //draw in game win/lose states when there is only 1 character standing
    if(!p.isAlive) drawGameOver();
    if(!b.isAlive && b.deathTimer <= 0){
      drawWin();
      if(b instanceof Guard) guardCleared = true;
      if(b instanceof Queen) queenCleared = true;
    }
  }
  else{ //draws tutorial screen on first time entering a fight
    drawTut();
  }
}

void drawTut(){ //draws the tutorial screen and related entities
  //container rectangle
  rectMode(CENTER);
  stroke(0);
  fill(160);
  strokeWeight(10);
  rect(width/2, height/2, width-150, height-250);
  
  //Title + blurb
  textSize(60);
  fill(0);
  text("KINGSLAYER TUTORIAL", width/2, 225);
  textSize(20);
  
  //core gameplay explanation
  text("Welcome! In KINGSLAYER, your objective is to dodge the incoming waves of projectiles and defeat the boss!", width/2, 325);
  
  //movement/attack explanation
  text("You can move with the WASD or Arrow keys, with 'W' and 'UP' being jump respectively.", width/2, 400);
  text("There are 2 types of attacks: the Melee attack (Z/LMB) and the Ranged Attack (X/RMB) ", width/2, 440);
  text("You can also Dash with the SHIFT key, but watch out for your Stamina, located in the lower left!", width/2, 480);
  
  //start button
  textSize(24);
  text("When you're ready, click the button below:", width/2, 605);
  if(cursor.intersects(tutButton)){
    tutButton.drawMe(color(75, 75, 0), 255, 5);
  }
  else tutButton.drawMe(color(150, 150, 0), 255, 5);
  textSize(75);
  fill(0);
  text("START", width/2, 700); //text on button
}

void drawHome(){ //draws the landing/home screen for the game and associated entities
  background(0, 160, 225);
  rectMode(CENTER);
  textSize(160);
  textAlign(CENTER);
  fill(0);
  
  text("KINGSLAYER", width/2-5, height/3-2.5);
  fill(220, 200, 0);
  text("KINGSLAYER", width/2+5, height/3+2.5);
  
  //start button: goes to stage select
  selButton.pos.x = width/2;
  selButton.pos.y = height/2+60;
  if(cursor.intersects(selButton)){
    selButton.drawMe(color(75, 75, 0), 255, 5);
    if(mouseButton == LEFT){
      buttonClick.play(0);
      gameState = SELECT;
      infoPanel = NONE;
    }
  }
  else selButton.drawMe(color(150, 150, 0), 255, 5);
  textSize(48);
  fill(0);
  text("STAGE SELECT", selButton.pos.x, selButton.pos.y+15);
  
  
  //shop button
  shopButton.pos.x = width/2;
  shopButton.pos.y = height/2+180;
  if(cursor.intersects(shopButton)){
    shopButton.drawMe(color(75, 75, 0), 255, 5);
    if(mouseButton == LEFT){
      buttonClick.play(0);
      gameState = SHOP;
      shopPanel = -1;
    }
  }
  else shopButton.drawMe(color(150, 150, 0), 255, 5);
  textSize(72);
  fill(0);
  text("SHOP", shopButton.pos.x, shopButton.pos.y+25);
}

void drawSelect(){ //draws the stage selection screen and associated buttons
  background(165, 115, 85);
  strokeWeight(5);
  stroke(80, 25, 0);
  fill(150, 85, 45);
  rectMode(CORNER);
  rect(20, 20, width/3+20, 5*height/6 - 20);
  
  //boss button
  if(cursor.intersects(bossButton1) || infoPanel == GUARD){
    bossButton1.drawMe(color(120), 255, 5);
  }
  else bossButton1.drawMe(color(180), 255, 5);
  textSize(48);
  fill(0);
  text("HEAD GUARD", bossButton1.pos.x, bossButton1.pos.y+15);
  
  //queen button
  if(guardCleared){
    if(cursor.intersects(bossButton2) || infoPanel == QUEEN){
      bossButton2.drawMe(color(120), 255, 5);
    }
    else bossButton2.drawMe(color(180), 255, 5);
    textSize(48);
    fill(0);
    text("QUEEN", bossButton2.pos.x, bossButton2.pos.y+15);
  }
  
  //king button
  if(guardCleared && queenCleared){
    if(cursor.intersects(bossButton3) || infoPanel == KING){
      bossButton3.drawMe(color(120), 255, 5);
    }
    else bossButton3.drawMe(color(180), 255, 5);
    textSize(48);
    fill(0);
    text("KING", bossButton3.pos.x, bossButton3.pos.y+15);
  }
  
  //shop button
  shopButton.pos.x = width/6+25;
  shopButton.pos.y = height-70;
  if(cursor.intersects(shopButton)){
    shopButton.drawMe(color(75, 75, 0), 255, 5);
    if(mouseButton == LEFT){
      buttonClick.play(0);
      gameState = SHOP;
      shopPanel = -1;
    }
  }
  else shopButton.drawMe(color(150, 150, 0), 255, 5);
  textSize(72);
  fill(0);
  text("SHOP", shopButton.pos.x, shopButton.pos.y+25);
  
  //info panel (panel on a boss that appears when a tab on the left is clicked)
  if(infoPanel != NONE){
    strokeWeight(5);
    stroke(80, 25, 0);
    fill(150, 85, 45);
    rectMode(CORNER);
    rect(width/3+60, 20, 2*width/3-80, height-40);
    fill(180);
    stroke(0);
    rect(width/3+80, 40, 2*width/3-120, 600);
    switch(infoPanel){
      case GUARD:
        bImg = loadImage("guard/idle/hg_idle_0.png");
        bImg.resize(bImg.width*2, bImg.height*2);
        b = new Boss(new PVector(2*width/3+40, 280), new PVector(bImg.width, bImg.height), bImg, "HEAD GUARD", p.stunDmgMult, EASY);
        platImg = loadImage("stage/stage_0_0.png");
        plat = new Platform(new PVector(2*width/3+40, 560), new PVector(platImg.width, platImg.height), platImg, false, false);
        break;
      case QUEEN:
        bImg = loadImage("queen/idle/qn_idle_0.png");
        bImg.resize(bImg.width*2, bImg.height*2);
        b = new Boss(new PVector(2*width/3+40, 280), new PVector(bImg.width, bImg.height), bImg, "QUEEN", p.stunDmgMult, EASY);
        platImg = loadImage("stage/stage_1_0.png");
        plat = new Platform(new PVector(2*width/3+40, 560), new PVector(platImg.width, platImg.height), platImg, false, false);
        break;
      case KING:
        bImg = loadImage("king/idle/king_idle_0.png");
        bImg.resize(bImg.width*2, bImg.height*2);
        b = new Boss(new PVector(2*width/3+40, 280), new PVector(bImg.width, bImg.height), bImg, "KING", p.stunDmgMult, EASY);
        platImg = loadImage("stage/stage_2_0.png");
        plat = new Platform(new PVector(2*width/3+40, 560), new PVector(platImg.width, platImg.height), platImg, false, false);
        break;
      default:
        break;
    }
    b.drawMe();
    plat.drawMe();
    textAlign(CENTER);
    fill(0);
    text(b.name, 2*width/3+40, 720);
        
    //game start button
    gameButton.pos.x = 2*width/3+40; 
    gameButton.pos.y = height-90;
    if(cursor.intersects(gameButton)){
      gameButton.drawMe(color(75, 75, 0), 255, 5);
      if(mouseButton == LEFT){
        buttonClick.play(0);
        startGame(infoPanel, diffState);
        gameState = GAME;
      }
    }
    else gameButton.drawMe(color(150, 150, 0), 255, 5);
    textSize(70);
    fill(0);
    text("START", gameButton.pos.x, gameButton.pos.y+25);
  }
  
  //difficulty state buttons
  for(int i = 0; i < diffButtons.size(); i++){
    Entity db = diffButtons.get(i);
    color dCol = color(0);
    String dText = "";
    switch(i){
      case 0:
        if(cursor.intersects(db) || diffState == EASY){
          dCol = color(0, 80, 0);
          if(mouseButton == LEFT){
            buttonClick.play(0);
            diffState = EASY;
          }
        }
        else dCol = color(0, 160, 0);
        dText = "EASY";
        break;
      case 1:
        if(cursor.intersects(db) || diffState == NORM){
          dCol = color(75, 75, 0);
          if(mouseButton == LEFT){
            buttonClick.play(0);
            diffState = NORM;
          }
        }
        else dCol = color(150, 150, 0);
        dText = "NORM";
        break;
      case 2:
        if(cursor.intersects(db) || diffState == HARD){
          dCol = color(80, 0, 0);
          if(mouseButton == LEFT){
            buttonClick.play(0);
            diffState = HARD;
          }
        }
        else dCol = color(160, 0, 0);
        dText = "HARD";
        break;
      default:
        ;
    }
    db.drawMe(dCol, 255, 5);
    textSize(60);
    fill(0);
    text(dText, db.pos.x, db.pos.y+25);
  }
}

void drawShop(){ //draws the shop menu and associated entities
  background(120);
  strokeWeight(5);
  stroke(0);
  fill(80);
  rectMode(CORNER);
  rect(20, 140, width/3+20, 5*height/6 - 20);
  
  //stage select button
  selButton.pos.x = width/6+25;
  selButton.pos.y = 70;  
  if(cursor.intersects(selButton)){
    selButton.drawMe(color(75, 75, 0), 255, 5);
    if(mouseButton == LEFT){
      buttonClick.play(0);
      gameState = SELECT;
      infoPanel = NONE;
    }
  }
  else selButton.drawMe(color(150, 150, 0), 255, 5);
  textSize(48);
  fill(0);
  text("STAGE SELECT", selButton.pos.x, selButton.pos.y+15);
  
  //money display
  text("MONEY:$" + money, width/6+25, height-60);
  
  //shop item buttons
  for(int i = 0; i < shopButtons.size(); i++){
    Entity sb = shopButtons.get(i);
    if(cursor.intersects(sb) || shopPanel == i){
      sb.drawMe(color(80, 25, 0), 255, 5);
      if(mouseButton == LEFT){
        buttonClick.play(0);
        shopPanel = i;
      }
    }
    else sb.drawMe(color(150, 85, 45), 255, 5);
    
    String s = "";
    switch(i){
      case 0:
        s = "FIRE";
        break;
      case 1:
        s = "WATER";
        break;
      case 2:
        s = "EARTH";
        break;
      case 3:
        s = "WIND";
        break;
      case 4:
        s = "LIGHTNING";
        break;
      case 5:
        s = "LIGHT";
        break;
      case 6:
        s = "DARK";
        break;
      default:
        ;
    }
    textSize(48);
    fill(0);
    text(s, sb.pos.x, sb.pos.y+15);
  }
  
  //shop info panels
  if(shopPanel != -1){
    shopImg = loadImage("shop/shop_" + (shopPanel+1) + ".png");
    String item = "", effect = "";
    int buyCount = -1;
    switch(shopPanel){
      case 0:
        item = "FIRE CRYSTAL";
        effect = "Increase gun damage";
        buyCount = fire;
        break;
      case 1:
        item = "WATER CRYSTAL";
        effect = "Increase max HP";
        buyCount = water;
        break;
      case 2:
        item = "EARTH CRYSTAL";
        effect = "Increase Stun amount";
        buyCount = earth;
        break;
      case 3:
        item = "WIND CRYSTAL";
        effect = "Increase max Stamina and max jumps";
        buyCount = wind;
        break;
      case 4:
        item = "LIGHTNING CRYSTAL";
        effect = "Increase movement speed";
        buyCount = lightning;
        break;
      case 5:
        item = "LIGHT CRYSTAL";
        effect = "Increase sword damage";
        buyCount = light;
        break;
      case 6:
        item = "DARK CRYSTAL";
        effect = "Increase Stun Break damage multiplier";
        buyCount = dark;
        break;
      default:
        ;
    }
    int price = prices.get(shopPanel);
    price = 50 + 25*buyCount;
    prices.set(shopPanel, price);
    imageMode(CORNER);
    image(shopImg, 2*width/3-40, 40);
    textAlign(CENTER);
    text(item, 2*width/3+50, 600);
    textSize(32);
    text(effect, 2*width/3+40, 650);
    if(price <= 100) text("$" + price, 2*width/3+30, 700);
    else text("SOLD OUT", 2*width/3+30, 700);
    if(cursor.intersects(buyButton)){
      buyButton.drawMe(color(75, 75, 0), 255, 5);
    }
    else buyButton.drawMe(color(150, 150, 0), 255, 5);
    textSize(70);
    fill(0);
    text("BUY: " + buyCount + "/3", buyButton.pos.x, buyButton.pos.y+25);
  }
}

void buyItem(int item){ //method to buy items
  buy.play(0);
  switch(item){
    case 0:
      if(fire < 3) fire++;
      if(fire >= 3) soldOut.set(0, true);
      break;
    case 1:
      if(water < 3) water++;
      if(water >= 3) soldOut.set(1, true);
      break;
    case 2:
      if(earth < 3) earth++;
      if(earth >= 3) soldOut.set(2, true);
      break;
    case 3:
      if(wind < 3) wind++;
      if(wind >= 3) soldOut.set(3, true);
      break;
    case 4:
      if(lightning < 3) lightning++;
      if(lightning >= 3) soldOut.set(4, true);
      break;
    case 5:
      if(light < 3) light++;
      if(light >= 3) soldOut.set(5, true);
      break;
    case 6:
      if(dark < 3) dark++;
      if(dark >= 3) soldOut.set(6, true);
      break;
    default:
      ;
  }
}

void drawGameOver(){ //draws in-game game over screen
  //half-transparant black layer
  rectMode(CENTER);
  fill(0, 200);
  rect(width/2, height/2, width, height);
  
  //flashing "YOU LOSE" text
  textAlign(CENTER);
  fill(160, 0, 0);
  if(frameCount%30 == 0 && !textBlink) textBlink = true; //boolean toggle every half second
  else if(frameCount%30 == 0 && textBlink) textBlink = false;
  if(textBlink){ //every half a second, display text (creates retro blink effect)
    textSize(120);
    text("YOU LOSE", width/2, height/2+20);
  } 
  
  //retry button
  gameButton.pos.x = width/2;
  gameButton.pos.y = height/2+120;
  if(cursor.intersects(gameButton)){
    gameButton.drawMe(color(75, 75, 0), 255, 5);
    if(mouseButton == LEFT){
      buttonClick.play(0);
      startGame(infoPanel, diffState);
      gameState = GAME;
    }
  }
  else gameButton.drawMe(color(150, 150, 0), 255, 5);
  textSize(70);
  fill(0);
  text("RETRY?", gameButton.pos.x, gameButton.pos.y+25);
  
  //stage select button
  selButton.pos.x = width/2;
  selButton.pos.y = height/2+240;
  if(cursor.intersects(selButton)){
    selButton.drawMe(color(75, 75, 0), 255, 5);
    if(mouseButton == LEFT){
      buttonClick.play(0);
      gameState = SELECT;
      infoPanel = NONE;
    }
  }
  else selButton.drawMe(color(150, 150, 0), 255, 5);
  textSize(48);
  fill(0);
  text("STAGE SELECT", selButton.pos.x, selButton.pos.y+15);
}

void drawWin(){//draws game win screen
  //half-transparant white layer
  rectMode(CENTER);
  fill(255, 200);
  rect(width/2, height/2, width, height);
  
  //flashing "YOU LOSE" text
  textAlign(CENTER);
  fill(0, 80, 160);
  if(frameCount%30 == 0 && !textBlink) textBlink = true; //boolean toggle every half second
  else if(frameCount%30 == 0 && textBlink) textBlink = false;
  if(textBlink){ //every half a second, display text (creates retro blink effect)
    textSize(120);
    text("YOU WIN", width/2, height/2+20);
  } 
  //money earned
  textSize(48);
  fill(0);
  text("MONEY EARNED:$" + b.moneyEarned, width/2, height/2+80);
  //stage select button
  selButton.pos.x = width/2;
  selButton.pos.y = height/2+160;
  if(cursor.intersects(selButton)){
    selButton.drawMe(color(75, 75, 0), 255, 5);
    if(mouseButton == LEFT){
      buttonClick.play(0);
      gameState = SELECT;
      infoPanel = NONE;
    }
  }
  else selButton.drawMe(color(150, 150, 0), 255, 5);
  textSize(45);
  fill(0);
  text("STAGE SELECT", selButton.pos.x, selButton.pos.y+20);
}

void startGame(int boss, int diff){
  //init objects
  pImg = loadImage("player/idle/idle_0.png");
  p = new Player(new PVector(width/4-pImg.width/2, height/2), new PVector(pImg.width, pImg.height), pImg);
  p.setUpgrades(fire, water, earth, wind, lightning, light, dark); //sets player upgrades
  
  if(boss == GUARD){
    s = new Stage(0, color(135, 206, 235, 80));
    bImg = loadImage("guard/idle/hg_idle_0.png");
    b = new Guard(new PVector(3*width/4-bImg.width/2, height/2), new PVector(bImg.width, bImg.height), bImg, "Head Guard", p.stunDmgMult, diff);
  }
  
  if(boss == QUEEN){
    s = new Stage(1, color(180, 80));
    bImg = loadImage("queen/idle/qn_idle_0.png");
    b = new Queen(new PVector(3*width/4-bImg.width/2, height/2), new PVector(bImg.width, bImg.height), bImg, "Queen", p.stunDmgMult, diff);
  }
  
  if(boss == KING){
    s = new Stage(2, color(200, 200, 180, 80));
    bImg = loadImage("king/idle/king_idle_0.png");
    b = new King(new PVector(3*width/4-bImg.width/2, height/2), new PVector(bImg.width, bImg.height), bImg, "King", p.stunDmgMult, diff);
  }
  
}
