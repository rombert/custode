/*
 * Temperature HTTP client
 * 
 * Hardware:
 * 
 * - Arduino Uno (5V, GND, Analog X)
 * - DHT22 Temperature sensor
 * - W5100 Ethernet shield
 * 
 * Software:
 * 
 * - Prometheus Push Gateway
 * 
 * See also https://learn.adafruit.com/dht/overview
 */


#include <Ethernet.h>
#include <DHT.h>

// input data
byte mac[] = { 0x00, 0xAA, 0xBB, 0xCC, 0xDE, 0xAC }; // ensure unique value on LAN
char vhost[] = "prometheus-pushgateway.badkub.es";
int port = 80; // HTTP port of the prometheus push gw
int pin = 3; // Analog sensor pin
int dump_delay_millis = 1000;
int loop_delay_millis = 60000;  // we should send values every minute

// globals
EthernetClient client;
DHT dht(pin, DHT22);

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

  dht.begin();
}

void loop() {
  // put your main code here, to run repeatedly:
  // float temp = readTemperature(pin);
  float temp = dht.readTemperature();
  float humidity = dht.readHumidity();
  Serial.println("Temp: " + String(temp) + " C, humidity: " + String(humidity) + "%");
  /*
   * The ethernet client println command sends a \r\n, which throws off the content-length calculations,
   * and maybe the push gateway itself. So manually append a carriage return
   */
  if ( client.connect(vhost, port) ) {
    String payload ="# TYPE room_temp_celsius gauge\nroom_temp_celsius{room=\"living_room\"} " + String(temp, 2) + "\n";
    payload += "# TYPE room_humidity_percentage gauge\nroom_humidity_percentage{room=\"living_room\"} " + String(humidity, 2);
    Serial.println("Connected to server");
    client.println("POST /metrics/job/room_temperature HTTP/1.1");
    client.println("Host: " + String(vhost));
    client.println("Content-Length: " + String(payload.length() + 1) );
    client.println("Content-Type: application/x-www-form-urlencoded");
    client.println("Connection: Close");
    client.print("\n");
    client.print(payload);
    client.print("\n");
    client.flush();
    Serial.println("Data sent, dumping response");
    delay(dump_delay_millis); // need to wait for the response to be available on the client
    while ( client.available() ) {
      char c = client.read();
      Serial.print(c);
    }
    client.stop();
  } else {
    Serial.println("Connection to server failed");
  }

  delay(loop_delay_millis - dump_delay_millis);
}
