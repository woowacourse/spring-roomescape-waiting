package roomescape.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.ReservationTimeRepository;

/**
 * JdbcReservationTimeRepository 슬라이스 테스트 (@JdbcTest).
 *
 * <p>검증 대상: findAvailable의 NOT IN 서브쿼리.
 * "특정 날짜·테마에 이미 예약된 시간을 제외하고 남은 시간만" 반환하는 비단순 쿼리다.
 * 서브쿼리의 날짜·테마 조건이 정확한지는 SQL을 실제 실행해야만 잡힌다.
 *
 * <p>findAll/save/deleteById, UNIQUE(start_at) 제약 같은 건 서비스·인수에서 거쳐가므로
 * 여기서는 findAvailable의 필터링 경계에 집중한다.
 */
@JdbcTest
@Import(JdbcReservationTimeRepository.class)
class JdbcReservationTimeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    private static final LocalDate DATE = LocalDate.of(2050, 12, 31);

    private Long time10;
    private Long time11;
    private Long themeId;

    @BeforeEach
    void setUp() {
        time10 = insertTime(LocalTime.of(10, 0));
        time11 = insertTime(LocalTime.of(11, 0));
        themeId = insertTheme("테마A");
    }

    @Nested
    @DisplayName("findAvailable — 예약되지 않은 시간만")
    class Available {

        @Test
        @DisplayName("해당 날짜·테마에 예약된 시간은 제외된다")
        void 예약된_시간_제외() {
            insertReservation("브라운", DATE, time10, themeId);  // 10:00 예약됨

            List<ReservationTime> available = reservationTimeRepository.findAvailable(DATE, themeId);

            assertThat(available).extracting(ReservationTime::getStartAt)
                    .containsExactlyInAnyOrder(LocalTime.of(11, 0));  // 11:00만 남음
        }

        @Test
        @DisplayName("다른 날짜의 예약은 영향을 주지 않는다 (날짜 경계)")
        void 다른_날짜_무관() {
            insertReservation("브라운", DATE.minusDays(1), time10, themeId);  // 다른 날짜에 10:00 예약

            List<ReservationTime> available = reservationTimeRepository.findAvailable(DATE, themeId);

            // DATE 기준으로는 10:00도 11:00도 모두 가능
            assertThat(available).extracting(ReservationTime::getStartAt)
                    .containsExactlyInAnyOrder(LocalTime.of(10, 0), LocalTime.of(11, 0));
        }

        @Test
        @DisplayName("다른 테마의 예약은 영향을 주지 않는다 (테마 경계)")
        void 다른_테마_무관() {
            Long themeB = insertTheme("테마B");
            insertReservation("브라운", DATE, time10, themeB);  // 테마B의 10:00 예약

            // 테마A 기준으로는 10:00도 여전히 가능
            List<ReservationTime> availableA = reservationTimeRepository.findAvailable(DATE, themeId);

            assertThat(availableA).extracting(ReservationTime::getStartAt)
                    .containsExactlyInAnyOrder(LocalTime.of(10, 0), LocalTime.of(11, 0));
        }

        @Test
        @DisplayName("예약이 없으면 등록된 모든 시간이 가능하다")
        void 예약_없으면_전부() {
            List<ReservationTime> available = reservationTimeRepository.findAvailable(DATE, themeId);

            assertThat(available).hasSize(2);
        }
    }

    // --- given 헬퍼 ---

    private Long insertTime(LocalTime startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", startAt);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?", Long.class, startAt);
    }

    private Long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name, "설명", "url");
        return jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?", Long.class, name);
    }

    private void insertReservation(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date, timeId, themeId);
    }
}
