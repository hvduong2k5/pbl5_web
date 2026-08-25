# Tomato Detection System

Hệ thống quản lý và giám sát quy trình phân loại tự động cà chua, kết hợp Camera AI và băng chuyền phần cứng, hiển thị dữ liệu thời gian thực trên giao diện Web.

## 1. Tính Năng Chính
- **Giám Sát Thời Gian Thực (Real-time Dashboard)**: Truyền dữ liệu cảm biến và kết quả AI lên giao diện Web thông qua WebSocket.
- **Quản Lý Mẻ Thu Hoạch (Batch Management)**: Lưu trữ thống kê số lượng quả Chín/Xanh/Lỗi theo từng lô.
- **Tích Hợp AI & IoT**: Nhận tín hiệu điều khiển từ vi điều khiển (ESP32/PLC) qua MQTT Broker và nhận dữ liệu phân loại hình ảnh qua REST API/MQTT.
- **Lưu Trữ Hình Ảnh**: Lưu trữ ảnh gốc từ Camera AI lên MinIO (S3-compatible Storage).
- **Trích Xuất Dữ Liệu**: Xuất báo cáo thống kê và danh sách trái cây ra file Excel bằng thư viện EasyExcel.
- **Tối Ưu Hiệu Năng (Caching)**: Sử dụng Caffeine Cache cho xác thực người dùng và phân quyền, giảm truy vấn cơ sở dữ liệu.

## 2. Công Nghệ Sử Dụng

### Backend
- Java 17, Spring Boot 3
- Spring Security, JWT, Spring Data JPA, Hibernate
- Eclipse Paho (MQTT), Spring WebSockets, Caffeine Cache
- MinIO SDK, Alibaba EasyExcel

### Cơ Sở Hạ Tầng (Infrastructure)
- PostgreSQL
- Eclipse Mosquitto (MQTT)
- MinIO
- Docker & Docker Compose

### Kiểm Thử (Testing)
- JUnit 5, Mockito, Spring Boot Test (Độ phủ 100% tầng Service và Controller)

## 3. Yêu Cầu Môi Trường
- Java: JDK 17 trở lên.
- Maven: 3.8.1 trở lên.
- Docker & Docker Compose (khuyến nghị dùng để khởi chạy các dịch vụ DB, MinIO, MQTT).

## 4. Hướng Dẫn Cài Đặt và Khởi Chạy

### 4.1. Cấu Hình
Ứng dụng sử dụng biến môi trường. Tạo file `.env` tại thư mục gốc (nơi chứa file `docker-compose.yml` hoặc `pom.xml`) với nội dung:

```env
# Cấu hình PostgreSQL
DB_URL=jdbc:postgresql://localhost:5432/detection_tomatoes
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Cấu hình MinIO
MINIO_URL=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET_NAME=tomatoes

# Cấu hình MQTT
MQTT_BROKER=tcp://localhost:1883
MQTT_USERNAME=admin
MQTT_PASSWORD=admin
MQTT_TOPIC_SUBSCRIBE=tomato/events
MQTT_TOPIC_PUBLISH=tomato/control

# Cấu hình Security (JWT)
JWT_SECRET=my-very-secure-secret-key-1234567890
JWT_EXPIRATION=2592000000
```

### 4.2. Khởi Chạy Hệ Thống

**Khởi chạy bằng Docker Compose:**
Cần có sẵn file `docker-compose.yml` chứa DB, MinIO, MQTT.
```bash
mvn clean package -DskipTests
docker-compose up --build -d
docker-compose logs -f
```

**Khởi chạy môi trường phát triển (Dev Mode):**
Yêu cầu đã bật PostgreSQL, MQTT và MinIO ở môi trường local.
```bash
mvn clean spring-boot:run
```

## 5. Phân Quyền và Bảo Mật (RBAC)

Hệ thống sử dụng JWT để xác thực và phân quyền.

### Authentication
API Đăng nhập: `POST /api/auth/login`
```json
{
    "username": "admin",
    "password": "123456"
}
```
Lấy `accessToken` từ phản hồi và gắn vào HTTP Header `Authorization: Bearer <Token>`.

### Authorization
- `CONTROL_SYSTEM`: Quyền điều khiển băng chuyền.
- `MANAGE_BATCH`: Quyền tạo và quản lý lô hàng.
- `EXPORT_DATA`: Quyền xuất dữ liệu Excel.
- `VIEW_HISTORY`: Quyền xem lịch sử phân loại.

## 6. Giao Tiếp Phần Cứng (Hardware Integration)

Phần cứng giao tiếp qua MQTT.
Topic: `tomato/events`
Cấu trúc Payload mẫu:
```json
{
    "event": "detected", 
    "id": "esp32_cam_001",
    "label": "RIPE",
    "image_url": "http://192.168.1.166:9000/tomatoes/img_123.jpg",
    "confidence": 0.98,
    "timestamp": "2026-08-25 10:42:10"
}
```

## 7. Chạy Kiểm Thử (Unit Testing)
Hệ thống tích hợp bộ unit test độc lập không phụ thuộc vào hạ tầng thật.
```bash
mvn test
```