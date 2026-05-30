package roomescape.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWithWaitingOrder;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.JdbcTimeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcReservationRepository reservationRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private JdbcThemeRepository themeRepository;
    @Autowired
    private JdbcTimeRepository timeRepository;

    @DisplayName("테마와 날짜 시간을 이용해 예약 시간을 찾는다.")
    @Test
    void findTimeIdsByThemeIdAndDate_테마와_날짜로_예약_시간_조회_테스트() {
        // given
        ReservationTime time1 = insertTime(
                LocalDateTime.of(2026, 6, 1, 10, 0),
                LocalDateTime.of(2026, 6, 1, 12, 0));
        ReservationTime time2 = insertTime(
                LocalDateTime.of(2026, 6, 1, 13, 0),
                LocalDateTime.of(2026, 6, 1, 15, 0));
        Theme theme = insertTheme("테마");
        Reservation reservation1 = insertReservation("어셔", time1, theme, Status.RESERVED, LocalDateTime.now());
        Reservation reservation2 = insertReservation("라이", time2, theme, Status.WAITING, LocalDateTime.now());
        LocalDate date = LocalDate.of(2026, 6, 1);

        // when
        assertThat(reservationRepository.findTimeIdsByThemeIdAndDate(theme.getId(), date))
                .containsExactly(time1.getId(), time2.getId());
        assertThat(reservationRepository.findTimeIdsByThemeIdAndDate(theme.getId(), date.plusDays(1)))
                .isEmpty();
    }


    @DisplayName("save로 예약을 저장한다.")
    @Test
    void save_예약_저장_테스트() {
        // given
        Theme theme = insertTheme("테마");
        ReservationTime time = insertTime(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)
        );
        Reservation reservation = new Reservation("어셔", time, theme, Status.RESERVED, LocalDateTime.now());

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
        Theme theme = insertTheme("테마");
        ReservationTime time = insertTime(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        Reservation existing = insertReservation("어셔1", time, theme, Status.WAITING, LocalDateTime.now().plusHours(1));
        insertReservation("어셔2", time, theme, Status.WAITING, LocalDateTime.now().plusHours(2));
        insertReservation("어셔3", time, theme, Status.WAITING, LocalDateTime.now().plusHours(3));

        // when
        Optional<Long> id = reservationRepository.findEarliestWaiting(time.getId(), theme.getId());

        // then
        assertThat(id).isPresent();
        assertThat(id.get()).isEqualTo(existing.getId());
    }

    @DisplayName("예약 대기 상태를 RESERVED로 승격한다.")
    @Test
    void promoteToReserved_예약_대기_승격_테스트() {
        // given
        Theme theme = insertTheme("테마");
        ReservationTime time = insertTime(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        Reservation waiting = insertReservation("어셔1", time, theme, Status.WAITING, LocalDateTime.now().plusHours(1));

        // when
        boolean affected = reservationRepository.promoteToReserved(waiting.getId());
        Optional<Reservation> reservation = reservationRepository.findById(waiting.getId());

        // then
        assertThat(affected).isTrue();
        assertThat(reservation).isPresent();
        assertThat(reservation.get().getStatus()).isEqualTo(Status.RESERVED);
    }

    @DisplayName("특정 날짜와 테마의 예약이 존재한다면 true를 반환한다")
    @Test
    void isDuplicated_예약_존재하면_true_반환() {
        // given
        Theme theme = insertTheme("테마");
        ReservationTime time = insertTime(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        insertReservation("어셔1", time, theme, Status.RESERVED, LocalDateTime.now().plusHours(1));

        // when
        boolean result = reservationRepository.isDuplicated(theme.getId(), time);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("특정 날짜와 테마의 예약이 존재하지 않는다면 false를 반환한다")
    @Test
    void isDuplicated_예약_존재하지_않으면_false_반환() {
        // given
        Theme theme = insertTheme("테마");
        ReservationTime time = insertTime(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));

        // when
        boolean result = reservationRepository.isDuplicated(theme.getId(), time);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("특정 날짜와 테마의 같은 이름의 예약이 존재한다면 true를 반환한다")
    @Test
    void isDuplicatedWithName_예약_존재하면_true_반환() {
        // given
        Theme theme = insertTheme("테마");
        ReservationTime time = insertTime(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        insertReservation("어셔", time, theme, Status.RESERVED, LocalDateTime.now().plusHours(1));

        // when
        boolean result = reservationRepository.isDuplicatedWithName("어셔", theme.getId(), time);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("특정 날짜와 테마의 같은 이름의 예약이 존재하지 않으면 false를 반환한다")
    @Test
    void isDuplicatedWithName_예약_존재하지_않으면_false_반환() {
        // given
        Theme theme = insertTheme("테마");
        ReservationTime time = insertTime(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));

        // when
        boolean result = reservationRepository.isDuplicatedWithName("어셔", theme.getId(), time);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("대기 순번을 포함한 예약 전체 정보를 조회한다.")
    @Test
    void findAllByName_대기_순번_포함_예약_대기_정보_조회() {
        // given
        Theme theme = insertTheme("테마");
        ReservationTime time1 = insertTime(
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        ReservationTime time2 = insertTime(
                LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(5));
        insertReservation("어셔1", time1, theme, Status.RESERVED, LocalDateTime.now().plusHours(1));
        insertReservation("어셔2", time1, theme, Status.WAITING, LocalDateTime.now().plusHours(2));
        insertReservation("어셔3", time1, theme, Status.WAITING, LocalDateTime.now().plusHours(3));
        insertReservation("어셔3", time2, theme, Status.RESERVED, LocalDateTime.now().plusHours(3));

        // when
        List<ReservationWithWaitingOrder> reservations = reservationRepository.findAllByName("어셔3");

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations.get(0).waitingOrder()).isEqualTo(2);
        assertThat(reservations.get(1).waitingOrder()).isEqualTo(0);
    }

    private Reservation insertReservation(String name, ReservationTime time, Theme theme, Status status, LocalDateTime createdAt) {
        Reservation reservation = new Reservation(name, time, theme, status, createdAt);
        return reservationRepository.save(reservation);
    }

    private Theme insertTheme(String name) {
        return themeRepository.save(
                new Theme(name, "설명", "iamgeUrl")
        );
    }

    private ReservationTime insertTime(LocalDateTime start, LocalDateTime end) {
        return timeRepository.save(start, end);
    }
}
