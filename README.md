# Tomato Detection System

Hệ thống quản lý và giám sát quy trình phân loại tự động cà chua (Tomato Detection System). Hệ thống này kết hợp với AI Camera và băng chuyền (thông qua MQTT) để cập nhật dữ liệu, hình ảnh (MinIO) và hiển thị trực tiếp (Real-time) trạng thái của trái cây trên Web Dashboard.

## 1. Công nghệ sử dụng
- **Backend**: Java 17, Spring Boot, Spring Data JPA, WebSocket.
- **Frontend**: HTML, CSS, JavaScript (Vanilla, Fetch API, WebSocket).
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

## 5. Sử dụng hệ thống

Sau khi khởi động thành công, hệ thống cung cấp các giao diện chính sau:

1. **Dashboard Thời gian thực (Home Page)**
   - Truy cập: `http://localhost:8080/`
   - Hiển thị trực tiếp quá trình cà chua đi qua 3 trạm: **Detected (Chờ phân loại) -> Transferred (Chờ gạt) -> Sorted (Đã xử lý xong)**. Bảng thống kê bên phải sẽ cập nhật liên tục.

2. **Lịch sử dữ liệu (History Page)**
   - Truy cập: `http://localhost:8080/history.html`
   - Hiển thị bảng chi tiết các trái cà chua đã quét (bao gồm hình ảnh, nhãn RIPE/UNRIPE/ROTTEN, độ chính xác AI và thời gian) theo từng Batch cụ thể.

3. **API Nhận dữ liệu từ AI (Dành cho AI Server)**
   - Endpoint: `POST /api/fruit`
   - Payload mẫu (Ví dụ code Python gửi sang):
     ```json
     {
         "id": "CAM1_123",
         "result": "RIPE",
         "imageUrl": "http://localhost:9000/tomatoes/CAM1_123.jpg",
         "confidence": 0.95
     }
     ```