/*
 * Temperature HTTP client
 * 
 * Hardware:
 * 
 * - Arduino Uno (5V, GND, Analog X)
 * - LM50 Temperature sensor
 * - W5100 Ethernet shield
 * 
 * Software:
 * 
 * - Prometheus Push Gateway
 */


#include <Ethernet.h>

// input data
byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDE, 0xAC }; // ensure unique value on LAN
byte server[] = { 10, 25, 0, 66}; // IP address of the prometheus push gw
String vhost = "push-gateway.badkub.es";
int port = 80; // HTTP port of the prometheus push gw
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
  String payload ="# TYPE room_temp_celsius gauge\nroom_temp_celsius{room=\"master_bedroom\"} " + String(temp, 2);

  Serial.println("Content-Length: " + String(payload.length() + 1));
  Serial.println(payload);

  /*
   * The ethernet client println command sends a \r\n, which throws off the content-length calculations,
   * and maybe the posh gateway itself. So manually append a carriage return
   */
  if ( client.connect(server, port) ) {
    Serial.println("Connected to server");
    client.println("POST /metrics/job/room_temperature HTTP/1.1");
    client.println("Host: " + vhost);
    client.println("Content-Length: " + String(payload.length() + 1) );
    client.println("Content-Type: application/x-www-form-urlencoded");
    client.println("Connection: Close");
    client.print("\n");
    client.print(payload);
    client.print("\n");
    Serial.println("Data sent");
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
