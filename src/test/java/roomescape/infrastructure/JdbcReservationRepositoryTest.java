package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixtures.NORMAL_MEMBER_1;
import static roomescape.TestFixtures.NORMAL_MEMBER_2;
import static roomescape.TestFixtures.RESERVATION_TIME_1;
import static roomescape.TestFixtures.THEME_1;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.domain.reservation.Reservation;
import roomescape.infrastructure.persistance.JdbcReservationRepository;

class JdbcReservationRepositoryTest extends JdbcSupportTest {

    private final JdbcReservationRepository reservationRepository = new JdbcReservationRepository(
            new NamedParameterJdbcTemplate(TEST_DATASOURCE),
            TEST_DATASOURCE);

    @Test
    void 전체_예약을_조회할_수_있다() {
        //given
        insertMember(NORMAL_MEMBER_1);
        insertMember(NORMAL_MEMBER_2);
        insertTheme(THEME_1);
        insertReservationTime(RESERVATION_TIME_1);
        insertReservation(1L, LocalDate.of(2025, 4, 21), 1L, 1L);
        insertReservation(2L, LocalDate.of(2025, 4, 22), 1L, 1L);

        //when
        List<Reservation> reservations = reservationRepository.findAll();

        //then
        assertThat(reservations).isEqualTo(List.of(
                new Reservation(1L, NORMAL_MEMBER_1,
                        LocalDate.of(2025, 4, 21),
                        RESERVATION_TIME_1,
                        THEME_1),
                new Reservation(2L, NORMAL_MEMBER_2,
                        LocalDate.of(2025, 4, 22),
                        RESERVATION_TIME_1,
                        THEME_1)
        ));
    }

    @Test
    void id값으로_예약을_찾을_수_있다() {
        //given
        insertMember(NORMAL_MEMBER_1);
        insertTheme(THEME_1);
        insertReservationTime(RESERVATION_TIME_1);
        insertReservation(1L, LocalDate.of(2025, 4, 21), 1L, 1L);

        //when
        Optional<Reservation> reservation = reservationRepository.findById(1L);

        //then
        assertThat(reservation).hasValue(
                new Reservation(
                        1L,
                        NORMAL_MEMBER_1,
                        LocalDate.of(2025, 4, 21),
                        RESERVATION_TIME_1,
                        THEME_1));
    }

    @Test
    void id값에_해당하는_예약이_없다면_Optional_empty를_반환한다() {
        //when
        Optional<Reservation> reservation = reservationRepository.findById(1L);

        //then
        assertThat(reservation).isEmpty();
    }

    @Test
    void 예약을_생성할_수_있다() {
        //given
        insertMember(NORMAL_MEMBER_1);
        insertTheme(THEME_1);
        insertReservationTime(RESERVATION_TIME_1);
        Reservation reservation = new Reservation(
                NORMAL_MEMBER_1,
                LocalDate.of(2025, 4, 21),
                RESERVATION_TIME_1, THEME_1);

        //when
        Long createdId = reservationRepository.create(reservation);

        //then
        assertThat(reservationRepository.findById(createdId))
                .hasValue(new Reservation(
                        1L,
                        NORMAL_MEMBER_1,
                        LocalDate.of(2025, 4, 21),
                        RESERVATION_TIME_1,
                        THEME_1));
    }

    @Test
    void 예약을_삭제할_수_있다() {
        //given
        insertMember();
        insertTheme();
        insertReservationTime();
        insertReservation(1L, LocalDate.of(2025, 4, 21), 1L, 1L);

        //when
        reservationRepository.deleteById(1L);

        //then
        assertThat(reservationRepository.findById(1L)).isEmpty();
    }

    @Test
    void 특정_time_id를_사용하는_예약이_존재하는지_알_수_있다() {
        //given
        insertMember();
        insertTheme();
        insertReservationTime();
        insertReservation(1L, LocalDate.of(2025, 4, 21), 1L, 1L);

        //when
        boolean result = reservationRepository.existByTimeId(1L);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void 특정_날짜와_time_id를_사용하는_예약이_존재하는지_알_수_있다() {
        //given
        insertMember();
        insertTheme();
        insertReservationTime();
        insertReservation(1L, LocalDate.of(2025, 4, 21), 1L, 1L);

        //when
        boolean result = reservationRepository.existByDateAndTimeId(LocalDate.of(2025, 4, 21), 1L);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void theme_id를_사용하는_예약이_존재하는지_알_수_있다() {
        //given
        insertMember();
        insertTheme();
        insertReservationTime();
        insertReservation(1L, LocalDate.of(2025, 4, 21), 1L, 1L);

        //when
        boolean result = reservationRepository.existByThemeId(1L);

        //then
        assertThat(result).isTrue();
    }
}
