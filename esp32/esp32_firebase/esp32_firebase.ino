/*
 * ESP32 - Firebase Realtime Database
 * Smart Farm Monitor - Giám sát nhiệt độ & độ ẩm nông nghiệp
 * 
 * Phần cứng:
 *   - ESP32 DevKit
 *   - DHT11 cảm biến nhiệt độ & độ ẩm
 *   - LED xanh (trạng thái hoạt động)
 *   - LCD I2C 16x2
 * 
 * Thư viện cần cài:
 *   - Firebase ESP Client (by mobizt)
 *   - DHT sensor library (by Adafruit)
 *   - LiquidCrystal_I2C
 * 
 * Hướng dẫn:
 *   1. Thay WIFI_SSID và WIFI_PASSWORD bằng thông tin WiFi của bạn
 *   2. Thay FIREBASE_HOST và FIREBASE_AUTH bằng thông tin Firebase project
 *   3. Upload code lên ESP32
 */

#include <WiFi.h>
#include <FirebaseESP32.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <DHT.h>
#include <time.h>

// ==================== NTP ====================
#define NTP_SERVER "pool.ntp.org"
#define GMT_OFFSET  25200  // UTC+7 (Vietnam) in seconds
#define DST_OFFSET  0

// ==================== CẤU HÌNH WIFI ====================
#define WIFI_SSID     "Hoàng Khang"       // <-- Thay bằng tên WiFi của bạn
#define WIFI_PASSWORD "passwifi"    // <-- Thay bằng mật khẩu WiFi

// ==================== CẤU HÌNH FIREBASE ====================
#define FIREBASE_HOST "gen-lang-client-0691122932-default-rtdb.firebaseio.com"  // <-- Thay bằng URL Firebase
#define FIREBASE_AUTH "FqTKRoCl1hTLJ3l5opgjZlI0vgWSVLsdVwDxHQ8U"   // <-- Thay bằng Database Secret

// ==================== LCD I2C ====================
#define SDA_PIN 21
#define SCL_PIN 22
LiquidCrystal_I2C lcd(0x27, 16, 2);

// ==================== DHT11 ====================
#define DHTPIN  4
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);

// ==================== LED ====================
#define LED_GREEN 18

// ==================== FIREBASE ====================
FirebaseData firebaseData;
FirebaseConfig firebaseConfig;
FirebaseAuth firebaseAuth;

// ==================== BIẾN THỜI GIAN ====================
unsigned long lastSendTime = 0;
unsigned long lastHistoryTime = 0;
const unsigned long SEND_INTERVAL = 2000;      // Gửi sensor data mỗi 2 giây
const unsigned long HISTORY_INTERVAL = 60000;  // Lưu history mỗi 60 giây

void setup() {
  Serial.begin(115200);

  // ===== I2C & LCD =====
  Wire.begin(SDA_PIN, SCL_PIN);
  lcd.init();
  lcd.backlight();

  // ===== DHT =====
  dht.begin();

  // ===== LED =====
  pinMode(LED_GREEN, OUTPUT);
  digitalWrite(LED_GREEN, HIGH);

  // ===== LCD khởi động =====
  lcd.setCursor(0, 0);
  lcd.print("FARM MONITOR");
  lcd.setCursor(0, 1);
  lcd.print("Connecting...");

  // ===== Kết nối WiFi =====
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Dang ket noi WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  Serial.print("Da ket noi WiFi. IP: ");
  Serial.println(WiFi.localIP());

  // ===== Kết nối Firebase =====
  firebaseConfig.host = FIREBASE_HOST;
  firebaseConfig.signer.tokens.legacy_token = FIREBASE_AUTH;
  
  Firebase.begin(&firebaseConfig, &firebaseAuth);
  Firebase.reconnectWiFi(true);

  // ===== Cấu hình NTP để lấy thời gian thực =====
  configTime(GMT_OFFSET, DST_OFFSET, NTP_SERVER);
  Serial.println("Dang dong bo thoi gian NTP...");
  struct tm timeinfo;
  while (!getLocalTime(&timeinfo)) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nDa dong bo thoi gian NTP!");

  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("WiFi: OK");
  lcd.setCursor(0, 1);
  lcd.print("Firebase: OK");
  delay(2000);
  lcd.clear();
}

void loop() {
  unsigned long currentTime = millis();

  // ===== Đọc cảm biến =====
  float humidity = dht.readHumidity();
  float temperature = dht.readTemperature();

  // ===== Kiểm tra lỗi DHT =====
  if (isnan(humidity) || isnan(temperature)) {
    lcd.setCursor(0, 0);
    lcd.print("DHT ERROR     ");
    lcd.setCursor(0, 1);
    lcd.print("CHECK SENSOR  ");
    Serial.println("Loi cam bien DHT!");
    delay(2000);
    return;
  }

  // ===== Hiển thị LCD =====
  lcd.setCursor(0, 0);
  lcd.print("T:");
  lcd.print(temperature, 1);
  lcd.print((char)223);
  lcd.print("C ");

  lcd.setCursor(9, 0);
  lcd.print("H:");
  lcd.print(humidity, 0);
  lcd.print("% ");

  // ===== Hiển thị trạng thái =====
  lcd.setCursor(0, 1);
  if (temperature > 40 || temperature < 10 || humidity > 80 || humidity < 30) {
    lcd.print("CANH BAO!     ");
  } else {
    lcd.print("BINH THUONG   ");
  }

  // ===== Gửi dữ liệu lên Firebase (mỗi 2 giây) =====
  if (currentTime - lastSendTime >= SEND_INTERVAL) {
    lastSendTime = currentTime;

    Firebase.setFloat(firebaseData, "/sensor/temperature", temperature);
    Firebase.setFloat(firebaseData, "/sensor/humidity", humidity);
    time_t now;
    time(&now);
    Firebase.setInt(firebaseData, "/sensor/timestamp", (int)now);

    Serial.print("Sent -> T: ");
    Serial.print(temperature);
    Serial.print(" H: ");
    Serial.println(humidity);
  }

  // ===== Lưu lịch sử (mỗi 60 giây) =====
  if (currentTime - lastHistoryTime >= HISTORY_INTERVAL) {
    lastHistoryTime = currentTime;

    time_t nowHist;
    time(&nowHist);
    String historyPath = "/history/" + String((unsigned long)nowHist);
    Firebase.setFloat(firebaseData, historyPath + "/temperature", temperature);
    Firebase.setFloat(firebaseData, historyPath + "/humidity", humidity);
    Firebase.setInt(firebaseData, historyPath + "/timestamp", (int)nowHist);

    Serial.println("Da luu lich su");
  }

  delay(500);
}
