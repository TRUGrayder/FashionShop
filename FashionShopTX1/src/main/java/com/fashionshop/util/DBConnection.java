package com.fashionshop.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp kết nối JDBC thuần (đúng yêu cầu TX1)
 * Hỗ trợ tự động fallback linh hoạt giữa cổng 3306 và 3307 để mọi thành viên nhóm
 * và máy hiện tại đều chạy mượt mà mà không lo sai cổng XAMPP.
 */
@Component
public class DBConnection {

    private static final Logger log = LoggerFactory.getLogger(DBConnection.class);

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    private volatile String workingUrl;

    public Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }

        // Nếu đã xác định được cổng hoạt động, ưu tiên dùng ngay
        String targetUrl = (workingUrl != null) ? workingUrl : url;

        try {
            Connection conn = DriverManager.getConnection(targetUrl, username, password);
            if (workingUrl == null) {
                workingUrl = targetUrl;
            }
            return conn;
        } catch (SQLException ex) {
            // Nếu bị lỗi kết nối cổng, tự động thử đảo giữa 3306 và 3307
            if (url != null && (url.contains(":3306") || url.contains(":3307"))) {
                String altUrl = targetUrl.contains(":3306")
                        ? targetUrl.replace(":3306", ":3307")
                        : targetUrl.replace(":3307", ":3306");
                try {
                    Connection conn = DriverManager.getConnection(altUrl, username, password);
                    workingUrl = altUrl;
                    log.info(">>> Đã tự động kết nối thành công MySQL tại cổng thay thế: {}", altUrl);
                    return conn;
                } catch (SQLException ignored) {
                    // Cả 2 cổng đều không kết nối được
                }
            }
            throw ex;
        }
    }
}
