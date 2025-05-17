package roomescape;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.ReservationController;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class LayeredArchitectureTest {

    private final ReservationController reservationController;

    public LayeredArchitectureTest(
            @Autowired final ReservationController reservationController
    ) {
        this.reservationController = reservationController;
    }

    @DisplayName("reservationController의 필드에, JdbcTemplate가 존재하지 않는지 테스트")
    @Test
    void 구단계() {
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
