
const double dBAnalogQuiet = 10;//envelope - calibrated value from analog input (48 dB equivalent)
const double dBAnalogModerate =  12;
const double dBAnalogLoud = 17;

int count=0;
double sum=0;
double value=0;
double volt=0;


#define PIN_ANALOG_IN A0

void setup() {
  Serial.begin(9600);
}

void loop() {
  float decibelsValueQuiet = 49.5;
  float decibelsValueModerate = 65;
  float decibelsValueLoud = 70;
  float valueDb;
  volt =  analogRead(PIN_ANALOG_IN);
  count++;
  sum += volt;
  
  if(count==10000){
    value=sum/count;
    count=0;
    sum=0;
    if (value < 13){
      decibelsValueQuiet += calculateDecibels(value, 'q');
      Serial.print("Quiet: ");
      Serial.println(decibelsValueQuiet);
      valueDb =  decibelsValueQuiet;
    }
    else if ((value > 13) && ( value <= 23) ){
      // Serial.println(value);
      decibelsValueModerate += calculateDecibels(value, 'm');
      Serial.print("Moderate: ");
      Serial.println(decibelsValueModerate);
      valueDb = decibelsValueModerate;
    }
    else if(value > 22){
      //Serial.println(value); 
      decibelsValueLoud += calculateDecibels(value, 'l');
      Serial.print("Loud: ");
      Serial.println(decibelsValueLoud);
      valueDb = decibelsValueLoud;
    }
    delay(500);
  }
}


float calculateDecibels(int x, char c){
  float decibelsCalculated;
  if (c == 'q')
    decibelsCalculated = 20 * log10(x/dBAnalogQuiet);
  if (c == 'm')
    decibelsCalculated = 20 * log10(x/dBAnalogModerate);
  if (c == 'l')
    decibelsCalculated = 20 * log10(x/dBAnalogLoud);  
  return (decibelsCalculated);
}
