# Tomato Detection System

Hệ thống quản lý và giám sát quy trình phân loại tự động cà chua (Tomato Detection System). Hệ thống này kết hợp với AI Camera và băng chuyền (thông qua MQTT) để cập nhật dữ liệu, hình ảnh (MinIO) và hiển thị trực tiếp (Real-time) trạng thái của trái cây trên Web Dashboard.

## 1. Công nghệ sử dụng
- **Backend**: Java 17, Spring Boot, Spring Data JPA, WebSocket, Spring Security (JWT, RBAC).
- **Frontend**: HTML, CSS, JavaScript (Vanilla, Fetch API, WebSocket). Giao diện Responsive UI tương thích cả Desktop và Mobile.
- **Database**: PostgreSQL.
- **Message Broker**: MQTT.
- **Storage**: MinIO (lưu trữ hình ảnh từ AI Camera).
- **Deployment**: Docker & Docker Compose.

---

## 2. Yêu cầu hệ thống (Prerequisites)
Để chạy dự án, máy tính của bạn cần cài đặt sẵn:
- **Java**: JDK 17 trở lên.
- **Docker** & **Docker Compose** (Dành cho việc chạy nhanh các dịch vụ phụ trợ như DB, MinIO, MQTT).
- **Maven**: 3.8.1 trở lên.

### Hướng dẫn cài đặt Maven chi tiết

**Đối với Windows:**
1. Truy cập trang chủ Maven: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
2. Tải xuống file nén `Binary zip archive` (ví dụ: `apache-maven-3.9.6-bin.zip`).
3. Giải nén file vừa tải vào một thư mục cố định trên máy (ví dụ: `C:\Program Files\apache-maven-3.9.6`).
4. Thiết lập biến môi trường (Environment Variables):
   - Mở Start Menu, gõ **Environment Variables** và chọn *Edit the system environment variables*.
   - Bấm nút **Environment Variables...**.
   - Ở phần *System variables*, bấm **New...** tạo biến mới:
     - Variable name: `M2_HOME`
     - Variable value: `C:\Program Files\apache-maven-3.9.6`
   - Tìm biến `Path` trong danh sách *System variables*, chọn **Edit** -> **New** và thêm dòng: `%M2_HOME%\bin`
   - Nhấn OK để lưu tất cả.
5. Kiểm tra lại bằng cách mở Command Prompt (cmd) và gõ:
   ```cmd
   mvn -v
   ```
   *(Nếu hiện ra phiên bản Maven và Java là thành công).*

**Đối với macOS (dùng Homebrew):**
Mở Terminal và chạy lệnh:
```bash
brew install maven
```
Kiểm tra lại: `mvn -v`

**Đối với Linux (Ubuntu/Debian):**
Mở Terminal và chạy lệnh:
```bash
sudo apt update
sudo apt install maven
```
Kiểm tra lại: `mvn -v`

---

## 3. Cấu hình ứng dụng

Ứng dụng đọc cấu hình từ file `application.properties` (hoặc `application.yml`) nằm trong thư mục `src/main/resources/`. 

Dưới đây là các thông số quan trọng cần thiết lập để hệ thống kết nối thành công với các dịch vụ bên ngoài:

```properties
# 1. Cấu hình Cơ sở dữ liệu PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/detection_tomatoes
spring.datasource.username=postgres
spring.datasource.password=postgres

# 2. Cấu hình MinIO (Lưu ảnh AI)
minio.url=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket-name=tomatoes

# 3. Cấu hình MQTT Broker (Nhận tín hiệu băng chuyền/AI)
mqtt.broker-url=tcp://localhost:1883
mqtt.username=admin
mqtt.password=admin
mqtt.topic.subscribe=tomato/events
```
*(Nếu bạn sử dụng Docker Compose để chạy toàn bộ, hãy đổi `localhost` thành tên các container tương ứng như `db`, `minio`, `mqtt`).*

---

## 4. Hướng dẫn chạy dự án

Có 2 cách để chạy hệ thống:

### Cách 1: Chạy trực tiếp bằng Maven (Dành cho Dev)
Nếu bạn đã tự cài đặt và chạy PostgreSQL, MinIO, và MQTT trên máy cá nhân:
1. Mở Terminal (hoặc CMD) tại thư mục gốc của project (nơi chứa file `pom.xml`).
2. Build và chạy ứng dụng Spring Boot:
   ```bash
   mvn clean spring-boot:run
   ```

### Cách 2: Chạy toàn bộ hệ thống bằng Docker Compose (Khuyên dùng)
Nếu file `docker-compose.yml` của bạn đã định nghĩa đầy đủ các services (Spring Boot, DB, MQTT, MinIO):
1. Build file JAR của Backend trước (bỏ qua Test để build nhanh):
   ```bash
   mvn clean package -DskipTests
   ```
2. Khởi chạy toàn bộ hệ thống bằng Docker Compose:
   ```bash
   docker-compose up --build -d
   ```
3. Xem log của hệ thống để đảm bảo mọi thứ hoạt động ổn định:
   ```bash
   docker-compose logs -f
   ```
4. Để tắt hệ thống, chạy lệnh:
   ```bash
   docker-compose down
   ```

---

## 5. Phân quyền và Bảo mật (RBAC & JWT)

Hệ thống sử dụng **JWT (JSON Web Token)** để bảo mật không trạng thái (stateless) kết hợp với mô hình phân quyền **RBAC (Role-Based Access Control)**.

### Cách lấy Token
- Gửi request `POST /api/auth/login` với body JSON chứa thông tin tài khoản mặc định (đã được tạo sẵn trong `data.sql`):
  ```json
  {
      "username": "admin",
      "password": "123456"
  }
  ```
- Hệ thống trả về `accessToken`. Đưa token này vào header `Authorization: Bearer <token>` để truy cập các API bị khóa.

### Các quyền (Permissions) hoạt động:
Một số API nhạy cảm yêu cầu User gửi kèm Token có chứa quyền tương ứng:
- **`CONTROL_SYSTEM`**: Dùng để điều khiển băng chuyền (Start/Stop).
- **`MANAGE_BATCH`**: Dùng để tạo các mẻ (Batch) cà chua mới.
- **`EXPORT_DATA`**: Dùng để trích xuất file Excel thông tin các quả cà chua.

---

## 6. Giao diện và Giao thức kết nối
 
 1. **Dashboard Giám sát Thời gian thực (Home Page)**
    - Truy cập: `http://localhost:8080/`
    - Giao diện thân thiện tương thích trên mọi thiết bị (Responsive).
    - Hiển thị trực tiếp quá trình quả chạy qua các trạm trên băng chuyền (Phát hiện -> Chuyển -> Phân loại -> Vào hộp).
 
 2. **Lịch sử dữ liệu (History Page)**
    - Truy cập: `http://localhost:8080/history.html`
    - Thống kê dạng bảng chi tiết các trái cà chua đã quét theo từng mẻ (Batch).
 
 3. **Quản lý người dùng và Quyền (Admin Page)**
    - Giao diện Admin quản lý tài khoản và phân quyền cho hệ thống.

 4. **Giao thức Nhận dữ liệu (Dành cho AI Camera & ESP32)**
    - Dữ liệu được gửi hoàn toàn qua **MQTT**.
    - Topic: `tomato/events`
    - Payload mẫu (Định dạng JSON gửi từ Camera hoặc Vi điều khiển):
      ```json
      {
          "event": "detected", // Các sự kiện: detected, classified, transfer, sorted, status
          "id": "6453745",
          "label": "RIPE",
          "type": null,
          "image_url": "http://192.168.1.166:9000/tomatoes/6453745.jpg",
          "confidence": 0.95,
          "timestamp": "2026-06-03 22:42:10"
      }
      ```
    - Khi có sự kiện `status`, hệ thống cập nhật trạng thái hoạt động (VD: `Running`, `Stopped`) của băng chuyền lên giao diện thông qua WebSocket.