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
- **Maven**: 3.8.1 trở lên.
- **Docker** & **Docker Compose**.

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
*(Nếu bạn sử dụng Docker Compose, hãy đổi `localhost` thành tên các container tương ứng như `db`, `minio`, `mqtt`).*

---

## 4. Hướng dẫn chạy dự án

### 4.1. Build file JAR

1. Cài đặt Maven (nếu chưa có):
   - Tải Maven từ [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi).
   - Giải nén và thêm đường dẫn `bin` của Maven vào biến môi trường `PATH`.
   - Kiểm tra cài đặt bằng lệnh:
     ```
     mvn -v
     ```

2. Build dự án:
   ```
   ./mvnw clean package
   ```
   File JAR sẽ được tạo trong thư mục `target/`.

### 4.2. Chạy ứng dụng với Docker

1. Đảm bảo Docker và Docker Compose đã được cài đặt.
2. Cấu hình file `application.properties` dựa trên file mẫu `application.properties.example`.
3. Chạy lệnh sau để khởi động ứng dụng:
   ```
   docker-compose up --build
   ```
4. Truy cập ứng dụng tại [http://localhost:8080](http://localhost:8080).

---

## 5. Chú ý

- Đảm bảo các dịch vụ MinIO và MQTT Broker đã chạy trước khi khởi động ứng dụng.
- Nếu cần thay đổi cấu hình, chỉnh sửa file `application.properties` hoặc các biến môi trường trong `docker-compose.yml`.

---

## 6. Sử dụng hệ thống

Sau khi khởi động thành công, hệ thống cung cấp các giao diện chính sau:

1. **Dashboard Thời gian thực (Home Page)**
   - Truy cập: `http://localhost:8080/`
   - Hiển thị trực tiếp quá trình cà chua đi qua 3 trạm: **Detected (Chờ phân loại) -> Transferred (Chờ gạt) -> Sorted (Đã xử lý xong)**. Bảng thống kê bên phải sẽ cập nhật liên tục.

2. **Lịch sử dữ liệu (History Page)**
   - Truy cập: `http://localhost:8080/history.html`
   - Hiển thị bảng chi tiết các trái cà chua đã quét (bao gồm hình ảnh, nhãn RIPE/UNRIPE/ROTTEN, độ chính xác AI và thời gian) theo từng Batch cụ thể.

3. **API Nhận dữ liệu từ AI (Dành cho AI Server)**
   - Endpoint: `POST /api/fruit`
   - Payload mẫu:
     ```json
     {
         "id": "CAM1_123",
         "result": "RIPE",
         "imageUrl": "http://localhost:9000/tomatoes/CAM1_123.jpg",
         "confidence": 0.95
     }
     ```