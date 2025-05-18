package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservationtime.ReservationTimeTestDataConfig;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.ThemeTestDataConfig;
import roomescape.theme.domain.Theme;
import roomescape.user.MemberTestDataConfig;
import roomescape.user.domain.User;

@DataJpaTest
@Import({ThemeTestDataConfig.class,
        MemberTestDataConfig.class,
        ReservationTimeTestDataConfig.class
})
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeTestDataConfig themeTestDataConfig;
    @Autowired
    private MemberTestDataConfig memberTestDataConfig;
    @Autowired
    private ReservationTimeTestDataConfig timeTestDataConfig;

    private Reservation createReservation(int plusDays, ReservationTime time) {
        LocalDate date = LocalDate.now().plusDays(plusDays);
        return ReservationFixture.createByBookedStatus(date, time, themeTestDataConfig.getSavedTheme(),
                memberTestDataConfig.getSavedUser());
    }

    @DisplayName("유저의 모든 예약 정보들을 조회할 수 있다.")
    @Test
    void findByUser_success_byMember() {
        // given
        User savedMember = memberTestDataConfig.getSavedUser();
        Theme savedTheme = themeTestDataConfig.getSavedTheme();
        ReservationTime savedTime = timeTestDataConfig.getSavedReservationTime();
        List<Reservation> reservations = List.of(ReservationFixture.createByBookedStatus(
                        LocalDate.now().plusDays(1), savedTime, savedTheme, savedMember),
                ReservationFixture.createByBookedStatus(
                        LocalDate.now().plusDays(2), savedTime, savedTheme, savedMember));
        List<Reservation> expectedReservations = reservationRepository.saveAll(reservations);

        // when
        List<Reservation> actualReservations = reservationRepository.findByUser(savedMember);

        // then
        assertThat(actualReservations).containsExactlyInAnyOrderElementsOf(expectedReservations);
    }


    @DisplayName("예약 시간에 해당하는 예약의 존재 여부를 알 수 있다.")
    @Test
    void existsByReservationTime() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));

        Reservation reservation = createReservation(1, reservationTime);
        reservationRepository.save(reservation);

        // when
        boolean actual = reservationRepository.existsByReservationTime(reservationTime);

        // then
        assertThat(actual).isTrue();
    }
}
