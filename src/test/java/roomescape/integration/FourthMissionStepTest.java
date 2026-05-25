package roomescape.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.integration.support.SpringWebTest;
import roomescape.reservation.controller.ReservationController;

@SpringWebTest
public class FourthMissionStepTest {

    @Autowired
    private ReservationController reservationController;

    @Test
    void 계층화_리팩터링() {
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
