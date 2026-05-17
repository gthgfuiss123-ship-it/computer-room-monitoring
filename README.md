# Smart Farm Monitor

Hệ thống giám sát nhiệt độ và độ ẩm cho nông nghiệp (chăn nuôi, trồng trọt) sử dụng ứng dụng Android và IoT (ESP32 + Firebase).

## Tổng quan

Hệ thống gồm 2 phần:
- **Ứng dụng Android (Kotlin)**: Hiển thị dữ liệu nhiệt độ, độ ẩm realtime, cảnh báo, lịch sử
- **ESP32 + Cảm biến DHT11**: Thu thập dữ liệu nhiệt độ, độ ẩm và gửi lên Firebase

### Kiến trúc hệ thống

```
ESP32 + DHT11  →  WiFi  →  Firebase Realtime Database  →  Android App
```

## Tính năng

### Ứng dụng Android
- Đăng nhập / Đăng ký tài khoản (Firebase Authentication)
- **Trang chủ**: Dashboard nhiệt độ và độ ẩm realtime
- **Thống kê**: Chi tiết từng cảm biến với thanh tiến trình và ngưỡng cảnh báo
- **Cài đặt**: Thông tin ngưỡng cảnh báo, tài khoản, đăng xuất
- **Lịch sử**: Xem lịch sử dữ liệu cảm biến với trạng thái
- **Cảnh báo**: Thông báo khi nhiệt độ > 40°C hoặc < 10°C, độ ẩm > 80% hoặc < 30%

### Phần cứng (ESP32)
- Đọc nhiệt độ & độ ẩm (DHT11)
- Hiển thị LCD I2C 16x2
- LED trạng thái (xanh: hoạt động)
- Gửi dữ liệu lên Firebase qua WiFi
- Lưu lịch sử tự động mỗi 60 giây

## Cấu trúc thư mục

```
computer-room-monitoring/
├── app/                          # Android App
│   └── src/main/
│       ├── java/.../monitoring/
│       │   ├── ui/
│       │   │   ├── login/        # Màn hình đăng nhập
│       │   │   ├── home/         # Trang chủ + MainActivity
│       │   │   ├── sensor/       # Thống kê cảm biến
│       │   │   ├── settings/     # Cài đặt
│       │   │   └── history/      # Lịch sử
│       │   ├── data/
│       │   │   ├── model/        # Data models
│       │   │   └── repository/   # Firebase Repository
│       │   └── viewmodel/        # ViewModels (MVVM)
│       └── res/                  # Layouts, drawables, values
├── esp32/                        # Code ESP32 Arduino
│   └── esp32_firebase/
│       └── esp32_firebase.ino
├── build.gradle.kts
└── README.md
```

## Hướng dẫn cài đặt

### 1. Tạo Firebase Project

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Tạo project mới (ví dụ: `smart-farm-monitor`)
3. Bật **Authentication** → **Email/Password**
4. Tạo **Realtime Database** → chọn region → bắt đầu ở **test mode**
5. Vào **Project Settings** → **Add app** → chọn **Android**
   - Package name: `com.computerroom.monitoring`
   - Download file `google-services.json`
   - Đặt file vào thư mục `app/`

### 2. Cài đặt Android App

1. Mở project bằng **Android Studio**
2. Đặt file `google-services.json` vào thư mục `app/`
3. Sync Gradle
4. Chạy app trên thiết bị hoặc emulator

### 3. Cài đặt ESP32

1. Cài **Arduino IDE** và thêm board **ESP32**
2. Cài thư viện:
   - `Firebase ESP32 Client` (by Mobizt)
   - `DHT sensor library` (by Adafruit)
   - `LiquidCrystal_I2C`
3. Mở file `esp32/esp32_firebase/esp32_firebase.ino`
4. Thay đổi cấu hình:
   ```cpp
   #define WIFI_SSID     "Tên_WiFi"
   #define WIFI_PASSWORD "Mật_khẩu_WiFi"
   #define FIREBASE_HOST "your-project.firebaseio.com"
   #define FIREBASE_AUTH "your-database-secret"
   ```
5. Upload lên ESP32

### 4. Sơ đồ kết nối phần cứng

```
ESP32 Pin    →  Component
─────────────────────────
GPIO 4       →  DHT11 (Data)
GPIO 18      →  LED Xanh
GPIO 21      →  LCD SDA
GPIO 22      →  LCD SCL
```

## Firebase Database Structure

```json
{
  "sensor": {
    "temperature": 28.5,
    "humidity": 65,
    "timestamp": 1234567890
  },
  "history": {
    "timestamp_key": {
      "temperature": 28.5,
      "humidity": 65,
      "timestamp": 1234567890
    }
  }
}
```

## Ngưỡng cảnh báo

| Chỉ số | Cảnh báo thấp | Bình thường | Cảnh báo cao |
|--------|---------------|-------------|--------------|
| Nhiệt độ | < 10°C | 10°C - 40°C | > 40°C |
| Độ ẩm | < 30% | 30% - 80% | > 80% |

## Kiến trúc phần mềm (MVVM)

```
View (Activity/Fragment)
    ↓ observes LiveData
ViewModel
    ↓ calls
Repository (Singleton)
    ↓ reads/writes
Firebase Realtime Database
```

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|-----------|-----------|
| Ngôn ngữ Android | Kotlin |
| IDE | Android Studio |
| Database | Firebase Realtime Database |
| Authentication | Firebase Auth |
| Architecture | MVVM |
| UI | Material Design Components |
| Vi điều khiển | ESP32 |
| Cảm biến | DHT11 |
| Giao tiếp | WiFi |

## Thành viên nhóm

- Phát triển ứng dụng Android (Kotlin)
- Phát triển phần cứng ESP32
- Thiết kế mạch và kết nối cảm biến

## License

MIT License
