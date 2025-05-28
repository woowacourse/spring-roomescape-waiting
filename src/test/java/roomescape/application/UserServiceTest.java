package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.DateUtils.tomorrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.domain.RoomescapeSchedule;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWithOrder;
import roomescape.domain.user.User;

@Import(UserService.class)
class UserServiceTest extends ServiceTest {

    @Autowired
    private UserService service;

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
    void getMyReservations() {
        // given
        var user = service.register("popo@email.com", "pw", "popo");
        var reservation = reserveWithUser(user);

        // when
        var reservations = service.getMyReservations(user.id());

        // then
        assertThat(reservations).contains(new ReservationWithOrder(reservation, 1));
    }

    private Reservation reserveWithUser(final User user) {
        var savedTimeSlot = repositoryHelper.saveAnyTimeSlot();
        var savedTheme = repositoryHelper.saveAnyTheme();
        var reservation = new Reservation(user, RoomescapeSchedule.forReserve(tomorrow(), savedTimeSlot, savedTheme));
        return repositoryHelper.saveReservation(reservation);
    }
}
