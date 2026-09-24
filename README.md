# Lịch Tuần - Android (OPPO Find X9 Edition)
Ứng dụng xem Thời khóa biểu, Lịch làm việc tuần 7 ngày, Android Home Screen Widget và Studio tạo hình nền màn hình khóa chuẩn cho **OPPO Find X9** (ColorOS).

## Tính năng chính
- 📅 **Lưới Lịch Tuần 7 Ngày:** Thứ 2 đến Chủ Nhật, hiển thị theo trục thời gian thực.
- 🔴 **Real-time Indicator:** Vạch đỏ trôi theo thời gian thực và tự động phát sáng ô sự kiện đang diễn ra.
- 📱 **Home Screen Widget (ColorOS):** Tiện ích màn hình chính hiển thị toàn bộ tuần, hỗ trợ kéo dãn toàn màn hình.
- ⏰ **Báo Thức & Nhắc Nhở Đúng Giờ:** Sử dụng `AlarmManager` chính xác từng giây để đổ chuông nhắc vào tiết/ca làm.
- 🖼️ **Studio Hình Nền Màn Khóa:** Xuất hình nền chuẩn tỷ lệ 19.8:9 (1264x2780 px) vừa vặn Safe Zone của OPPO Find X9.
- 📷 **OCR Quét Ảnh TKB:** Bóc tách thời khóa biểu từ ảnh bằng Google ML Kit.

## Kiến trúc công nghệ
- Ngôn ngữ: Kotlin
- UI Framework: Jetpack Compose (Material 3)
- Widget: Jetpack Glance / AppWidgetProvider
- OCR: Google ML Kit Text Recognition
