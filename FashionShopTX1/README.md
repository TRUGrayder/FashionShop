# 🛍️ Fashion Shop TX1 - Nhóm 8

> **Đề tài:** Xây dựng website thương mại điện tử tích hợp trợ lý AI theo kiến trúc phân tầng sử dụng Spring Boot, MySQL và Qdrant cho cửa hàng thời trang  
> **Học phần:** Bài tập lớn / TX1  
> **Nhóm thực hiện:** Nhóm 8

---

## 🛠️ Công nghệ sử dụng
- **Backend:** Java 17, Spring Boot 3.2.5
- **Data Access:** Pure JDBC (`JdbcTemplate`, `RowMapper`) theo đúng yêu cầu phân tầng TX1
- **Database:** MySQL (MariaDB / XAMPP)
- **Frontend:** Thymeleaf template engine + Bootstrap 5.3.3 + Bootstrap Icons
- **Kiến trúc:** Layered Architecture: `Controller` → `Service` (Interface + Impl) → `DAO` (Interface + JDBC Impl) → `MySQL`

---

## 🚀 Hướng dẫn chạy nhanh cho thành viên nhóm

### 1. Chuẩn bị Cơ sở dữ liệu (MySQL)
1. Mở **XAMPP Control Panel** -> Bấm **Start** MySQL.
2. Mở trình duyệt vào **http://localhost/phpmyadmin**.
3. Vào tab **SQL**, mở file [`sql/fashion_shop.sql`](file:///sql/fashion_shop.sql), copy toàn bộ nội dung và bấm **Go** (Thực thi) để tạo database `fashion_shop` và nạp sẵn dữ liệu mẫu.

### 2. Cấu hình (`application.properties`)
File cấu hình tại `src/main/resources/application.properties` đã được đặt về cổng localhost mặc định:
```properties
# Server
server.port=8080

# Cấu hình MySQL mặc định
spring.datasource.url=jdbc:mysql://localhost:3306/fashion_shop?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
```
*(Nếu MySQL máy bạn dùng cổng 3307 hoặc có đặt mật khẩu cho root, hãy chỉnh lại tương ứng).*

### 3. Khởi chạy ứng dụng
- **Cách 1 (IntelliJ IDEA):** Mở thư mục `FashionShopTX1` trong IntelliJ, tìm file `src/main/java/com/fashionshop/FashionShopApplication.java` và bấm nút **Run ▶️** (hoặc `Shift + F10`).
- **Cách 2 (Terminal):** Chạy lệnh:
  ```bash
  mvn spring-boot:run
  ```

---

## 🌐 Đường dẫn truy cập & Tài khoản demo

Truy cập hệ thống tại: **http://localhost:8080**

### Tài khoản mẫu:
- 👑 **Quản trị viên (Admin):**
  - Username: `admin` | Password: `123456`
  - Quản lý sản phẩm, đơn hàng, duyệt trạng thái và xem dashboard thống kê.
- 👤 **Khách hàng (User):**
  - Username: `user1` (hoặc `user2`) | Password: `123456`
  - Đặt hàng, giỏ hàng tự tính tiền realtime, quản lý thông tin tài khoản, xem và hủy đơn hàng.

---

## 📌 Các chức năng chính đã hoàn thiện
1. **Khách hàng:**
   - Xem danh sách sản phẩm, chi tiết sản phẩm, lọc theo danh mục.
   - Giỏ hàng thông minh: Nút tăng/giảm số lượng tự động cập nhật tổng tiền và đồng bộ cơ sở dữ liệu theo thời gian thực (không cần bấm cập nhật thủ công).
   - Đặt hàng & thanh toán (checkout) với địa chỉ và thông tin người nhận.
   - Trang cá nhân (`/profile`): Cập nhật họ tên, số điện thoại, địa chỉ nhận hàng mặc định, đổi mật khẩu và xem thống kê đơn hàng.
   - Lịch sử đơn hàng (`/orders`): Theo dõi trạng thái đơn hàng (tiếng Việt), xem chi tiết hóa đơn, hỗ trợ hủy đơn hàng với tính năng tự động hoàn lại số lượng tồn kho.
   - Đăng nhập/Đăng ký (`/login`, `/register`): Giao diện hiện đại, nút hiện/ẩn mật khẩu, nút 1 chạm điền nhanh tài khoản demo.
2. **Quản trị viên (Admin):**
   - Dashboard trực quan: Doanh thu, số đơn, số sản phẩm, tình trạng kho.
   - Quản lý sản phẩm (`/admin/products`): Thêm mới, chỉnh sửa, xóa sản phẩm, kiểm tra form validation.
   - Quản lý đơn hàng (`/admin/orders`): Danh sách đơn hàng, duyệt trạng thái trực tiếp (Chờ xử lý, Đã xác nhận, Đang giao hàng, Hoàn thành, Đã hủy).

---

## 🧪 Kiểm thử (Unit Test)
Chạy kiểm thử:
```bash
mvn test
```
- Test DAO: Kiểm tra truy vấn danh sách sản phẩm qua JDBC.
- Test Service/Validation: Kiểm tra quy tắc nghiệp vụ khi thêm sản phẩm và xử lý dữ liệu.
