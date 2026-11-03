# 📖 HƯỚNG DẪN CÀI ĐẶT VÀ CHẠY PROJECT - FASHION SHOP (NHÓM 8)

> **Đề tài:** Xây dựng website thương mại điện tử tích hợp trợ lý AI theo kiến trúc phân tầng sử dụng Spring Boot, MySQL và Qdrant cho cửa hàng thời trang  
> **Học phần:** Bài tập lớn / TX1  
> **Nhóm thực hiện:** Nhóm 8

---

## 💻 YÊU CẦU MÔI TRƯỜNG MÁY TÍNH
Trước khi chạy project, hãy đảm bảo máy tính đã cài đặt:
1. **Java JDK 17** trở lên (Khuyến nghị JDK 17 hoặc JDK 21).
2. **XAMPP** (hoặc MySQL Server / MySQL Workbench / DBeaver) để chạy cơ sở dữ liệu MySQL.
3. **IDE lập trình:** Khuyến nghị dùng **IntelliJ IDEA** (Community hoặc Ultimate) hoặc **Eclipse / VS Code**.
4. **Maven:** Đã được tích hợp sẵn trong IntelliJ IDEA (không cần cài thêm ngoài nếu dùng IntelliJ).

---

## 🚀 CÁC BƯỚC CÀI ĐẶT VÀ CHẠY PROJECT

### BƯỚC 1: Khởi động MySQL & Import Database

1. Mở phần mềm **XAMPP Control Panel**, nhấn **Start** tại mục **MySQL** (cổng mặc định thường là `3306`).
2. Mở trình duyệt web truy cập vào: **http://localhost/phpmyadmin**  
   *(Hoặc dùng MySQL Workbench / DBeaver / Navicat tùy thói quen)*.
3. Tạo cơ sở dữ liệu & nhập dữ liệu:
   - **Cách 1 (Nhanh nhất):** Chọn tab **SQL** trong phpMyAdmin, mở file:
     `FashionShopTX1/sql/fashion_shop.sql`
     sao chép toàn bộ nội dung và dán vào ô SQL rồi bấm **Go / Thực hiện**.
   - **Cách 2:** Nhấn **Import** trên phpMyAdmin -> Chọn tệp `FashionShopTX1/sql/fashion_shop.sql` -> Nhấn **Import**.
4. Sau khi import xong, bạn sẽ thấy cơ sở dữ liệu `fashion_shop` với 6 bảng:
   - `users` (người dùng & quản trị viên)
   - `categories` (danh mục sản phẩm)
   - `products` (sản phẩm thời trang)
   - `cart_items` (giỏ hàng)
   - `orders` (đơn hàng)
   - `order_details` (chi tiết đơn hàng)

---

### BƯỚC 2: Kiểm tra cấu hình kết nối (`application.properties`)

Mở file:
📁 `FashionShopTX1/src/main/resources/application.properties`

Nội dung cấu hình mặc định đã được thiết lập sẵn cho môi trường chuẩn:
```properties
# Server Port
server.port=8080

# Cấu hình kết nối MySQL (Mặc định XAMPP: port 3306, user root, không mật khẩu)
spring.datasource.url=jdbc:mysql://localhost:3306/fashion_shop?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

> ⚠️ **LƯU Ý DÀNH CHO THÀNH VIÊN:**
> - Nếu MySQL trên máy bạn chạy cổng khác (ví dụ `3307`), hãy sửa `3306` thành cổng của bạn.
> - Nếu tài khoản `root` của bạn có mật khẩu, hãy điền vào sau `spring.datasource.password=mat_khau_cua_ban`.
> - Nếu cổng web `8080` bị trùng với ứng dụng khác, có thể đổi sang `server.port=8081`.

---

### BƯỚC 3: Mở project và Chạy ứng dụng

#### 🔹 Cách 1: Chạy bằng IntelliJ IDEA (Khuyến khích)
1. Mở **IntelliJ IDEA** -> Chọn **File** -> **Open** (hoặc *Open Project*).
2. Trỏ tới thư mục `FashionShopTX1` (nơi chứa file `pom.xml`).
3. Chờ IntelliJ tự động tải các thư viện Maven (thanh tiến trình ở góc dưới bên phải).
4. Tìm đến file khởi chạy chính theo đường dẫn:
   `src/main/java/com/fashionshop/FashionShopApplication.java`
5. Nhấp chuột phải vào file hoặc bấm biểu tượng **Run ▶️** màu xanh (hoặc phím tắt `Shift + F10`) để chạy.
6. Khi thấy dòng log thông báo `Started FashionShopApplication in ... seconds` là hệ thống đã khởi động thành công!

#### 🔹 Cách 2: Chạy bằng dòng lệnh Terminal / CMD
1. Mở cửa sổ **Terminal** hoặc **Command Prompt (CMD)** tại thư mục `FashionShopTX1`.
2. Gõ lệnh:
   ```bash
   mvn spring-boot:run
   ```

---

## 🌐 DANH SÁCH ĐƯỜNG DẪN & TÀI KHOẢN TRẢI NGHIỆM

Sau khi ứng dụng chạy thành công, mở trình duyệt web và truy cập:

| Chức năng | Đường dẫn (URL) | Mô tả |
| :--- | :--- | :--- |
| **Trang chủ** | [http://localhost:8080](http://localhost:8080) | Xem banner, sản phẩm hot, danh mục nổi bật |
| **Danh sách sản phẩm** | [http://localhost:8080/products](http://localhost:8080/products) | Lọc theo danh mục, tìm kiếm, xem giá |
| **Giỏ hàng** | [http://localhost:8080/cart](http://localhost:8080/cart) | Tăng giảm số lượng realtime tự động tính tiền |
| **Đăng nhập** | [http://localhost:8080/login](http://localhost:8080/login) | Có nút bấm 1 chạm điền nhanh tài khoản test |
| **Đăng ký** | [http://localhost:8080/register](http://localhost:8080/register) | Đăng ký tài khoản thành viên mới |
| **Trang cá nhân** | [http://localhost:8080/profile](http://localhost:8080/profile) | Cập nhật hồ sơ, địa chỉ mặc định, đổi mật khẩu |
| **Lịch sử đơn hàng** | [http://localhost:8080/orders](http://localhost:8080/orders) | Xem tiến độ đơn hàng, chi tiết đơn, hủy đơn |
| **Dashboard Quản trị** | [http://localhost:8080/admin/dashboard](http://localhost:8080/admin/dashboard) | Thống kê doanh thu, số sản phẩm, đơn hàng |
| **Quản lý sản phẩm** | [http://localhost:8080/admin/products](http://localhost:8080/admin/products) | Thêm, sửa, xóa sản phẩm |
| **Quản lý đơn hàng** | [http://localhost:8080/admin/orders](http://localhost:8080/admin/orders) | Duyệt đơn và cập nhật trạng thái đơn hàng |

---

### 🔑 TÀI KHOẢN DEMO CÓ SẴN TRONG CƠ SỞ DỮ LIỆU

| Loại tài khoản | Tên đăng nhập (`username`) | Mật khẩu (`password`) | Vai trò / Quyền hạn |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | `admin` | `123456` | Toàn quyền trang Admin (`/admin`), duyệt đơn, quản lý kho hàng |
| **Khách hàng 1 (User)** | `user1` | `123456` | Mua hàng, giỏ hàng, đặt hàng, quản lý đơn cá nhân |
| **Khách hàng 2 (User)** | `user2` | `123456` | Tài khoản khách hàng mẫu |

*(Ở trang Đăng nhập có sẵn 2 nút bấm **"Điền Admin demo"** và **"Điền User demo"** để đăng nhập ngay lập tức mà không cần gõ phím).*

---

## 🏗️ KIẾN TRÚC PROJECT (DÙNG ĐỂ BÁO CÁO / THUYẾT MINH TX1)

Dự án được xây dựng chuẩn theo mô hình phân tầng **Layered Architecture** đáp ứng 100% yêu cầu TX1:
```text
Controller (Web Layer) 
    ⬇
Service Interface & Implementation (Business Logic Layer) 
    ⬇
DAO Interface & JDBC Implementation (Data Access Layer - Pure JDBC / JdbcTemplate) 
    ⬇
MySQL Database
```

- **Backend:** Java 17, Spring Boot 3.2.5.
- **Data Access:** Tuân thủ kỹ thuật JDBC thuần (`JdbcTemplate`, `RowMapper`), không phụ thuộc Hibernate/JPA.
- **Frontend:** Server-side rendering với Thymeleaf kết hợp Bootstrap 5.3.3 và Bootstrap Icons.
- **Validation:** Kiểm tra hợp lệ 2 lớp (Client-side HTML5/JavaScript + Server-side Controller).
- **Xử lý nghiệp vụ kho:** Tự động trừ tồn kho khi mua hàng thành công và tự động hoàn trả số lượng vào kho khi hủy đơn.

---

## 🛠️ XỬ LÝ LỖI THƯỜNG GẶP (FAQ)

1. **Lỗi `Communications link failure` hoặc `Connection refused`:**
   - **Nguyên nhân:** Chưa bật MySQL trong XAMPP hoặc sai cổng kết nối.
   - **Cách sửa:** Kiểm tra xem XAMPP đã Start MySQL chưa. Xem cổng trong XAMPP là `3306` hay `3307` để sửa lại file `application.properties`.

2. **Lỗi `Access denied for user 'root'@'localhost'`:**
   - **Nguyên nhân:** Tài khoản root MySQL của bạn có mật khẩu nhưng trong `application.properties` đang để trống.
   - **Cách sửa:** Điền đúng mật khẩu MySQL của bạn vào dòng `spring.datasource.password=...`.

3. **Lỗi `Web server failed to start. Port 8080 was already in use`:**
   - **Nguyên nhân:** Cổng `8080` đang bị ứng dụng khác trên máy chiếm dụng.
   - **Cách sửa:** Sửa dòng `server.port=8080` thành `server.port=8081` trong `application.properties`, sau đó truy cập web qua `http://localhost:8081`.
