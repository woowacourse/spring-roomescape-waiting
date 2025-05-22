package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.DateUtils.tomorrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.TestRepositoryHelper;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDateTime;
import roomescape.domain.reservation.ReservationSlot;

@DataJpaTest
@Import({UserService.class, TestRepositoryHelper.class})
class UserServiceTest {

    @Autowired
    private UserService service;
    @Autowired
    private TestRepositoryHelper repositoryHelper;

    @Test
    @DisplayName("사용자를 추가한다.")
    void registerUser() {
        // given
        var email = "user@email.com";
        var password = "password";
        var name = "user";

        // when
        var registeredUser = service.register(email, password, name);

        // then
        var users = service.findAllUsers();
        assertThat(users).contains(registeredUser);
    }

    @Test
    @DisplayName("사용자의 예약을 조회한다.")
    void getReservations() {
        // given
        var savedTimeSlot = repositoryHelper.saveAnyTimeSlot();
        var savedTheme = repositoryHelper.saveAnyTheme();
        var user = service.register("popo@email.com", "pw", "popo");

        var reservation = new Reservation(user, new ReservationSlot(ReservationDateTime.of(tomorrow(), savedTimeSlot), savedTheme));
        var savedReservation = repositoryHelper.saveReservation(reservation);
        repositoryHelper.flushAndClear();

        // when
        var reservations = service.getReservations(user.id());

        // then
        assertThat(reservations).contains(savedReservation);
    }
}
