package roomescape.business.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.exception.business.InvalidCreateArgumentException;

class ReservationTest {

    private static final LocalDate DATE = LocalDate.now().plusDays(5);
    private static final ReservationTime RESERVATION_TIME = ReservationTime.create(LocalTime.of(10, 0));
    private static final Theme THEME = Theme.create("공포", "", "");
    private static final String NAME = "dompoo";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password1234!";

    @Nested
    class 생성_테스트 {

        @Test
        void 정상적인_예약을_생성할_수_있다() {
            // given
            final User user = User.create(NAME, EMAIL, PASSWORD);

            // when
            final Reservation reservation = Reservation.create(user, DATE, RESERVATION_TIME, THEME);

            // then
            assertThat(reservation).isNotNull();
            assertThat(reservation.getUser().getName().value()).isEqualTo(NAME);
            assertThat(reservation.getDate().value()).isEqualTo(DATE);
            assertThat(reservation.getTime()).isEqualTo(RESERVATION_TIME);
            assertThat(reservation.getTheme()).isEqualTo(THEME);
        }

        @Test
        void 과거_날짜로_예약할_수_없다() {
            final LocalDate pastDate = LocalDate.now().minusDays(1);
            final User user = User.create(NAME, EMAIL, PASSWORD);

            assertThatThrownBy(() -> Reservation.create(user, pastDate, RESERVATION_TIME, THEME))
                    .isInstanceOf(InvalidCreateArgumentException.class);
        }

        @Test
        void 현재보다_일주일_이후로_예약할_수_없다() {
            final LocalDate over7DaysDate = LocalDate.now().plusDays(8);
            final User user = User.create(NAME, EMAIL, PASSWORD);

            assertThatThrownBy(() -> Reservation.create(user, over7DaysDate, RESERVATION_TIME, THEME))
                    .isInstanceOf(InvalidCreateArgumentException.class);
        }
    }
}
