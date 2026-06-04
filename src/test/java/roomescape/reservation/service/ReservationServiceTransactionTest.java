package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReservationServiceTransactionTest {

    private static final String NAME = "브라운";
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(1);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("예약 취소 중 활성 예약 삭제가 실패하면 취소 이력 저장도 롤백된다.")
    void cancel_rollback_whenDeleteFailsAfterHistoryInserted() {
        // given
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS reservation_delete_blocker
                (
                    reservation_id BIGINT NOT NULL,
                    PRIMARY KEY (reservation_id),
                    FOREIGN KEY (reservation_id) REFERENCES reservation (id)
                )
                """);

        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(16, 30)));
        Theme theme = themeRepository.save(new Theme(
                "취소 롤백 테스트",
                "취소 롤백 테스트용 테마",
                "https://example.com/rollback-theme.png"
        ));
        Reservation reservation = reservationService.create(NAME, FUTURE_DATE, time.getId(), theme.getId());

        try {
            jdbcTemplate.update(
                    "INSERT INTO reservation_delete_blocker (reservation_id) VALUES (?)",
                    reservation.getId()
            );

            // when, then
            assertThatThrownBy(() -> reservationService.cancel(reservation.getId(), NAME))
                    .isInstanceOf(DataIntegrityViolationException.class);

            assertThat(reservationRepository.findById(reservation.getId())).isPresent();
            Integer historyCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM reservation_history WHERE reservation_id = ?",
                    Integer.class,
                    reservation.getId()
            );
            assertThat(historyCount).isZero();
        } finally {
            jdbcTemplate.update("DELETE FROM reservation_delete_blocker WHERE reservation_id = ?", reservation.getId());
            jdbcTemplate.update("DELETE FROM reservation_history WHERE reservation_id = ?", reservation.getId());
            jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", reservation.getId());
            jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", time.getId());
            jdbcTemplate.update("DELETE FROM theme WHERE id = ?", theme.getId());
            jdbcTemplate.execute("DROP TABLE IF EXISTS reservation_delete_blocker");
        }
    }

}
