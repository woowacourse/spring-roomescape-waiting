package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWithWaitingOrder;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@JdbcTest
@Import(JdbcReservationRepository.class)
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("테마와 날짜 시간을 이용해 예약 시간을 찾는다.")
    @Test
    void findTimeIdsByThemeIdAndDate_테마와_날짜로_예약_시간_조회_테스트() {
        Long timeId1 = insertTime("2026-05-06 10:00:00", "2026-05-06 12:00:00");
        Long timeId2 = insertTime("2026-05-06 13:00:00", "2026-05-06 15:00:00");

        LocalDate date = LocalDate.of(2026, 5, 6);
        Long themeId = insertTheme("테마");
        insertReservation("어셔", timeId1, themeId, Status.RESERVED
                , LocalDateTime.now());
        insertReservation("라이", timeId2, themeId, Status.RESERVED
                , LocalDateTime.now());

        assertThat(reservationRepository.findTimeIdsByThemeIdAndDate(themeId, date))
                .containsExactly(timeId1, timeId2);
        assertThat(reservationRepository.findTimeIdsByThemeIdAndDate(themeId, date.plusDays(1)))
                .isEmpty();
    }

    @DisplayName("save로 예약을 저장한다.")
    @Test
    void save_예약_저장_테스트() {
        // given
        Long themeId = insertTheme("테마");
        Theme theme = new Theme("테마", "테마 설명입니다", "test-url").withId(themeId);
        Long timeId = insertTime(LocalDateTime.now().plusHours(1).toString(),
                LocalDateTime.now().plusHours(3).toString());
        ReservationTime time = new ReservationTime(timeId, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        Reservation reservation = new Reservation("라이", time, theme, Status.RESERVED, null, null, LocalDateTime.now());

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
    void findEarliestWaiting_예약_대기_id_반환_테스트() {
        // given
        Long themeId = insertTheme("테마");
        Long timeId = insertTime(LocalDateTime.now().plusHours(1).toString(),
                LocalDateTime.now().plusHours(3).toString());
        Long firstId = insertReservation("어셔1", timeId, themeId, Status.WAITING, LocalDateTime.now().plusHours(1));
        insertReservation("어셔2", timeId, themeId, Status.WAITING, LocalDateTime.now().plusHours(2));
        insertReservation("어셔3", timeId, themeId, Status.WAITING, LocalDateTime.now().plusHours(3));

        // when
        Optional<Long> id = reservationRepository.findEarliestWaiting(timeId, themeId);

        // then
        assertThat(id).isPresent();
        assertThat(id.get()).isEqualTo(firstId);
    }

    @DisplayName("예약 대기 상태를 RESERVED로 승격한다.")
    @Test
    void promoteToReserved_예약_대기_승격_테스트() {
        // given
        Long themeId = insertTheme("테마");
        Long timeId = insertTime(LocalDateTime.now().plusHours(1).toString(),
                LocalDateTime.now().plusHours(3).toString());
        Long waitingId = insertReservation("어셔1", timeId, themeId, Status.WAITING, LocalDateTime.now().plusHours(1));

        // when
        boolean affected = reservationRepository.promoteToReserved(waitingId);
        Optional<Reservation> reservation = reservationRepository.findById(waitingId);

        // then
        assertThat(affected).isTrue();
        assertThat(reservation).isPresent();
        assertThat(reservation.get().getStatus()).isEqualTo(Status.RESERVED);
    }

    @DisplayName("특정 날짜와 테마의 예약이 존재한다면 true를 반환한다")
    @Test
    void hasConfirmedReservation_예약_존재하면_true_반환() {
        // given
        Long themeId = insertTheme("테마");
        Long timeId = insertTime(LocalDateTime.now().plusHours(1).toString(),
                LocalDateTime.now().plusHours(3).toString());
        ReservationTime time = new ReservationTime(timeId, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        insertReservation("어셔1", timeId, themeId, Status.RESERVED, LocalDateTime.now().plusHours(1));

        // when
        boolean result = reservationRepository.hasConfirmedReservation(themeId, time);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("특정 날짜와 테마의 예약이 존재하지 않는다면 false를 반환한다")
    @Test
    void hasConfirmedReservation_예약_존재하지_않으면_false_반환() {
        // given
        Long themeId = insertTheme("테마");
        Long timeId = insertTime(LocalDateTime.now().plusHours(1).toString(),
                LocalDateTime.now().plusHours(3).toString());
        ReservationTime time = new ReservationTime(timeId, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));

        // when
        boolean result = reservationRepository.hasConfirmedReservation(themeId, time);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("특정 날짜와 테마의 같은 이름의 예약이 존재한다면 true를 반환한다")
    @Test
    void hasConfirmedReservationWithName_예약_존재하면_true_반환() {
        // given
        Long themeId = insertTheme("테마");
        Long timeId = insertTime(LocalDateTime.now().plusHours(1).toString(),
                LocalDateTime.now().plusHours(3).toString());
        ReservationTime time = new ReservationTime(timeId, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        insertReservation("어셔", timeId, themeId, Status.RESERVED, LocalDateTime.now().plusHours(1));

        // when
        boolean result = reservationRepository.isDuplicatedWithName("어셔", themeId, time);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("특정 날짜와 테마의 같은 이름의 예약이 존재하지 않으면 false를 반환한다")
    @Test
    void hasConfirmedReservationWithName_예약_존재하지_않으면_false_반환() {
        // given
        Long themeId = insertTheme("테마");
        Long timeId = insertTime(LocalDateTime.now().plusHours(1).toString(),
                LocalDateTime.now().plusHours(3).toString());
        ReservationTime time = new ReservationTime(timeId, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));

        // when
        boolean result = reservationRepository.isDuplicatedWithName("어셔", themeId, time);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("대기 순번을 포함한 예약 전체 정보를 조회한다. 확정 예약인 경우, waitingOrder는 null이 된다.")
    @Test
    void findAllByName_대기_순번_포함_예약_대기_정보_조회() {
        // given
        Long themeId = insertTheme("테마");
        Long timeId = insertTime(LocalDateTime.now().plusHours(1).toString(),
                LocalDateTime.now().plusHours(3).toString());
        Long timeId2 = insertTime(LocalDateTime.now().plusHours(3).toString(),
                LocalDateTime.now().plusHours(5).toString());
        insertReservation("어셔1", timeId, themeId, Status.RESERVED, LocalDateTime.now().plusHours(1));
        insertReservation("어셔2", timeId, themeId, Status.WAITING, LocalDateTime.now().plusHours(2));
        insertReservation("어셔3", timeId, themeId, Status.WAITING, LocalDateTime.now().plusHours(3));
        insertReservation("어셔3", timeId2, themeId, Status.RESERVED, LocalDateTime.now().plusHours(3));

        // when
        List<ReservationWithWaitingOrder> reservations = reservationRepository.findAllByName("어셔3");

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations.get(0).waitingOrder()).isEqualTo(2);
        assertThat(reservations.get(1).waitingOrder()).isNull();
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

    private Long insertReservation(String name, Long timeId, Long themeId, Status status, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, time_id, theme_id, status, created_at) VALUES (?, ?, ?, ?, ?)",
                name,
                timeId,
                themeId,
                status.name(),
                createdAt.toString()
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation WHERE name = ? AND time_id = ? AND theme_id = ? AND created_at = ?",
                Long.class,
                name, timeId, themeId, createdAt.toString()
        );
    }
}
