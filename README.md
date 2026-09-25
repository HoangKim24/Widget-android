# 📅 Lịch Tuần Android (OPPO Find X9 Edition)

[![Android CI](https://github.com/HoangKim24/Widget-android/actions/workflows/build-apk.yml/badge.svg)](https://github.com/HoangKim24/Widget-android/actions/workflows/build-apk.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-green.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35%20(Android%2015)-brightgreen.svg)](https://developer.android.com)
[![Tailored For](https://img.shields.io/badge/Optimized%20for-OPPO%20Find%20X9%20%7C%20ColorOS-orange.svg)](https://www.oppo.com)

Ứng dụng **Quản lý Thời khóa biểu & Lịch tuần 7 ngày**, tích hợp **Home Screen Widget** cao cấp và **Studio tạo hình nền màn hình khóa** được tính toán tỷ lệ vàng chuẩn xác cho **OPPO Find X9** (ColorOS) cùng các thiết bị Android thế hệ mới.

---

## ⚡ Tải Ngay File Cài Đặt (APK)

👉 **Xem hướng dẫn chi tiết từng bước:** [**`HUONG_DAN_TAI_APK.md`**](HUONG_DAN_TAI_APK.md)

| Phiên bản | Cách tải | Tình trạng |
|---|---|---|
| **Bản thử nghiệm (Debug APK)** | [Tải từ GitHub Actions Artifacts](https://github.com/HoangKim24/Widget-android/actions) | Tự động cập nhật mỗi commit |
| **Bản chính thức (Release APK)** | [GitHub Releases](https://github.com/HoangKim24/Widget-android/releases) | Khi gắn tag phiên bản |

> 💡 **Mẹo:** Vào tab [Actions](https://github.com/HoangKim24/Widget-android/actions) -> Bấm vào lần build có dấu tích xanh gần nhất -> Kéo xuống dưới mục **Artifacts** để tải file `LichTuan-Android-Debug-APK.zip`.

---

## 🌟 Điểm Nổi Bật & Tính Năng Chính

### 1. 🗓️ Lưới Lịch Tuần 7 Ngày Trực Quan (Weekly Grid Schedule)
- Hiển thị đầy đủ cả tuần từ **Thứ 2 đến Chủ Nhật** trên một trục thời gian khoa học.
- Hỗ trợ đổi tuần linh hoạt (tuần trước, tuần sau, quay về tuần hiện tại).
- Phân loại sự kiện đa dạng với bảng màu sắc nét:
  - 📘 **Học Tập** (Study)
  - 💼 **Công Việc** (Work)
  - 🏃 **Thể Thao & Cá Nhân** (Personal/Sport)
  - 👥 **Họp & Gặp Mặt** (Meeting)
  - 🎯 **Quan Trọng** (Important)
- Hiển thị rõ tên môn/công việc, địa điểm học/phòng làm việc và khung giờ bắt đầu - kết thúc.

### 2. 🔴 Thước Đo Thời Gian Thực (Live Time Indicator)
- Vạch báo thời gian thực tế tự động trôi theo phút trong ngày.
- Tự động gắn tag **LIVE** nhấp nháy nổi bật trên sự kiện hoặc ca học đang diễn ra.

### 3. 📱 Home Screen Widget (Jetpack Glance)
- Tiện ích màn hình chính hiện đại được xây dựng trên nền tảng **Jetpack Glance**.
- Hỗ trợ co giãn tự do (Resizing) từ kích thước `2x2` đến `4x4` hoặc bung trọn màn hình trên **ColorOS / OxygenOS**.
- Dữ liệu đồng bộ tức thời khi bạn thêm, sửa hoặc xóa sự kiện trong ứng dụng.

### 4. ⏰ Hệ Thống Chuông Báo Thức & Thông Báo Đúng Giờ (Exact Alarms)
- Tích hợp chuẩn `AlarmManager.setExactAndAllowWhileIdle()` giúp báo thức đổ chuông chính xác từng giây ngay cả khi máy ở chế độ ngủ sâu (Doze Mode).
- Đổ chuông và rung cảnh báo trước khi sự kiện bắt đầu 15 phút.
- Broadcast Receiver xử lý độc lập, khởi động lại cảnh báo tự động sau khi thiết bị khởi động lại.

### 5. 🖼️ Lock Screen Wallpaper Studio (Tối ưu cho OPPO Find X9)
- Công cụ xuất ảnh hình nền chất lượng cao chuẩn tỷ lệ **19.8:9** (độ phân giải gốc **1264 x 2780 px**).
- Tính toán chính xác **Safe Zone (Vùng an toàn)**:
  - Tránh phần trên cùng dành cho Đồng hồ ColorOS và cụm camera nốt ruồi.
  - Tránh vùng dưới dành cho Cảm biến vân tay quang học trong màn hình và thanh điều hướng cử chỉ.
- Tùy biến tự do 4 nhóm cài đặt:
  - **Màu nền:** Đơn sắc OLED, Gradient hoàng hôn, Cyberpunk, Forest, hoặc ảnh cá nhân.
  - **Kiểu bố cục lịch:** Dạng lưới đầy đủ, danh sách dọc tối giản, v.v.
  - **Vị trí hiển thị:** Căn trên, căn giữa màn hình hoặc căn dưới.
  - **Độ mờ & thẻ màu:** Tinh chỉnh độ trong suốt của nền lịch giúp hòa quyện vào hình nền gốc.
- Tính năng **"Đặt làm màn hình khóa"** 1 chạm thông qua `WallpaperManager.FLAG_LOCK`.

### 6. 📷 OCR Quét Ảnh Thời Khóa Biểu (Google ML Kit)
- Quét nhanh ảnh chụp thời khóa biểu giấy hoặc ảnh chụp màn hình máy tính.
- Tự động nhận diện chữ, khung giờ và thứ trong tuần để nạp tự động vào lịch.

### 7. 💾 Lưu Trữ Bền Vững & Kiến Trúc Hiện Đại
- Cơ sở dữ liệu **SQLite** cục bộ bền vững, bảo vệ dữ liệu an toàn kể cả khi thoát ứng dụng hoặc xoay màn hình.
- Kiến trúc tuân thủ chuẩn **MVVM (Model - View - ViewModel)** kết hợp Kotlin **Coroutines & StateFlow**.

---

## 🏛️ Cấu Trúc Dự Án (Architecture)

```text
Widget-Android/
├── .github/workflows/
│   └── build-apk.yml               # CI/CD tự động build file APK khi đẩy code lên
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml     # Đăng ký quyền, Activity, Receiver, Widget
│   │   ├── java/com/hoangkim/widget/
│   │   │   ├── data/               # SQLite OpenHelper lưu trữ lịch trình
│   │   │   ├── model/              # Entity: CalendarEvent, EventCategory, Preset...
│   │   │   ├── receiver/           # BroadcastReceiver báo thức & cập nhật lịch
│   │   │   ├── repository/         # EventRepository quản lý luồng dữ liệu StateFlow
│   │   │   ├── ui/
│   │   │   │   ├── studio/         # Màn hình Lock Screen Wallpaper Studio
│   │   │   │   ├── weekly/         # Màn hình Lưới Lịch Tuần 7 Ngày
│   │   │   │   └── theme/          # Hệ thống Theme, Color, Type Material 3
│   │   │   ├── viewmodel/          # WeeklyScheduleViewModel, StudioViewModel
│   │   │   ├── wallpaper/          # Bộ vẽ Canvas hình nền tỷ lệ 19.8:9
│   │   │   ├── widget/             # Android AppWidget & Jetpack Glance Widget
│   │   │   └── MainActivity.kt     # Entry point & Điều hướng ứng dụng
│   │   └── res/                    # Icon, Vector, Drawable, Strings
│   └── build.gradle.kts            # Cấu hình dependency của app
├── preview/
│   └── app_preview.html            # Bản xem trước giao diện tương tác trên Web
├── HUONG_DAN_TAI_APK.md            # Tài liệu hướng dẫn tải và cài đặt chi tiết
└── README.md                       # Giới thiệu tổng quan dự án
```

---

## 🛠️ Công Nghệ Sử Dụng

- **Ngôn ngữ:** Kotlin 2.0.0
- **UI Toolkit:** Jetpack Compose + Material Design 3
- **Kiến trúc:** MVVM + AndroidViewModel + StateFlow + Coroutines
- **Lưu trữ:** SQLite Database (Offline-first)
- **Tiện ích màn hình chính:** Android AppWidgetProvider & AndroidX Glance
- **Xử lý nền & Cảnh báo:** Android `AlarmManager` + `NotificationCompat`
- **Thị giác máy tính (OCR):** Google ML Kit Text Recognition
- **CI/CD:** GitHub Actions (Ubuntu runner, JDK 17, Gradle 8.10.2)

---

## 💻 Hướng Dẫn Dành Cho Lập Trình Viên (Developer Guide)

### Yêu cầu hệ thống:
- **Android Studio:** Koala (2024.1+) hoặc Ladybug / mới hơn.
- **JDK:** OpenJDK 17.
- **Android SDK:** Compile SDK 35, Min SDK 26.

### Thao tác nhanh qua dòng lệnh:

```bash
# 1. Tải dự án về máy
git clone https://github.com/HoangKim24/Widget-android.git
cd Widget-android

# 2. Biên dịch bản Debug APK
./gradlew assembleDebug

# 3. Cài đặt trực tiếp lên thiết bị đang cắm cáp
./gradlew installDebug
```

File APK sau khi build thành công sẽ nằm ở:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🌐 Xem Trước Giao Diện (Preview)
Bạn có thể mở tệp [`preview/app_preview.html`](preview/app_preview.html) trên bất kỳ trình duyệt web nào (Chrome, Edge, Safari) để trải nghiệm mô phỏng giao diện tương tác của ứng dụng với đầy đủ hoạt ảnh và tính năng xem trước.

---

## 📄 Bản Quyền & Giấy Phép
Dự án được xây dựng và duy trì bởi **HoangKim24**.
Phát hành theo giấy phép mã nguồn mở mở rộng phục vụ học tập và phát triển cá nhân.
