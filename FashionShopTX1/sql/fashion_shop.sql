-- =============================================
-- FASHION SHOP - Database Schema hoàn chỉnh
-- Đề tài: Website thương mại điện tử tích hợp trợ lý AI
-- Spring Boot + MySQL + Qdrant | Nhóm 8
-- =============================================

CREATE DATABASE IF NOT EXISTS fashion_shop
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fashion_shop;

DROP TABLE IF EXISTS order_details;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    image VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(15,2) NOT NULL,
    quantity INT DEFAULT 0,
    image VARCHAR(500),
    category_id BIGINT,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_product (user_id, product_id)
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING',
    shipping_address TEXT,
    phone VARCHAR(20),
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    price DECIMAL(15,2) NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

INSERT INTO users (username, password, full_name, email, phone, address, role) VALUES
('admin', '123456', 'Quản trị viên', 'admin@fashionshop.com', '0901234567', 'Hà Nội', 'ADMIN'),
('user1', '123456', 'Nguyễn Văn An', 'an@gmail.com', '0912345678', '123 Nguyễn Huệ, Q1, TP.HCM', 'USER'),
('user2', '123456', 'Trần Thị Bình', 'binh@gmail.com', '0987654321', '45 Lê Lợi, Hà Nội', 'USER');

INSERT INTO categories (name, description, image) VALUES
('Áo thun', 'Áo thun nam nữ form đẹp, chất cotton cao cấp', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400'),
('Áo sơ mi', 'Áo sơ mi công sở và dạo phố lịch lãm', 'https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=400'),
('Quần jean', 'Quần jean cao cấp, form chuẩn', 'https://images.unsplash.com/photo-1542272454315-4c01d7abdf4a?w=400'),
('Váy đầm', 'Váy đầm nữ tính, dự tiệc và hàng ngày', 'https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=400'),
('Áo khoác', 'Áo khoác thời trang thu đông', 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400'),
('Phụ kiện', 'Túi xách, mũ, thắt lưng, trang sức', 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=400');

INSERT INTO products (name, description, price, quantity, image, category_id) VALUES
('Áo thun Basic Cotton Premium', 'Áo thun cotton 100% form regular, mềm mại, thấm hút tốt. Phù hợp mặc hàng ngày.', 199000, 50, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500', 1),
('Áo thun Oversize Hàn Quốc', 'Áo thun oversize phong cách Hàn Quốc, form rộng thoải mái, nhiều màu sắc.', 249000, 35, 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=500', 1),
('Áo thun Polo Classic', 'Áo polo cổ bẻ thanh lịch, chất liệu cá sấu cao cấp.', 299000, 40, 'https://images.unsplash.com/photo-1586790170083-2f9ceadc732d?w=500', 1),
('Áo sơ mi Oxford trắng', 'Áo sơ mi Oxford dài tay màu trắng, lịch lãm cho công sở.', 399000, 30, 'https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=500', 2),
('Áo sơ mi kẻ sọc', 'Áo sơ mi kẻ sọc thanh lịch, phù hợp môi trường công sở.', 379000, 25, 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500', 2),
('Quần jean Slim Fit', 'Quần jean slim fit co giãn tốt, tôn dáng, màu xanh đậm.', 499000, 40, 'https://images.unsplash.com/photo-1542272454315-4c01d7abdf4a?w=500', 3),
('Quần jean Baggy', 'Quần jean baggy form rộng đang hot trend.', 529000, 28, 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500', 3),
('Váy đầm hoa nhí', 'Váy đầm hoa nhí nữ tính, nhẹ nhàng, phù hợp dạo phố và đi chơi.', 459000, 22, 'https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=500', 4),
('Đầm body dự tiệc', 'Đầm body ôm sát quyến rũ, phù hợp dự tiệc tối.', 699000, 15, 'https://images.unsplash.com/photo-1566174053879-31528523f8ae?w=500', 4),
('Áo khoác Bomber', 'Áo khoác bomber phong cách streetwear, giữ ấm tốt.', 599000, 20, 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500', 5),
('Áo blazer công sở', 'Áo blazer thanh lịch, form chuẩn cho môi trường công sở.', 749000, 18, 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=500', 5),
('Túi xách da thật Mini', 'Túi xách da bò cao cấp, thiết kế tối giản sang trọng.', 899000, 12, 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=500', 6),
('Mũ bucket thời trang', 'Mũ bucket form đẹp, nhiều màu, dễ phối đồ.', 159000, 45, 'https://images.unsplash.com/photo-1572307480813-ceb0e445d4d8?w=500', 6);
