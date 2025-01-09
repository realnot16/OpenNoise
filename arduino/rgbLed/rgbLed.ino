//RGB Led
const int rpin=6;
const int gpin=5;
const int bpin=3;




void setup() {
  
  pinMode(rpin, OUTPUT);
  pinMode(gpin, OUTPUT);
  pinMode(bpin, OUTPUT);
}

void loop() {
  //GREEN
  analogWrite(rpin,0);
  analogWrite(gpin,255);
  analogWrite(bpin,0);
  delay(2000);
  //ORANGE
  analogWrite(rpin,255);
  analogWrite(gpin,20);
  analogWrite(bpin,0);

  delay(2000);
  //RED
  analogWrite(rpin,255);
  analogWrite(gpin,0);
  analogWrite(bpin,0);
  delay(2000);

}
