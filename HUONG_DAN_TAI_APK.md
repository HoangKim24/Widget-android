# 📱 Hướng Dẫn Tải & Cài Đặt File APK "Lịch Tuần Android"

Tài liệu này hướng dẫn chi tiết cách tải file APK đã được hệ thống **GitHub Actions** tự động biên dịch (build), cách giải nén và cài đặt trực tiếp lên điện thoại Android (đặc biệt tối ưu cho ColorOS / OPPO Find X9).

---

## 📌 Bảng mục lục
1. [Cách tải file APK từ GitHub Actions (Khuyên dùng - Nhanh nhất)](#1-cách-tải-file-apk-từ-github-actions)
2. [Cách giải nén và cài đặt APK lên điện thoại](#2-cách-giải-nén-và-cài-đặt-apk-lên-điện-thoại)
3. [Cấp quyền cài đặt ứng dụng ngoài (ColorOS / OPPO / Android 14-15)](#3-cấp-quyền-cài-đặt-ứng-dụng-ngoài)
4. [Tự kích hoạt chạy lại Build mới nhất (Workflow Dispatch)](#4-tự-kích-hoạt-chạy-lại-build-mới-nhất)
5. [Tải mã nguồn (Source Code) và tự Build trên máy tính](#5-tải-mã-nguồn-và-tự-build-trên-máy-tính)
6. [Các câu hỏi thường gặp (FAQ) & Xử lý sự cố](#6-các-câu-hỏi-thường-gặp-faq)

---

## 1. Cách tải file APK từ GitHub Actions

Mỗi khi có cập nhật code mới trên nhánh `main`, GitHub Actions sẽ tự động biên dịch ra bộ cài APK hoàn chỉnh.

### 👉 Các bước tải:

1. **Đăng nhập vào GitHub:**
   > ⚠️ **LƯU Ý QUAN TRỌNG:** Bạn **bắt buộc phải đăng nhập** tài khoản GitHub trên trình duyệt (điện thoại hoặc máy tính) thì mới nhìn thấy và tải được mục **Artifacts**.

2. **Truy cập vào trang Actions của dự án:**
   - Đường dẫn trực tiếp: [https://github.com/HoangKim24/Widget-android/actions](https://github.com/HoangKim24/Widget-android/actions)

3. **Chọn lần chạy gần nhất (Workflow Run):**
   - Tìm dòng đầu tiên có tiêu đề **Build Android APK** hoặc commit gần nhất (có icon dấu tích xanh lá cây `✓` nghĩa là đã build thành công).
   - Nhấp vào tiêu đề của lần chạy đó.

4. **Kéo xuống phần "Artifacts" (ở cuối trang):**
   - Bạn sẽ nhìn thấy mục:
     - 📦 **`LichTuan-Android-Debug-APK`**: Bản cài đặt thử nghiệm đầy đủ tính năng (khuyên dùng).
     - 📦 **`LichTuan-Android-Release-APK`**: Bản phát hành tối ưu.
   - Nhấp vào tên **`LichTuan-Android-Debug-APK`** để bắt đầu tải về. File tải về máy sẽ có đuôi là `.zip` (ví dụ: `LichTuan-Android-Debug-APK.zip`).

---

## 2. Cách giải nén và cài đặt APK lên điện thoại

Vì GitHub luôn nén file artifact thành định dạng `.zip` để bảo toàn dữ liệu, bạn cần giải nén trước khi cài:

### 🔹 Cách thực hiện ngay trên điện thoại:
1. Mở ứng dụng **Quản lý tệp (File Manager)** hoặc **Tệp tin** mặc định trên điện thoại OPPO / Android.
2. Vào thư mục **Tải về (Downloads)**.
3. Nhấn vào file `LichTuan-Android-Debug-APK.zip` vừa tải:
   - Chọn **Giải nén tại đây (Extract here)** hoặc **Giải nén vào thư mục**.
4. Sau khi giải nén, bạn sẽ thấy file có đuôi `.apk` (ví dụ: `app-debug.apk`).
5. Chạm vào file `.apk` đó để bắt đầu cài đặt.

### 🔹 Hoặc tải bằng Máy tính rồi chép sang điện thoại:
1. Tải file `.zip` về máy tính và giải nén (chuột phải chọn *Extract All...*).
2. Kết nối điện thoại với máy tính qua cáp USB (chọn chế độ *Truyền tệp / MTP*).
3. Chép file `app-debug.apk` vào thư mục *Download* của điện thoại.
4. Mở điện thoại, vào Quản lý tệp và bấm vào file `.apk` để cài đặt.

---

## 3. Cấp quyền cài đặt ứng dụng ngoài

Hệ điều hành Android (đặc biệt là ColorOS trên OPPO Find X9) có cơ chế bảo mật cao khi cài file APK ngoài kho ứng dụng Google Play:

### ⚠️ Bước 1: Cho phép cài đặt nguồn không xác định
- Khi bạn nhấn vào file `.apk`, nếu hệ thống hiển thị thông báo:
  > *"Để bảo mật, điện thoại của bạn hiện không được phép cài đặt các ứng dụng không xác định từ nguồn này."*
- Hãy bấm **Cài đặt (Settings)** -> Bật công tắc gạt **"Cho phép từ nguồn này" (Allow from this source)**.
- Quay lại và bấm **Cài đặt (Install)**.

### ⚠️ Bước 2: Cảnh báo từ Google Play Protect
- Vì đây là file tự biên dịch dành cho phát triển (Debug APK) chưa xuất bản trên Google Play Store, Play Protect có thể hiện cảnh báo:
  > *"Ứng dụng bị chặn bởi Play Protect"* hoặc *"Nhà phát triển không xác định"*.
- **Cách xử lý:** 
  1. Nhấn vào dòng chữ nhỏ: **Thông tin chi tiết (More details)** hoặc mũi tên mở rộng.
  2. Chọn nút: **Vẫn cài đặt (Install anyway)**.
  3. Quá trình cài đặt sẽ hoàn tất trong vài giây!

---

## 4. Tự kích hoạt chạy lại Build mới nhất

Nếu bạn muốn yêu cầu GitHub tự động build lại phiên bản APK mới nhất bất kỳ lúc nào mà không cần sửa code:

1. Vào tab **Actions**: [https://github.com/HoangKim24/Widget-android/actions](https://github.com/HoangKim24/Widget-android/actions).
2. Nhìn cột bên trái, nhấn vào workflow **Build Android APK**.
3. Bạn sẽ thấy một thanh thông báo màu xám/xanh có nút **Run workflow** ▾.
4. Bấm vào **Run workflow** -> Chọn nhánh `main` -> Bấm nút xanh **Run workflow**.
5. Đợi khoảng 1 - 2 phút để máy chủ GitHub biên dịch xong (hiện tích xanh `✓`), sau đó tải file APK theo [Mục 1](#1-cách-tải-file-apk-từ-github-actions).

---

## 5. Tải mã nguồn và tự Build trên máy tính

Nếu bạn muốn mở trực tiếp dự án trong **Android Studio** để sửa giao diện hoặc tự tay xuất APK:

### 📥 Bước 1: Lấy mã nguồn về máy tính
- **Cách A (Dùng Git):**
  ```bash
  git clone https://github.com/HoangKim24/Widget-android.git
  ```
- **Cách B (Tải ZIP trực tiếp từ GitHub):**
  - Vào trang chủ repo: [https://github.com/HoangKim24/Widget-android](https://github.com/HoangKim24/Widget-android)
  - Bấm nút xanh **Code** ▾ -> Chọn **Download ZIP**.
  - Giải nén file `.zip` ra ổ đĩa của bạn (ví dụ `D:\Widget-Android`).

### 🛠️ Bước 2: Mở & Build
- Khởi động **Android Studio** (phiên bản Hedgehog, Iguana, Jellyfish, Koala hoặc mới hơn).
- Chọn **Open** và trỏ đến thư mục `Widget-Android`.
- Chờ Gradle Sync hoàn tất tải các thư viện.
- Để xuất file APK cài ngay:
  - Vào menu **Build** -> **Build Bundle(s) / APK(s)** -> **Build APK(s)**.
  - Sau khi build xong, bấm vào chữ **locate** ở góc phải dưới thông báo để lấy file `app-debug.apk`.
- Hoặc chạy lệnh trong Terminal:
  ```powershell
  ./gradlew assembleDebug
  ```
  File APK xuất ra tại: `app/build/outputs/apk/debug/app-debug.apk`.

---

## 6. Các câu hỏi thường gặp (FAQ)

### ❓ Tại sao tôi vào mục Actions mà không thấy phần "Artifacts" để tải?
> **Trả lời:** Do bạn chưa đăng nhập vào tài khoản GitHub trên trình duyệt. GitHub chỉ cho phép người dùng đã đăng nhập tải các file build lưu trữ (Artifacts).

### ❓ Sau khi cài đặt, tiện ích Widget ở đâu?
> **Trả lời:** Hãy ra màn hình chính điện thoại, dùng 2 ngón tay chụm lại (pinch) hoặc nhấn giữ vào khoảng trống trên màn hình -> Chọn **Tiện ích (Widgets)** -> Cuộn tìm ứng dụng **Lịch Tuần** -> Chọn widget dạng lưới và kéo ra màn hình. Trên ColorOS, bạn có thể nhấn giữ widget và kéo cạnh để mở rộng toàn màn hình.

### ❓ Làm sao để đổi hình nền màn hình khóa chuẩn OPPO Find X9?
> **Trả lời:** Vào ứng dụng, bấm nút **Studio Màn Khóa** ở góc trên bên phải. Tùy chỉnh màu sắc, vị trí hiển thị lịch theo ý thích, sau đó bấm nút **"Đặt làm màn khóa"** để áp dụng trực tiếp mà không bị lệch tỉ lệ.
