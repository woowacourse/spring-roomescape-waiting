package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findTimeIdsByThemeIdAndDate() {
        Long timeId1 = insertTime("2026-05-06 10:00:00", "2026-05-06 12:00:00");
        Long timeId2 = insertTime("2026-05-06 13:00:00", "2026-05-06 15:00:00");

        LocalDate date = LocalDate.of(2026, 5, 6);
        Long themeId = insertTheme("테마");
        insertReservation("윤호준", timeId1, themeId, Status.RESERVED
                , LocalDateTime.now());
        insertReservation("박다혜", timeId2, themeId, Status.RESERVED
                , LocalDateTime.now());

        assertThat(reservationRepository.findTimeIdsByThemeIdAndDate(themeId, date))
                .containsExactly(timeId1, timeId2);

        assertThat(reservationRepository.findTimeIdsByThemeIdAndDate(themeId, date.plusDays(1)))
                .isEmpty();
    }

    @Test
    void findTimeIdsByThemeIdAndDate_다른테마의_예약시간은_조회하지_않는다() {
        Long timeId1 = insertTime("2026-05-06 10:00:00", "2026-05-06 12:00:00");
        Long timeId2 = insertTime("2026-05-06 13:00:00", "2026-05-06 15:00:00");

        LocalDate date = LocalDate.of(2026, 5, 6);
        Long themeId = insertTheme("테마1");
        Long otherThemeId = insertTheme("테마2");
        insertReservation("윤호준", timeId1, themeId, Status.RESERVED, LocalDateTime.now());
        insertReservation("박다혜", timeId2, otherThemeId, Status.RESERVED, LocalDateTime.now());

        assertThat(reservationRepository.findTimeIdsByThemeIdAndDate(themeId, date))
                .containsExactly(timeId1);
    }

    @Test
    @DisplayName("save로 예약을 저장한다.")
    void 예약_저장_테스트() {
        // given
        Long themeId = insertTheme("테마");
        Theme theme = new Theme("테마", "테마 설명입니다", "test-url").withId(themeId);
        Long timeId = insertTime(LocalDateTime.now().plusHours(1).toString(),
                LocalDateTime.now().plusHours(3).toString());
        ReservationTime time = new ReservationTime(timeId, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        Reservation reservation = new Reservation("라이", time, theme, Status.RESERVED, LocalDateTime.now());

        // when
        Reservation saved = reservationRepository.save(reservation);

        // then
        assertThat(saved.getTheme().getName()).isEqualTo(theme.getName());
        assertThat(saved.getTime().getStartAt()).isEqualTo(time.getStartAt());
        assertThat(saved.getTime().getEndAt()).isEqualTo(time.getEndAt());
        assertThat(saved.getStatus()).isEqualTo(Status.RESERVED);
    }

    @DisplayName("가장 오래된 WAITING 대기의 id 값을 가져온다.")
    @Test
    void 예약_대기_id_반환_테스트() {
        // given
        Long themeId = insertTheme("테마");
        Long timeId = insertTime(LocalDateTime.now().plusHours(1).toString(),
                LocalDateTime.now().plusHours(3).toString());
        insertReservation("어셔1", timeId, themeId, Status.WAITING, LocalDateTime.now().plusHours(1));
        insertReservation("어셔2", timeId, themeId, Status.WAITING, LocalDateTime.now().plusHours(2));
        insertReservation("어셔3", timeId, themeId, Status.WAITING,  LocalDateTime.now().plusHours(3));

        // when
        Optional<Long> id = reservationRepository.findEarliestWaiting(timeId, themeId);

        // then
        assertThat(id).isPresent();
        assertThat(id.get()).isEqualTo(1L);
    }

    @DisplayName("예약 대기 상태를 RESERVED로 승격한다.")
    @Test
    void 예약_대기_승격_테스트() {
        // given
        Long themeId = insertTheme("테마");
        Long timeId = insertTime(LocalDateTime.now().plusHours(1).toString(),
                LocalDateTime.now().plusHours(3).toString());
        insertReservation("어셔1", timeId, themeId, Status.WAITING, LocalDateTime.now().plusHours(1));

        // when
        boolean affected = reservationRepository.promoteToReserved(1L);
        Optional<Reservation> reservation = reservationRepository.findById(1L);

        // then
        assertThat(affected).isTrue();
        assertThat(reservation).isPresent();
        assertThat(reservation.get().getStatus()).isEqualTo(Status.RESERVED);
    }

    private Long insertTime(String startAt, String endAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_time, end_time) VALUES (?, ?)",
                startAt,
                endAt
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_time = ?",
                Long.class,
                startAt
        );
    }

    private Long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url) VALUES (?, ?, ?)",
                name,
                "설명",
                "https://example.com/theme.png"
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?",
                Long.class,
                name
        );
    }

    private void insertReservation(String name, Long timeId, Long themeId, Status status, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, time_id, theme_id, status, created_at) VALUES (?, ?, ?, ?, ?)",
                name,
                timeId,
                themeId,
                status.name(),
                createdAt.toString()
        );
    }
}
