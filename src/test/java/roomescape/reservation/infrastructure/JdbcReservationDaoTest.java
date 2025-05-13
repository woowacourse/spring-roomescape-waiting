package roomescape.reservation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.business.domain.Member;
import roomescape.member.business.domain.Role;
import roomescape.reservation.business.domain.Reservation;
import roomescape.reservation.business.domain.ReservationTime;
import roomescape.reservation.business.repository.ReservationDao;
import roomescape.theme.business.domain.Theme;

@JdbcTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JdbcReservationDaoTest {

    private ReservationDao reservationDao;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        reservationDao = new JdbcReservationDao(jdbcTemplate);
    }

    @Test
    void 모든_예약을_조회한다() {
        final List<Reservation> allReservations = reservationDao.findAll();

        assertThat(allReservations).hasSize(6);
    }

    @Test
    void 예약을_추가한다() {
        final Reservation reservation = new Reservation(
                LocalDate.now().plusDays(1),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "", "", ""),
                new Member(1L, "엠제이", "", "", Role.MEMBER)
        );

        final Reservation saved = reservationDao.save(reservation);

        final Reservation expected = new Reservation(7L, LocalDate.now().plusDays(1),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "", "", ""),
                new Member(1L, "엠제이", "", "", Role.MEMBER)
        );
        assertThat(saved).isEqualTo(expected);
    }

    @Test
    void 예약을_삭제한다() {
        reservationDao.deleteById(1L);

        final List<Reservation> all = reservationDao.findAll();
        assertThat(all).hasSize(5);
    }

    @Test
    void id로_예약을_조회한다() {
        final Optional<Reservation> findReservation = reservationDao.findById(1L);

        assertThat(findReservation.get())
                .isEqualTo(new Reservation(1L, LocalDate.now().plusDays(1),
                                new ReservationTime(1L, LocalTime.of(10, 0)),
                                new Theme(1L, "", "", ""),
                                new Member(1L, "엠제이", "", "", Role.MEMBER)
                        )
                );
    }

    @Test
    void 같은_time_id를_가진_예약이_존재하는지_확인한다() {
        assertAll(
                () -> assertThat(reservationDao.existByTimeId(1L)).isTrue(),
                () -> assertThat(reservationDao.existByTimeId(10L)).isFalse()
        );
    }

    @Test
    void date와_theme_id가_일치하는_예약을_조회한다() {
        final List<Reservation> findReservation = reservationDao.findByDateAndThemeId(LocalDate.now().minusDays(4), 2L);

        final List<Reservation> expected = List.of(
                new Reservation(
                        2L,
                        LocalDate.now().minusDays(4),
                        new ReservationTime(2L, LocalTime.of(12, 0)),
                        new Theme(2L, "", "", ""),
                        new Member(1L, "엠제이", "", "", Role.MEMBER)
                )
        );
        assertThat(findReservation).isEqualTo(expected);
    }

    @Test
    void theme_id와_member_id로_기간에_해당하는_예약을_조회한다() {
        // given
        final List<Reservation> findReservations = reservationDao.findReservationByThemeIdAndMemberIdInDuration(2, 1,
                LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));

        // when & then
        assertThat(findReservations).hasSize(2);
    }
}
