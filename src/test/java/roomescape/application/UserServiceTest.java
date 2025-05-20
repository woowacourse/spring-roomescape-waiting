package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.DateUtils.tomorrow;
import static roomescape.DomainFixtures.JUNK_THEME;
import static roomescape.DomainFixtures.JUNK_TIME_SLOT;
import static roomescape.DomainFixtures.JUNK_USER;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.timeslot.TimeSlotRepository;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceTest {

    @Autowired
    private UserService service;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private TimeSlotRepository timeSlotRepository;
    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("사용자를 추가할 수 있다.")
    void registerUser() {
        // given
        var user = JUNK_USER;

        // when
        var created = service.register(user.email(), user.password(), user.name());

        // then
        var users = service.findAllUsers();
        assertThat(users).contains(created);
    }

    @Test
    @DisplayName("사용자의 예약을 조회할 수 있다.")
    void getReservations() {
        // given
        var savedTimeSlot = timeSlotRepository.save(JUNK_TIME_SLOT);
        var savedTheme = themeRepository.save(JUNK_THEME);
        var createdUser = service.register(JUNK_USER.email(), JUNK_USER.password(), JUNK_USER.name());
        var savedReservation = reservationRepository.save(
                Reservation.reserveNewly(createdUser, tomorrow(), savedTimeSlot, savedTheme));

        // when
        var reservations = service.getReservations(createdUser.id());

        // then
        assertThat(reservations).contains(savedReservation);
    }
}
