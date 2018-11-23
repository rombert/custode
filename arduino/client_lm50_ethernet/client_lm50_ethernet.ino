/*
 * Temperature HTTP client
 * 
 * Hardware:
 * 
 * - Arduino Uno (5V, GND, Analog X)
 * - LM50 Temperature sensor
 * - W5100 Ethernet shield
 */


#include <Ethernet.h>

// input data
byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDE, 0xAC }; // ensure unique value on LAN
byte server[] = { 192, 168, 1, 100}; // IP address of the HTR server
int port = 8081; // HTTP port of the HTR server
int pin = 3; // Analog sensor pin

// globals
EthernetClient client;

void setup() {
  Serial.begin(9600);
  Serial.println("Initialize Ethernet with DHCP:");
  if (Ethernet.begin(mac) == 0) {
    Serial.println("Failed to configure Ethernet using DHCP");
    if (Ethernet.hardwareStatus() == EthernetNoHardware) {
      Serial.println("Ethernet shield was not found.  Sorry, can't run without hardware. :(");
    } else if (Ethernet.linkStatus() == LinkOFF) {
      Serial.println("Ethernet cable is not connected.");
    }
    // no point in carrying on, so do nothing forevermore:
    while (true) {
      delay(1);
    }
  }

  Serial.print("IP address: ");
  Serial.println(Ethernet.localIP());
}

void loop() {
  // put your main code here, to run repeatedly:
  float temp = readTemperature(pin);
  int tempToSend = (int) (temp * 100 );

  if ( client.connect(server, port) ) {
    Serial.println("Connected to server");
    client.println("POST /record?source=bedroom_temp&timestamp=-1&temp_celsius=" + String(tempToSend) +" HTTP/1.0");
    client.println("Content-Type: application/x-www-form-urlencoded");
    client.println();    
  } else {
    Serial.println("Connection to server failed");
  }

  delay(60000); // we should send values every minute
}

float readTemperature(int temperaturePin) {
  float voltage, degreesC, readVal;
  readVal = analogRead(temperaturePin);
  voltage = readVal * 5 / 1024;
  degreesC = (voltage - 0.5) * 100;

  Serial.print(" pin value: ");
  Serial.print(readVal);
  Serial.print(" voltage: ");
  Serial.print(voltage);
  Serial.print("  deg C: ");
  Serial.println(degreesC);

  return degreesC;
}
