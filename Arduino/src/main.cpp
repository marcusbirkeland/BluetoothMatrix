#include <Arduino.h>
#include <SoftwareSerial.h>
#include <MD_Parola.h>
#include <MD_MAX72xx.h>
#include <SPI.h>
#include "Font_Data.h"

SoftwareSerial BTserial(4, 3); // RX | TX

#define _SS_MAX_RX_BUFF 256 // RX buffer size
#define CLK_PIN   13
#define DATA_PIN  11
#define CS_PIN    10

#define HARDWARE_TYPE MD_MAX72XX::FC16_HW
#define MAX_DEVICES 10
#define MAX_ZONES  2
#define ZONE_SIZE (MAX_DEVICES/MAX_ZONES)

int SCROLL_SPEED = 30;
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
	BTserial.begin(9600);
	// Set up display
	P.begin(MAX_ZONES);
	P.setInvert(false);
	for (uint8_t i=0; i<MAX_ZONES; i++){ // Set Zones for display
    	P.setZone(i, ZONE_SIZE*i, (ZONE_SIZE*(i+1))-1);
	}
	P.setIntensity(1);
}

void loop() {
	while(BTserial.available()){
    	input = BTserial.readString();
	}
  char buff[256];
  input.toCharArray(buff,256);
  static char message[256];
  char command[64] = "\0";
  if(strncmp(buff, "INTENSITY",strlen("INTENSITY"))== 0 || strncmp(buff, "SPEED",strlen("SPEED"))== 0){
	  strcpy(command, buff);
  } else {
	  strcpy (message, buff);
  }

  if (strncmp(command, "INTENSITY",strlen("INTENSITY"))== 0){
	  int intensity = (command[strlen("INTENSITY")] - '0' )*2;

	  P.setIntensity(intensity);
  } else if (strncmp(command, "SPEED", strlen("SPEED"))==0){
	  SCROLL_SPEED = (command[strlen("SPEED")] - '0') * 10;
  }
  Serial.println("command=" );
  Serial.println(command);
  Serial.println("Message=");
  Serial.println(message);
  display_text(message);

 /*  int intensity=0;
  if(strcmp(buff,"INT+")==0){
		intensity = 16;
		P.setIntensity(intensity);
		Serial.println("INTENSITY" + intensity);
  }
  	else{
			strcpy(message,buff);
  	}
	      	display_text(message);*/
}