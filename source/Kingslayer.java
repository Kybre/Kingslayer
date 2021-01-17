import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class D103_Herman_Chan_Assignment4_301398139 extends PApplet {



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

public void setup(){ //sets up program
  
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

public void loadAudio(){ //loads all the audio used in the game
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

public void playBGM(){ //plays background music
   AudioPlayer sound = null;
   sound = bgm; 
   sound.loop();
}

public void loadButtons(){ //loads button Entities
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

public void draw(){ //updates main portion of game
  
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

public void drawGame(){ //draws the main game
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

public void drawTut(){ //draws the tutorial screen and related entities
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

public void drawHome(){ //draws the landing/home screen for the game and associated entities
  background(0, 160, 225);
  rectMode(CENTER);
  textSize(160);
  textAlign(CENTER);
  fill(0);
  
  text("KINGSLAYER", width/2-5, height/3-2.5f);
  fill(220, 200, 0);
  text("KINGSLAYER", width/2+5, height/3+2.5f);
  
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

public void drawSelect(){ //draws the stage selection screen and associated buttons
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
    int dCol = color(0);
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

public void drawShop(){ //draws the shop menu and associated entities
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

public void buyItem(int item){ //method to buy items
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

public void drawGameOver(){ //draws in-game game over screen
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

public void drawWin(){//draws game win screen
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

public void startGame(int boss, int diff){
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
  
  public void update(){ //updates boss object
    super.update();
    
    //update boss phase
    if(health/maxHealth <= 0.25f) phase = 3;
    else if(health/maxHealth <= 0.5f) phase = 2;
    else if(health/maxHealth <= 0.75f) phase = 1;
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
        if(jumpbox.intersects(plat) && onGround && plat.floor && rand >= 98.5f && !shot && !stunBreak && actionFrame < 205) jump = true; //random chance for boss to jump if there is a floored platform above it, it is currently on the ground, and isn't shooting or stunned
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
  
  public void adjustHitboxes(){ //adjust boss hitboxes to stay on the boss
    footbox.pos.x = pos.x;
    footbox.pos.y = pos.y+7*dim.y/16;
    hurtbox.pos = pos;
    jumpbox.pos = new PVector(pos.x, pos.y-dim.y/2+50);
  }
  
  public void setFirePattern(){ //sets boss firing pattern
    firePattern = (int)random(0, 10);
  }
  
  public void drawMe(){ //draws boss
    pushMatrix();
    translate(pos.x, pos.y);
    //draw the image
    if(lookLeft) scale(-1, 1);
    imageMode(CENTER);
    image(img, 0, 0);
    popMatrix();
  }
  
  public void damage(float d){ //method to damage boss
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
  
  public void turnAway(Player player){ //turn boss away from player
    if(player.pos.x > this.pos.x && spd > 0 || player.pos.x < this.pos.x && spd < 0) spd = -spd;
  }
  
  public void drawDeath(){ //draw shrinking death animation
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
  
  public void fire(int pattern){ //method to fire pattern
  }
  
  public void warn(PImage wImg, float frames){ //method to toggle boss between 2 frames to create warning animation, wImg = other image used aside from base, frames = time allotted to warning player
   if(shotFrame%10 == 0 && warnToggle == false) warnToggle = true;
   else if(shotFrame%10 == 0 && warnToggle == true) warnToggle = false;
   if(!warnToggle || shotFrame >= frames-1){
     img = warnImages.get(3);
   }
   else {
     img = wImg;
   }
 }
 public void checkProjectiles(){ //checks boss's projectiles
    //bullet checking
    for(int i = 0; i<projs.size(); i++){
      Projectile pr = projs.get(i);
      pr.update();
      pr.drawMe(1.5f);
      if(pr.intersects(p.hurtbox) && !pr.hitOnce){ //if a projectile intersects the player's hurtbox and hasn't before
        p.damage(pr.dmg);
        pr.hitOnce = true;
      }
      if(pr.timer%60 == 0){ //if projectile has been intersecting with player hitbox for a second, toggle so that player can be damaged again
        pr.hitOnce = false;
      }
      if(pr.pos.x-pr.dim.x > width*1.5f || pr.pos.x+pr.dim.x < -0.5f*width || pr.pos.y-pr.dim.y > height*1.5f || pr.pos.y+pr.dim.y < -0.5f*height) projs.remove(pr); //if projectile leaves the screen*1.5
    }
  }
  
  public void drawHUD(){ //draws HUD for boss elements
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
class Character extends MovingObject{ //character superclass
  float maxHealth, health = maxHealth, damp = 0.5f, deathTimer, spd = 2; //health variables, movement dampener, death timer and movement speed respectively
  PImage img; //character image
  ArrayList<Projectile> projs = new ArrayList<Projectile>(); //projectiles stored under character
  Entity footbox; //hitbox for floor/wall collision detection
  boolean onGround, isAlive = true, lookLeft = false; //boolean checking states of: is player on solid ground, is player alive, and is player looking left respectively
  
  Character(PVector pos, PVector dim, PVector vel, PImage img){ //character constructor
    super(pos, dim, vel);
    this.img = img;
    footbox = new Entity(new PVector(pos.x, pos.y+dim.y/4), new PVector(dim.x/3, dim.y/8));
  }
  
  public void update(){ //updates character
    super.update();
    if(!onGround) vel.add(0, 15); //gravity
    vel.mult(damp); //movement dampening
  }
  
  public void fire(){ //fire method
  }
  
  public void damage(float d){ //damage method
    health -= d;
    if(health <= 0) isAlive = false;
  }
}
class Entity{ //entity superclass
  PVector pos, dim;
  
  Entity(PVector pos, PVector dim){ //entity constructor
    this.pos = pos;
    this.dim = dim;
  }
  
  public void update(){ //updates entity
  }
  
  public boolean intersects(Entity e){ //check for intersection between 2 entities
    if(abs(pos.x - e.pos.x) < (dim.x/2 + e.dim.x/2) && abs(pos.y-e.pos.y)< (dim.y/2 + e.dim.y/2)) return true; //if character hitboxes intersect return true;
    else return false;
  }
  
  public void drawMe(int c, float op, float st){ //draws entity. parameters: c = entity color, op = opacity, st = stroke weight
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
  
  public void update(){ //updates boss
    super.update();
  }
  
  public void adjustHitboxes(){ //adjust boss hitboxes to stay on the boss
    footbox.pos.x = pos.x;
    footbox.pos.y = pos.y+7*dim.y/16;
    if(lookLeft) hurtbox.pos.x = pos.x+10;
    else hurtbox.pos.x = pos.x-10;
    hurtbox.pos.y = pos.y;
    jumpbox.pos = new PVector(pos.x, pos.y-dim.y/2+25);
  }
  
  public void setFirePattern(){ //set firing pattern
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
  
  public void warn(PImage wImg, float frames){ //method to toggle boss between 2 frames to create warning animation, wImg = other image used aside from base, frames = time allotted to warning player
   state = WARN;
   if(shotFrame%10 == 0 && warnToggle == false) warnToggle = true;
   else if(shotFrame%10 == 0 && warnToggle == true) warnToggle = false;
   if(!warnToggle || shotFrame >= frames-1) img = warnImages.get(0);
   else img = wImg;
  }
  
  public void fire(int pattern){ //method for boss to fire
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
        prVel = 0.08f;
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
        prVel = 0.25f;
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
        prVel = 0.25f;
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
  
  public void drawMe(){ //draws guard based on state
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
boolean u, d, l, r, mouseDown; //movement checking booleans
boolean zDown = false; //checks if Z button is being held
boolean xDown = false; //checks if X button is being held

public void keyPressed(){
  if(keyCode==LEFT||key=='a'||key=='A') l=true; //move left if player is pressing 'a' key or left arrow
  if(keyCode==RIGHT||key=='d'||key=='D') r=true; //move right if player is pressing 'd' key or right arrow
  if(keyCode==UP||key=='w'||key=='W') u=true; //move up if player is pressing 'w' key or up arrow
  if(keyCode==DOWN||key=='s'||key=='S') {
    d=true; //move down if player is pressing 's' key or down arrow
  }
  if(keyCode == SHIFT && p.stamina > 0 && p.dashHeld == false) p.dash = true; //if player is holding shift, allow them to dash
  if(key=='z'||key=='Z'){ //if player is pressing Z, initiate player sword attack
    if(!zDown && !p.sword){
      p.sword = true; 
      p.animFrame = 0;
      zDown = true;
    }
  }
  if(key=='x'||key=='X'){ //if player is pressing X, initiate player shot
    if(!xDown && !p.shot){
      p.shot = true; 
      p.gunFrame = 0;
      xDown = true;
    }
  }
  if (key == ESC){ //diables ESC to exit game
    key = 0; 
  }
}

public void keyReleased(){
  if(keyCode==LEFT||key=='a'||key=='A') l=false;
  if(keyCode==RIGHT||key=='d'||key=='D') r=false;
  if(keyCode==UP||key=='w'||key=='W') {
    u=false;
    if(p.jumps > 0) p.jumps--; //reduce number of jumps player has remaining
  }
  if(keyCode==DOWN||key=='s'||key=='S') d=false;
  if(keyCode == SHIFT){
    p.dash = false;
    p.dashHeld = false;
  }
  if(key=='z'||key=='Z') zDown = false;
  if(key=='x'||key=='X') {
    xDown = false;
    p.shot = false;
  }
}

public void mouseClicked(){
  if(mouseButton == LEFT && !p.sword) {
    p.sword = true; 
    p.animFrame = 0;
  }//slash if player clicks left mouse button
  
  //boss button interactions - when clicked, shows the relevant information panel
  if(cursor.intersects(bossButton1) && mouseButton == LEFT && gameState == SELECT){
    if(!mouseDown) buttonClick.play(0);
    infoPanel = GUARD;
  }
  if(cursor.intersects(bossButton2) && mouseButton == LEFT && gameState == SELECT){
    if(!mouseDown) buttonClick.play(0);
    infoPanel = QUEEN;
  }
  if(cursor.intersects(bossButton3) && mouseButton == LEFT && gameState == SELECT){
    if(!mouseDown) buttonClick.play(0);
    infoPanel = KING;
  }
  
  //tutorial button interaction -  exits tutorial screen when clicked
  if(cursor.intersects(tutButton) && mouseButton == LEFT && gameState == GAME){
    if(!mouseDown) buttonClick.play(0);
    firstLoad = false;
  }
  
  //buy button interaction - checks if player has the money to buy and shop still has item in stock, then purchases item
  if(cursor.intersects(buyButton) &&  mouseButton == LEFT && gameState == SHOP && money >= prices.get(shopPanel) && !soldOut.get(shopPanel)){
    if(!mouseDown) buttonClick.play(0);
    buyItem(shopPanel);
    money -= prices.get(shopPanel);
  }
}

public void mousePressed(){
  mouseDown = true;
  if(mouseButton == RIGHT && !p.shot){ //if player is holding RMB, shoot
    p.shot = true; 
    p.animFrame = 0;
  }
}

public void mouseReleased(){
  mouseDown = false;
  if(mouseButton == RIGHT) p.shot = false; //stop player from shooting
}
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
  
  public void update(){
    super.update();
  }
  
  public void adjustHitboxes(){ //adjust boss hitboxes to stay on the boss
    footbox.pos.x = pos.x;
    footbox.pos.y = pos.y+7*dim.y/16;
    if(lookLeft) hurtbox.pos.x = pos.x+10;
    else hurtbox.pos.x = pos.x-10;
    hurtbox.pos.y = pos.y;
    jumpbox.pos = new PVector(pos.x, pos.y-dim.y/2+25);
  }
  
  public void setFirePattern(){ //set firing pattern
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
  
  public void warn(PImage wImg, float frames){ //method to toggle boss between 2 frames to create warning animation, wImg = other image used aside from base, frames = time allotted to warning player
    state = WARN;
   if(shotFrame%10 == 0 && warnToggle == false) warnToggle = true;
   else if(shotFrame%10 == 0 && warnToggle == true) warnToggle = false;
   if(!warnToggle || shotFrame >= frames-1) img = warnImages.get(0);
   else img = wImg;
  }
  
  public void fire(int pattern){ //method for boss to fire
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
          projs.add(new Projectile(new PVector(source.pos.x, source.pos.y), new PVector(20, 20), new PVector(prVel/1.5f, prVel/1.5f), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(source.pos.x, source.pos.y), new PVector(20, 20), new PVector(prVel/1.5f, -prVel/1.5f), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(source.pos.x, source.pos.y), new PVector(20, 20), new PVector(-prVel, 0), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(source.pos.x, source.pos.y), new PVector(20, 20), new PVector(-prVel/1.5f, prVel/1.5f), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(source.pos.x, source.pos.y), new PVector(20, 20), new PVector(-prVel/1.5f, -prVel/1.5f), 1, 1, color(0,80,160)));
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
          source.dim.mult(1.05f);
          source.vel.mult(1.05f);
          bossFire.play(0);
        }
        if(actionFrame >= 300) shot = false; //reset firing state
        break;
     
     case 5: //converging vertical lasers
        prVel = 0.25f;
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
          beam1.vel.x += 0.5f;
          beam2.vel.x -= 0.5f;
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
        prVel = 0.25f;
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
          beam1.vel.x += 0.5f;
          beam2.vel.x -= 0.5f;
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
          source.vel.mult(1.1f);
          playerSword.play(0);
        }
        if(actionFrame >= 300) shot = false; //reset firing state
        break;
        
      case 8: //floor burst
        prVel = 0.25f;
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
        prVel = 0.25f;
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
  
  public void drawMe(){ //draws king based on state 
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
class MovingObject extends Entity{ //superclass for all moving objects
  PVector vel;
  
  MovingObject(PVector pos, PVector dim, PVector vel){
    super(pos, dim);
    this.vel = vel;
  }
  
  public void update(){ //updates moving object
    move();
    handleWalls();
  }
  
  public void move(){ //movement based on vel PVector
    pos.add(vel);
  }
  
  public void accel(PVector force){ //adds force parameter to vel
    vel.add(force);
  }
  
  public void handleWalls(){ //method to handle wall collision
  }
  
  public void drawMe(){ //draws moving object
  }
}
class Platform extends Entity{ //platform class
  PImage img;
  boolean hasChara = false, wall = false, floor = false; //whether platform currently contains a character, whether is has wall collision, and whether it has floor collision respectively
  
  Platform(PVector pos, PVector dim, PImage img, boolean wall, boolean floor){ //platform constructor, boolean values determine whether platform has wall/floor collision
    super(pos, dim);
    this.img = img;
    this.wall = wall;
    this.floor = floor;
  }
  
  public void checkCollision(Character c){ //checks platform collision with a character
    //floor collision
    if(floor && intersects(c.footbox) && c.footbox.pos.y+c.footbox.dim.y/2 - (pos.y-dim.y/2) <= c.dim.y/8){ //checks whether character footbox intersects with platform
      hasChara = true;
      c.pos.y = pos.y - dim.y/2 - c.dim.y/2 + 1; //place character above platform
    }
    else hasChara = false;
    
    //wall collision
    if(wall && intersects(c.footbox) && c.footbox.pos.x > pos.x && c.footbox.pos.y - (pos.y - dim.y/2) > c.footbox.dim.y/2){ //checks whether character footbox intersects with right side of a wall
      c.pos.x = pos.x + dim.x/2 + c.footbox.dim.x/2; //places character directly right of the wall
    }
    else if(wall && intersects(c.footbox) && c.footbox.pos.x < pos.x && c.footbox.pos.y - (pos.y - dim.y/2) > c.footbox.dim.y/2){ //checks whether character footbox intersects with left side of a wall
      c.pos.x = pos.x - dim.x/2 - c.footbox.dim.x/2; //places character directly left of the wall
    }
  }
  
  public void checkRebound(Character c){ //method to check for boss rebounding from wall
    if(wall && intersects(c.footbox) && c.footbox.pos.x > pos.x && c.footbox.pos.y - (pos.y - dim.y/2) > c.footbox.dim.y/2){ //checks whether character intersects with right side of wall
      c.pos.x = pos.x + dim.x/2 + c.footbox.dim.x/2 - c.spd; //places character directly right of wall
      c.spd = -c.spd; //reverse movement direction
    }
    if(wall && intersects(c.footbox) && c.footbox.pos.x < pos.x && c.footbox.pos.y - (pos.y - dim.y/2) > c.footbox.dim.y/2){ //checks whether character footbox intersects with left side of a wall
      c.pos.x = pos.x - dim.x/2 - c.footbox.dim.x/2 - c.spd; //places character directly left of the wall
      c.spd = -c.spd; //reverse movement direction
    }
  }
  
  public void drawMe(){ //draws platform
    pushMatrix();
    translate(pos.x, pos.y);
    //draw the image
    imageMode(CENTER);
    image(img, 0, 0);
    popMatrix();
  }
}
class Player extends Character{ //play class
  float damp = 0.5f; //movement dampener
  
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
  
  public void setUpgrades(int fire, float water, float earth, int wind, float lightning, float light, float dark){ //sets variables to match global upgrade variables
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
    spdBase = 2 + 0.5f*lightning; //increases base speed
    spd = spdBase;
    
    //light upgrade
    swordDmg = 5 + 5*light; //increases sword damage
    
    //dark upgrade
    stunDmgMult = 2 + dark; //increases stun damage multiplier
  }
  
  public void update(){ //updates player obejct
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
    if(abs(vel.x) <= 0.125f && vel.y <= 0.125f && !d) state = IDLE;
    
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
      stamina -= 2.5f; //reduce stamina by 5 every frame when in dash state
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
  
  public void checkProjectiles(){ //checks all projectile associated with the player
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
  
  public void drawMe(){ //draws player based on state
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
  
  public void fire(){ //method for player to fire
    float prVel = 50;
    float projYPos = pos.y-30;
    if(lookLeft) prVel = -prVel;
    if(state == CROUCH) projYPos += 30; //change projectile position if player is crouching
    projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(60, 5), new PVector(prVel, 0), 1, 0, color(160,80,0))); //fires projectile
    playerFire.play(0);
  }
  
  public void damage(float d){ //method to damage player
    if(iTimer < 0){ //if player is not invincible
      health -= d;
      playerHit.play(0);
      iTimer = iFrames;
    }
    if(health <= 0) isAlive = false; //kill player if health drops below 0
  }
  
  public void drawHUD(){ //draws HUD relevant to player info
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
class Projectile extends MovingObject{ //projectile class

  boolean isAlive, hitOnce, orbit = false, gravity = false; //if projectile exists, if projectile has hit once, whether projectile orbits character and whether projectile is affected by gravity respectively
  float timer = -1, dmg, radius, angle = 0; //timer tracking how long projectile has been on screen, projectile damage, orbit radius and orbit angle respectively
  
  //projectile shape
  int shape;
  final int RECTANGLE = 0;
  final int CIRCLE = 1;
  int c; //projectile color
  Character orbitTarget; //orbit target for projectile
  
  Projectile(PVector pos, PVector dim, PVector vel, float damage, int shape, int c){ //projectile constructor
    super(pos, dim, vel);
    dmg = damage;
    this.shape = shape;
    this.c = c;
  }
  
  public void update(){ //updates projectile object
    super.update();
    timer++;
  }
  
  public void move(){ //moves projectile
    if(!orbit){ //if projectile is not orbitting character
      super.move();
      if(gravity)vel.add(0, 0.6f); //adds gravity if projectile is affected
    }
    else orbit(orbitTarget, radius, vel.x); //orbits character if projectile is set to orbit
  }
  
  public void orbit(Character c, float r, float v){ //method for projectile to orbit character
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
  
  public boolean proximity(Entity other, float range){ //checks if projectile is in distance 'range' of an entity 'other'
    return abs(pos.x-other.pos.x) < dim.x/2+other.dim.x/2 + range && abs(other.pos.y-pos.y) < dim.y/2+other.dim.y/2 + range;
  }
  
  public void beamX(float bVel){ //method to extend horizontal beam
    if(bVel > 0){ //if projectile is to be shot to the right, extend rightwards
      dim.x += bVel*dim.x;
    }
    else{ //if projectile is to be shot to the left, extend leftwards
      dim.x -= bVel*dim.x;
    }
    pos.x += (bVel*dim.x)/(2*abs(bVel)+2); //adjust beam position so that it only extends in 1 direction
  }
  
  public void beamY(float bVel){ //method to extend vertical beam
    if(bVel > 0){ //if projectile is to be shot down, extend down
      dim.y += bVel*dim.y;
    }
    else{ //if projectile is to be shot up, extend up
      dim.y -= bVel*dim.y;
    }
    pos.y += (bVel*dim.y)/(2*abs(bVel)+2); //adjust beam position so that it only extends in 1 direction
  }
  
  public void drawMe(float sw){ //draws projectile, sw = stroke weight
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
  
  public void update(){ //updates queen object
    super.update();
  }
  
  public void adjustHitboxes(){ //adjust boss hitboxes to stay on the boss
    footbox.pos.x = pos.x;
    footbox.pos.y = pos.y+7*dim.y/16;
    if(lookLeft) hurtbox.pos.x = pos.x+10;
    else hurtbox.pos.x = pos.x-10;
    hurtbox.pos.y = pos.y;
    jumpbox.pos = new PVector(pos.x, pos.y-dim.y/2+25);
  }
  
  public void setFirePattern(){ //set firing pattern
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
  
  public void warn(PImage wImg, float frames){ //method to toggle boss between 2 frames to create warning animation, wImg = other image used aside from base, frames = time allotted to warning player
    state = WARN;
   if(shotFrame%10 == 0 && warnToggle == false) warnToggle = true;
   else if(shotFrame%10 == 0 && warnToggle == true) warnToggle = false;
   if(!warnToggle || shotFrame >= frames-1) img = warnImages.get(0);
   else img = wImg;
  }
  
  public void fire(int pattern){ //method for boss to fire 
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
        prVel = 0.75f;
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
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(25, 25), new PVector(prVel/1.5f, prVel/1.5f), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(25, 25), new PVector(prVel/1.5f, -prVel/1.5f), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(25, 25), new PVector(-prVel, 0), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(25, 25), new PVector(-prVel/1.5f, prVel/1.5f), 1, 1, color(0,80,160)));
          projs.add(new Projectile(new PVector(pos.x, projYPos), new PVector(25, 25), new PVector(-prVel/1.5f, -prVel/1.5f), 1, 1, color(0,80,160)));
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
        prVel = 0.04f;
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
        prVel = 0.04f;
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
            if(o.vel.y > 0) o.vel.y -= 0.25f; //slow projectile down
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
            else if(o.vel.y < 0) o.vel.y += 0.5f; //slow projectile down
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
        prVel = 0.25f;
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
        prVel = 0.25f;
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
  
  public void drawMe(){ //draws queen based on state 
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
class Stage{
  ArrayList<Platform> plats = new ArrayList<Platform>();
  PImage floorImg, platImg;
  int bg;
  
  Stage(int preset, int background){
    bg = background;
    switch(preset){
      case 0:
        platImg = loadImage("stage/stage_0_0.png");
        floorImg = loadImage("stage/stage_0_1.png");
        //ground
        plats.add(new Platform(new PVector(width/2, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2-floorImg.width+5, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2+floorImg.width-5, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2-2*(floorImg.width-5), height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2+2*(floorImg.width-5), height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        
        //platforms
        plats.add(new Platform(new PVector(width/2+1.5f*platImg.width, height-5*platImg.height), new PVector(platImg.width, platImg.height), platImg, false, true));
        plats.add(new Platform(new PVector(width/2-1.5f*platImg.width, height-5*platImg.height), new PVector(platImg.width, platImg.height), platImg, false, true));
        
        //walls
        //right
        for(int i = 0; i < 6; i++){
          plats.add(new Platform(new PVector(width+floorImg.width/4-5, height-i*(floorImg.height-60)), new PVector(floorImg.width, floorImg.height), floorImg, true, false));
          plats.add(new Platform(new PVector(-floorImg.width/4+5, height-i*(floorImg.height-60)), new PVector(floorImg.width, floorImg.height), floorImg, true, false));
        }
        break;
      case 1:
        platImg = loadImage("stage/stage_1_0.png");
        floorImg = loadImage("stage/stage_1_1.png");
        //ground
        plats.add(new Platform(new PVector(width/2, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2-floorImg.width+5, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2+floorImg.width-5, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2-2*(floorImg.width-5), height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2+2*(floorImg.width-5), height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        
        //platforms
        plats.add(new Platform(new PVector(width/2+1.5f*platImg.width, height-5*platImg.height), new PVector(platImg.width, platImg.height), platImg, false, true));
        plats.add(new Platform(new PVector(width/2-1.5f*platImg.width, height-5*platImg.height), new PVector(platImg.width, platImg.height), platImg, false, true));
        
        //walls
        //right
        for(int i = 0; i < 6; i++){
          plats.add(new Platform(new PVector(width+floorImg.width/4-5, height-i*(floorImg.height-50)), new PVector(floorImg.width, floorImg.height), floorImg, true, false));
          plats.add(new Platform(new PVector(-floorImg.width/4+5, height-i*(floorImg.height-50)), new PVector(floorImg.width, floorImg.height), floorImg, true, false));
        }
        break;
      case 2:
        platImg = loadImage("stage/stage_2_0.png");
        floorImg = loadImage("stage/stage_2_1.png");
        //ground
        plats.add(new Platform(new PVector(width/2, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2-floorImg.width+5, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2+floorImg.width-5, height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2-2*(floorImg.width-5), height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        plats.add(new Platform(new PVector(width/2+2*(floorImg.width-5), height-floorImg.height/16), new PVector(floorImg.width, floorImg.height), floorImg, true, true));
        
        //platforms
        plats.add(new Platform(new PVector(width/2+1.5f*platImg.width, height-5*platImg.height), new PVector(platImg.width, platImg.height), platImg, false, true));
        plats.add(new Platform(new PVector(width/2-1.5f*platImg.width, height-5*platImg.height), new PVector(platImg.width, platImg.height), platImg, false, true));
        
        //walls
        //right
        for(int i = 0; i < 6; i++){
          plats.add(new Platform(new PVector(width+floorImg.width/4-5, height-i*(floorImg.height-50)), new PVector(floorImg.width, floorImg.height), floorImg, true, false));
          plats.add(new Platform(new PVector(-floorImg.width/4+5, height-i*(floorImg.height-50)), new PVector(floorImg.width, floorImg.height), floorImg, true, false));
        }
        break;
      default:
        platImg = loadImage("stage/stage_0_0.png");
        plats.add(new Platform(new PVector(width/2, height-platImg.height/2), new PVector(platImg.width, platImg.height), platImg, true, true));
        break;
    }
  }
  
  public void checkCollision(Character c){ //check stage collision for a character
    int platsTouched = 0;
    for(int i = 0; i < plats.size(); i++){ //check collision for all platforms
      Platform plat = plats.get(i);
      plat.checkCollision(c);
      if(plat.hasChara) platsTouched++;
    }
    if (platsTouched>0) c.onGround = true; //if player is touching a platform set onGround to true
    else c.onGround = false;
  }
  
  public void checkRebound(Character c){ //check boss rebound for all platforms
    for(int i = 0; i < plats.size(); i++){
      Platform plat = plats.get(i);
      plat.checkRebound(c);
    }
  }
  
  public void drawMe(){ //draws stage
    background(bg);
    for(int i = 0; i < plats.size(); i++){ //draws all individual platforms
      Platform plat = plats.get(i);
      plat.drawMe();
    }
  }
}
  public void settings() {  size(1600, 900); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "D103_Herman_Chan_Assignment4_301398139" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
