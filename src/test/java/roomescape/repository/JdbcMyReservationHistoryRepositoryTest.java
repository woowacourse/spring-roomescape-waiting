package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.theme.Theme;
import roomescape.repository.history.JdbcMyReservationHistoryRepository;
import roomescape.repository.history.dto.MyReservationHistoryRow;
import roomescape.repository.reservation.JdbcReservationRepository;
import roomescape.repository.reservationtime.JdbcReservationTimeRepository;
import roomescape.repository.reservationwaiting.JdbcReservationWaitingRepository;
import roomescape.repository.theme.JdbcThemeRepository;

@JdbcTest
class JdbcMyReservationHistoryRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcMyReservationHistoryRepository historyRepository;
    private JdbcReservationRepository reservationRepository;
    private JdbcReservationTimeRepository reservationTimeRepository;
    private JdbcThemeRepository themeRepository;
    private JdbcReservationWaitingRepository waitingRepository;

    @BeforeEach
    void setup() {
        clearTables();
        historyRepository = new JdbcMyReservationHistoryRepository(jdbcTemplate);
        reservationRepository = new JdbcReservationRepository(jdbcTemplate);
        reservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
        themeRepository = new JdbcThemeRepository(jdbcTemplate);
        waitingRepository = new JdbcReservationWaitingRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("이름으로 예약과 대기를 함께 조회하고, 대기에는 순번이 계산된다")
    void findByUserName() {
        Theme theme = themeRepository.save(
                Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png"));
        ReservationTime time1 = reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("10:00")));
        ReservationTime time2 = reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("11:00")));

        // 아루의 확정 예약
        reservationRepository.save(
                Reservation.createNew("아루", LocalDate.parse("2026-08-06"), theme, time1));
        // 쿠다의 예약 슬롯에 다른이름(먼저), 아루(나중) 순으로 대기
        Reservation kudaReservation = reservationRepository.save(
                Reservation.createNew("쿠다", LocalDate.parse("2026-08-07"), theme, time2));
        waitingRepository.save(ReservationWaiting.createNew(
                kudaReservation, "다른이름", LocalDateTime.parse("2026-08-06T11:59")));
        waitingRepository.save(ReservationWaiting.createNew(
                kudaReservation, "아루", LocalDateTime.parse("2026-08-06T12:00")));

        List<MyReservationHistoryRow> histories = historyRepository.findByUserName("아루");

        assertThat(histories).hasSize(2);
        // 날짜 순 정렬: 먼저 8/6 예약, 다음 8/7 대기
        MyReservationHistoryRow reservationRow = histories.get(0);
        assertThat(reservationRow.status()).isEqualTo("RESERVATION");
        assertThat(reservationRow.name()).isEqualTo("아루");
        assertThat(reservationRow.sequence()).isZero();

        MyReservationHistoryRow waitingRow = histories.get(1);
        assertThat(waitingRow.status()).isEqualTo("WAITING");
        assertThat(waitingRow.name()).isEqualTo("아루");
        // 다른이름이 먼저 신청했으므로 아루는 2번째 순번
        assertThat(waitingRow.sequence()).isEqualTo(2);
    }

    private void clearTables() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }
}
