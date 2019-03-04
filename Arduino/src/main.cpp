#include <Arduino.h>
#include <MD_Parola.h>
#include <MD_MAX72xx.h>
#include <SPI.h>
#include "Font_Data.h"

#define CLK_PIN   13
#define DATA_PIN  11
#define CS_PIN    10

#define HARDWARE_TYPE MD_MAX72XX::FC16_HW
#define MAX_DEVICES 10
#define MAX_ZONES  2
#define ZONE_SIZE (MAX_DEVICES/MAX_ZONES)

#define SCROLL_SPEED 30
#define SPEED_TIME  20
#define PAUSE_TIME  0

MD_Parola P = MD_Parola(HARDWARE_TYPE, CS_PIN, MAX_DEVICES);
String input="Heihei";

void display_text(char *input_string){        
  P.setFont(0, BigFontLower);
  P.setFont(1, BigFontUpper);
  P.displayZoneText(1, input_string, PA_LEFT, SCROLL_SPEED, 0, PA_SCROLL_LEFT, PA_SCROLL_LEFT);
  P.displayZoneText(0, input_string, PA_RIGHT, SCROLL_SPEED, 0, PA_SCROLL_LEFT, PA_SCROLL_LEFT);
  uint8_t  curZone = 1;
  while (!P.getZoneStatus (curZone))
    P.displayAnimate(); // Refresh display
}

void setup() {
	pinMode(LED_BUILTIN, OUTPUT);
	Serial.begin(9600); // Default communication rate of the Bluetooth module
	// Set up display
	P.begin(MAX_ZONES);
	P.setInvert(false);
	for (uint8_t i=0; i<MAX_ZONES; i++){ // Set Zones for display
    	P.setZone(i, ZONE_SIZE*i, (ZONE_SIZE*(i+1))-1);
	}
}

void loop() {
	while(Serial.available()){
    	input = Serial.readString();
	}
  char buff[256];
  input.toCharArray(buff,256);
  if(strncmp("INTENSITY_CONTROL",buff,strlen("INTENSITY_CONTROL"))==0){
 	  uint8_t dig1 = buff[strlen("INTENSITY_CONTROL")];
	  uint8_t dig2 = buff[strlen("INTENSITY_CONTROL")+1];
   	  uint8_t intensity = dig1*10+dig2;
	if(intensity >=0 && intensity <= 16){
    	P.setIntensity(dig1*10+dig2);
	}
	}else{
    	display_text(buff);
  	}
}