package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;
import roomescape.reservation.service.dto.ReservationWaitWithRankResponse;

@DataJpaTest
class JpaReservationWaitRepositoryTest {

    @Autowired
    private JpaReservationWaitRepository reservationWaitRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        cleanupTestData();
        insertTestData();
    }

    private void cleanupTestData() {
        jdbcTemplate.update("DELETE FROM reservation_wait");
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("DELETE FROM reservation_time");
    }

    private void insertTestData() {
        jdbcTemplate.update(
                "INSERT INTO member (id, name, email, role) VALUES (?, ?, ?, ?)",
                1L, "member1", "11@gmail.com", "USER"
        );
        jdbcTemplate.update(
                "INSERT INTO member (id, name, email, role) VALUES (?, ?, ?, ?)",
                2L, "member2", "22@gmail.com", "USER"
        );

        jdbcTemplate.update(
                "INSERT INTO theme (id, name, description, thumbnail) VALUES (?, ?, ?, ?)",
                1L, "테마1", "테마1 설명", "www.theme1.com"
        );

        jdbcTemplate.update(
                "INSERT INTO reservation_time (id, start_at) VALUES (?, ?)",
                1L, LocalTime.of(14, 30)
        );

        jdbcTemplate.update("""
                        INSERT INTO reservation_wait (id, reservation_date, member_id, theme_id, time_id) 
                        VALUES (?, ?, ?, ?, ?)
                        """,
                1L, LocalDate.of(2025, 8, 10), 1L, 1L, 1L
        );
        jdbcTemplate.update("""
                        INSERT INTO reservation_wait (id, reservation_date, member_id, theme_id, time_id) 
                        VALUES (?, ?, ?, ?, ?)
                        """,
                2L, LocalDate.of(2025, 8, 10), 2L, 1L, 1L
        );
    }

    @DisplayName("회원 ID로 예약 대기 목록을 조회하면 대기 순위와 함께 반환된다")
    @Test
    void findWithRankByInfoMemberId() {
        // when
        final List<ReservationWaitWithRankResponse> result = reservationWaitRepository.findWithRankByInfoMemberId(2L);

        // then
        assertAll(
                () -> assertThat(result).isNotEmpty(),
                () -> assertThat(result.get(0).reservationWait().getId()).isEqualTo(2L),
                () -> assertThat(result.get(0).rank()).isEqualTo(2)
        );
    }

    @DisplayName("날짜, 시간, 테마로 특정 순서의 예약 대기를 조회한다")
    @Test
    void findByParamsAt() {
        // given
        final ReservationDate date = ReservationDate.from(LocalDate.of(2025, 8, 10));
        final int index = 0;

        // when
        final Optional<ReservationWait> result = reservationWaitRepository.findByParamsAt(date, 1L, 1L, index);

        // then
        assertThat(result).isPresent();
        ReservationWait reservationWait = result.get();
        assertAll(
                () -> assertThat(reservationWait.getId()).isEqualTo(1L),
                () -> assertThat(reservationWait.getMember().getId()).isEqualTo(1L),
                () -> assertThat(reservationWait.getTime().getId()).isEqualTo(1L),
                () -> assertThat(reservationWait.getTheme().getId()).isEqualTo(1L)
        );
    }

    @DisplayName("존재하지 않는 순서의 예약 대기를 조회하면 빈 Optional을 반환한다")
    @Test
    void findByParamsAtNotFound() {
        // given
        ReservationDate date = ReservationDate.from(LocalDate.of(2025, 8, 10));

        // when
        Optional<ReservationWait> result = reservationWaitRepository.findByParamsAt(date, 1L, 1L, 3);

        // then
        assertThat(result).isEmpty();
    }
}
