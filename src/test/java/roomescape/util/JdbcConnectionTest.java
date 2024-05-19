package roomescape.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.controller.ReservationController;

@DisplayName("JDBC 테스트")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class JdbcConnectionTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationController reservationController;


    @DisplayName("DataSource 객체를 이용하여 Connection 검증한다.")
    @Test
    void connectionTest() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("ReservationController에 있던 데이터베이스 관련 로직 분리를 검증한다.")
    @Test
    void injectionTest() {
        boolean isJdbcTemplateInjected = false;

        for (Field field : reservationController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }
}
