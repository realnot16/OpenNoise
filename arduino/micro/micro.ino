#include <LiquidCrystal.h>

//RGB Led
const int bpin=3;
const int gpin=5;
const int rpin=6;


//LCD
const int rs=12, en=13, d4=8, d5=9, d6=10, d7=11;
LiquidCrystal lcd(rs,en,d4,d5,d6,d7);

//BUZZER
const int buzzerPin=2;
const int buttonPin=4;
const int ledPin=7;

int buttonState=0;
int buttonChanged=0;

//Sound Measure
const double dBAnalogQuiet = 10;   
const double dBAnalogModerate =  12;
const double dBAnalogLoud = 17;

float valueDb;

int inputPin = A0;
int count=0;
double volt=0;
double sum=0;
double value=0;

//interactions
char state;
int flagAlert=0;
int contMax=0;
float maxDb=0; 






void setup()
{
  Serial.begin(9600); // open serial port, set the baud rate to 9600 bps
  lcd.begin(16,2); //start the lcd
  lcd.print("Valore dB: ");
  pinMode(rpin, OUTPUT);
  pinMode(bpin, OUTPUT);
  pinMode(gpin, OUTPUT);
  pinMode(buzzerPin, OUTPUT);
  pinMode(buttonPin, INPUT);
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin,HIGH);
  buttonState=1;
}

void loop()
{
  
  volt =  analogRead(inputPin);
  count++;
  sum += volt;
  if(count==10000){     //set interval for sound checking. 10000: 2 sec
    value=sum/count+1;
    count=0;
    sum=0;
    
    checkSound(value);
    buttonChanged=0;
  }

  if(buttonChanged==0&&digitalRead(buttonPin)==HIGH){   //ATTIVO O DISATTIVO IL BUZZER
    if(buttonState==0){
      buttonChanged=1;
      buttonState=1;
      digitalWrite(ledPin,HIGH);
    }
    else{
      buttonChanged=1;
      buttonState=0;
      digitalWrite(ledPin,LOW);
    }
  }
}

void checkSound(float sound){
  if (sound < 13)
  {
    state='q';
    rgbPin(state);
    buzzer(state);
    valueDb= calculateDecibels(sound, state);
    displayLCD(valueDb,state);
    checkPrint(valueDb);
  }
  else if ((sound >= 13) && ( sound <= 17) )
  {
    state='m';
    rgbPin(state);
    buzzer(state);
    valueDb = calculateDecibels(sound, state);
    displayLCD(valueDb,state);
    checkPrint(valueDb);

  }
  else if(sound > 17)
  {
    state='l';
    rgbPin(state);
    buzzer(state);
    valueDb = calculateDecibels(sound, state);;
    displayLCD(valueDb,state);
    checkPrint(valueDb);
  }
  delay(500);
}

//INVIO IL VALORE DB TRAMITE PORTA SERIALE
void checkPrint(float dB){  
  if(maxDb<dB){ //Memorizzo il valore massimo individuato
    maxDb=dB;
  }
  contMax++;
  if(contMax==9){ //ogni 9 iterazioni invio il valore tramite porta seriale
    Serial.println(maxDb);
    maxDb=0;
    contMax=0;
  }
}

//MODIFICO LO STATO DEL LED
void rgbPin(char c){ 
  if (c == 'q'){  //GREEN
    analogWrite(rpin,0);
    analogWrite(gpin,200);
    analogWrite(bpin,0);
  }
  if (c == 'm'){  //ORANGE
    analogWrite(rpin,255);
    analogWrite(gpin,20);
    analogWrite(bpin,0);
  }
  if (c == 'l'){  //RED
    analogWrite(rpin,200);
    analogWrite(gpin,0);
    analogWrite(bpin,0);
  }
}

//MODIFICO LO STATO DEL BUZZER
void buzzer(char c){
        
  if (c == 'q'||c == 'm'){         //Se il suono è basso o normale
    if(flagAlert>=0) flagAlert=0;  //reset Alert count
  }
  if (c == 'l'){                   //Se il suono è troppo alto
    flagAlert++;
    
    if(flagAlert>=3&&buttonState==1){
      tone(buzzerPin, 1000); // Send 1KHz sound signal
      delay(1000);        // 1 sec delay
      noTone(buzzerPin);     // Stop sound
    }
  }
}

void displayLCD(float db,char c){

  lcd.setCursor(11,0);
  lcd.print(db);
  lcd.setCursor(0,1);
  
  if (c == 'q'){
    lcd.print("A Quiet Space!  ");
  }
  if (c == 'm'){
    lcd.print("Parla piano!    ");
  }
  if (c == 'l'){
    lcd.print("Abbassa la voce!");
  }
}

//CALCOLO IL VALORE IN DECIBEL
float calculateDecibels(float x, char c){
  float decibelsCalculated;
  if (c == 'q')
    decibelsCalculated = 20 * log10(x/dBAnalogQuiet)+49.5;
  if (c == 'm')
    decibelsCalculated = 20 * log10(x/dBAnalogModerate)+65;
  if (c == 'l')
    decibelsCalculated = 20 * log10(x/dBAnalogLoud)+70;  
  return (decibelsCalculated);
}
