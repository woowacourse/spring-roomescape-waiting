package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.FixedClockConfig;
import roomescape.domain.Reservation;
import roomescape.exception.DuplicateException;
import roomescape.exception.ResourceNotFoundException;

@SpringBootTest
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationCommandServiceTest {

    // reservation-fixture.sql 기준 (fixed clock: 2026-05-05):
    // id=2: user_b / 2026-06-05 / time2 / theme1 (미래)
    // id=3: user_c / 2026-06-05 / time1 / theme1 (미래)
    // 2026-06-05 / time1 / theme2 슬롯은 비어 있음

    @Autowired
    private ReservationCommandService reservationCommandService;

    @Test
    @DisplayName("이미 예약된 슬롯에는 예약을 생성할 수 없다.")
    void createDuplicateSlot() {
        assertThatThrownBy(() ->
                reservationCommandService.create("new-user", LocalDate.of(2026, 6, 5), 1L, 1L))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("비어 있는 슬롯에는 예약 생성에 성공한다.")
    void createSuccess() {
        Reservation created = reservationCommandService.create("new-user", LocalDate.of(2026, 6, 5), 1L, 2L);

        assertThat(created.id()).isNotNull();
        assertThat(created.owner().name()).isEqualTo("new-user");
    }

    @Test
    @DisplayName("존재하지 않는 예약은 취소할 수 없다.")
    void cancelNonExistent() {
        assertThatThrownBy(() ->
                reservationCommandService.cancel(999L, "user_b"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약은 변경할 수 없다.")
    void updateNonExistent() {
        assertThatThrownBy(() ->
                reservationCommandService.update(999L, "user_b", LocalDate.of(2026, 7, 1), 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("이미 예약된 슬롯으로는 변경할 수 없다.")
    void updateToDuplicateSlot() {
        // id=2(user_b)를 id=3(user_c)이 점유한 2026-06-05/time1/theme1로 변경 시도
        assertThatThrownBy(() ->
                reservationCommandService.update(2L, "user_b", LocalDate.of(2026, 6, 5), 1L))
                .isInstanceOf(DuplicateException.class);
    }
}
