/*
 * ESP32 - Firebase Realtime Database
 * Hệ thống giám sát phòng máy tính
 * 
 * Phần cứng:
 *   - ESP32 DevKit
 *   - DHT22 (hoặc DHT11) cảm biến nhiệt độ & độ ẩm
 *   - PIR cảm biến chuyển động
 *   - LED xanh (trạng thái hoạt động)
 *   - LED đỏ (cảnh báo)
 *   - Buzzer
 *   - Relay module (điều khiển quạt, đèn)
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
#define WIFI_SSID     "YOUR_WIFI_SSID"       // <-- Thay bằng tên WiFi của bạn
#define WIFI_PASSWORD "YOUR_WIFI_PASSWORD"    // <-- Thay bằng mật khẩu WiFi

// ==================== CẤU HÌNH FIREBASE ====================
#define FIREBASE_HOST "YOUR_PROJECT.firebaseio.com"  // <-- Thay bằng URL Firebase
#define FIREBASE_AUTH "YOUR_FIREBASE_DATABASE_SECRET" // <-- Thay bằng Database Secret

// ==================== LCD I2C ====================
#define SDA_PIN 21
#define SCL_PIN 22
LiquidCrystal_I2C lcd(0x27, 16, 2);

// ==================== DHT22 ====================
#define DHTPIN  4
#define DHTTYPE DHT22
DHT dht(DHTPIN, DHTTYPE);

// ==================== PIR ====================
#define PIR_PIN 27

// ==================== LED ====================
#define LED_GREEN 18
#define LED_RED   19

// ==================== BUZZER ====================
#define BUZZER_PIN 26

// ==================== RELAY (điều khiển thiết bị) ====================
#define RELAY_FAN   25
#define RELAY_LIGHT 33

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

  // ===== PIR =====
  pinMode(PIR_PIN, INPUT);

  // ===== LED =====
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_RED, OUTPUT);

  // ===== BUZZER =====
  pinMode(BUZZER_PIN, OUTPUT);

  // ===== RELAY =====
  pinMode(RELAY_FAN, OUTPUT);
  pinMode(RELAY_LIGHT, OUTPUT);
  digitalWrite(RELAY_FAN, LOW);
  digitalWrite(RELAY_LIGHT, LOW);

  // ===== Trạng thái ban đầu =====
  digitalWrite(LED_GREEN, HIGH);
  digitalWrite(LED_RED, LOW);
  digitalWrite(BUZZER_PIN, LOW);

  // ===== LCD khởi động =====
  lcd.setCursor(0, 0);
  lcd.print("ROOM MONITOR");
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

  // ===== Khởi tạo trạng thái thiết bị trên Firebase =====
  Firebase.setBool(firebaseData, "/devices/fan", false);
  Firebase.setBool(firebaseData, "/devices/light", false);
  Firebase.setBool(firebaseData, "/devices/buzzer", false);
}

void loop() {
  unsigned long currentTime = millis();

  // ===== Đọc cảm biến =====
  float humidity = dht.readHumidity();
  float temperature = dht.readTemperature();
  int motion = digitalRead(PIR_PIN);

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

  // ===== Xử lý chuyển động =====
  if (motion == HIGH) {
    digitalWrite(LED_RED, HIGH);
    lcd.setCursor(0, 1);
    lcd.print("MOTION ALERT! ");
    Serial.println("Phat hien chuyen dong!");
  } else {
    digitalWrite(LED_RED, LOW);
    lcd.setCursor(0, 1);
    lcd.print("SAFE          ");
  }

  // ===== Gửi dữ liệu lên Firebase (mỗi 2 giây) =====
  if (currentTime - lastSendTime >= SEND_INTERVAL) {
    lastSendTime = currentTime;

    Firebase.setFloat(firebaseData, "/sensor/temperature", temperature);
    Firebase.setFloat(firebaseData, "/sensor/humidity", humidity);
    Firebase.setBool(firebaseData, "/sensor/motion", motion == HIGH);
    time_t now;
    time(&now);
    Firebase.setInt(firebaseData, "/sensor/timestamp", (int)now);

    Serial.print("Sent -> T: ");
    Serial.print(temperature);
    Serial.print(" H: ");
    Serial.print(humidity);
    Serial.print(" Motion: ");
    Serial.println(motion == HIGH ? "YES" : "NO");
  }

  // ===== Lưu lịch sử (mỗi 60 giây) =====
  if (currentTime - lastHistoryTime >= HISTORY_INTERVAL) {
    lastHistoryTime = currentTime;

    String historyPath = "/history/" + String(millis());
    Firebase.setFloat(firebaseData, historyPath + "/temperature", temperature);
    Firebase.setFloat(firebaseData, historyPath + "/humidity", humidity);
    Firebase.setBool(firebaseData, historyPath + "/motion", motion == HIGH);
    time_t nowHist;
    time(&nowHist);
    Firebase.setInt(firebaseData, historyPath + "/timestamp", (int)nowHist);

    Serial.println("Da luu lich su");
  }

  // ===== Đọc lệnh điều khiển từ Firebase =====
  // Quạt
  if (Firebase.getBool(firebaseData, "/devices/fan")) {
    bool fanOn = firebaseData.boolData();
    digitalWrite(RELAY_FAN, fanOn ? HIGH : LOW);
  }

  // Đèn
  if (Firebase.getBool(firebaseData, "/devices/light")) {
    bool lightOn = firebaseData.boolData();
    digitalWrite(RELAY_LIGHT, lightOn ? HIGH : LOW);
  }

  // Buzzer
  if (Firebase.getBool(firebaseData, "/devices/buzzer")) {
    bool buzzerOn = firebaseData.boolData();
    digitalWrite(BUZZER_PIN, buzzerOn ? HIGH : LOW);
  }

  delay(500);
}
