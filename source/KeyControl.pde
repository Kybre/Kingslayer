boolean u, d, l, r, mouseDown; //movement checking booleans
boolean zDown = false; //checks if Z button is being held
boolean xDown = false; //checks if X button is being held

void keyPressed(){
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

void keyReleased(){
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

void mouseClicked(){
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

void mousePressed(){
  mouseDown = true;
  if(mouseButton == RIGHT && !p.shot){ //if player is holding RMB, shoot
    p.shot = true; 
    p.animFrame = 0;
  }
}

void mouseReleased(){
  mouseDown = false;
  if(mouseButton == RIGHT) p.shot = false; //stop player from shooting
}
