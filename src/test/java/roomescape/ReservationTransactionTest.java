package roomescape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.domain.Waiting;
import roomescape.repository.WaitingRepository;
import roomescape.service.ReservationService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class ReservationTransactionTest {

    private static final String 예약자 = "예약자";
    private static final String 대기자 = "대기자";

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private WaitingRepository waitingRepository;

    private long reservationId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");

        String futureDate = LocalDate.now().plusDays(1).toString();
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "공포", "무서운 테마", "thumb.png");

        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                예약자, futureDate, 1L, 1L);
        jdbcTemplate.update("INSERT INTO waiting (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                대기자, futureDate, 1L, 1L);

        reservationId = 1L;
    }

    @Test
    @DisplayName("정상 취소 시: 예약 삭제/대기 승격/대기 삭제가 모두 함께 반영된다")
    void 정상_취소시_세_변경이_원자적으로_반영된다() {
        reservationService.deleteUserReservation(reservationId, 예약자);

        assertThat(countReservations()).isEqualTo(1);
        assertThat(reservationExists(예약자)).isFalse();
        assertThat(reservationExists(대기자)).isTrue();
        assertThat(countWaitings()).isZero();
    }

    @Test
    @DisplayName("승격 후 대기 삭제 단계에서 장애가 나면: 예약 삭제와 승격까지 모두 롤백된다")
    void 중간_실패시_전체_롤백되어_데이터_일관성이_유지된다() {
        doThrow(new RuntimeException("DB 장애 시뮬레이션"))
                .when(waitingRepository).delete(any(Waiting.class));

        assertThatThrownBy(() -> reservationService.deleteUserReservation(reservationId, 예약자))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 장애 시뮬레이션");

        assertThat(countReservations()).isEqualTo(1);
        assertThat(reservationExists(예약자)).isTrue();
        assertThat(reservationExists(대기자)).isFalse();
        assertThat(countWaitings()).isEqualTo(1);
        assertThat(waitingExists(대기자)).isTrue();
    }

    private int countReservations() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
    }

    private int countWaitings() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM waiting", Integer.class);
    }

    private boolean reservationExists(String name) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE name = ?", Integer.class, name);
        return count != null && count > 0;
    }

    private boolean waitingExists(String name) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE name = ?", Integer.class, name);
        return count != null && count > 0;
    }
}