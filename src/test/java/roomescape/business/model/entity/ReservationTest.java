package roomescape.business.model.entity;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.business.InvalidCreateArgumentException;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

class ReservationTest {

    private static final LocalDate DATE = LocalDate.now().plusDays(5);
    private static final ReservationTime RESERVATION_TIME = new ReservationTime(LocalTime.of(10, 0));
    private static final Theme THEME = new Theme("공포", "", "");
    private static final String NAME = "dompoo";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password1234!";

    @Nested
    class 생성_테스트 {

        @Test
        void 정상적인_예약을_생성할_수_있다() {
            // given
            final User user = new User(NAME, EMAIL, PASSWORD);
            final ReservationSlot slot = new ReservationSlot(RESERVATION_TIME, DATE, THEME);

            // when
            final Reservation reservation = new Reservation(user, slot);

            // then
            assertThat(reservation).isNotNull();
            assertThat(reservation.getUser()).isEqualTo(user);
            assertThat(reservation.getSlot()).isEqualTo(slot);
        }

        @Test
        void 과거_날짜로_예약할_수_없다() {
            final User user = new User(NAME, EMAIL, PASSWORD);
            final ReservationSlot slot = new ReservationSlot(RESERVATION_TIME, LocalDate.now().minusDays(1), THEME);

            assertThatThrownBy(() -> new Reservation(user, slot))
                    .isInstanceOf(InvalidCreateArgumentException.class);
        }

        @Test
        void 현재보다_일주일_이후로_예약할_수_없다() {
            final User user = new User(NAME, EMAIL, PASSWORD);
            final ReservationSlot slot = new ReservationSlot(RESERVATION_TIME, LocalDate.now().plusDays(8), THEME);

            assertThatThrownBy(() -> new Reservation(user, slot))
                    .isInstanceOf(InvalidCreateArgumentException.class);
        }
    }
}
