package roomescape.feature.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.fixture.TimeFixture;
import roomescape.global.error.exception.GeneralException;

class ReservationTest {

    private static final ReserverName DEFAULT_RESERVER_NAME = new ReserverName("예약자");
    private static final Time DEFAULT_TIME = TimeFixture.VALID_10_00.createInstance();
    private static final Theme DEFAULT_THEME = ThemeFixture.VALID.createInstance();

    @Nested
    class 생성한다 {

        @Test
        void 미래_일정으로_생성하면_ACTIVE_상태로_생성된다() {
            LocalDate futureDate = LocalDate.now().plusYears(1);

            Reservation reservation = Reservation.create(DEFAULT_RESERVER_NAME, futureDate, DEFAULT_TIME, DEFAULT_THEME);

            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        }

        @Test
        void 과거_일정으로_생성하면_예외를_던진다() {
            LocalDate pastDate = LocalDate.now().minusYears(1);

            assertThatThrownBy(() -> Reservation.create(DEFAULT_RESERVER_NAME, pastDate, DEFAULT_TIME, DEFAULT_THEME))
                .isInstanceOf(GeneralException.class)
                .hasMessage("지난 예약은 생성할 수 없습니다");
        }
    }

    @Test
    void 취소하면_CANCELED_상태의_예약이_반환된다() {
        Reservation reservation = ReservationFixture.FUTURE.createInstance(DEFAULT_TIME, DEFAULT_THEME);

        assertThat(reservation.cancel().getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    void 대기로_전환하면_WAITING_상태의_예약이_반환된다() {
        Reservation reservation = ReservationFixture.FUTURE.createInstance(DEFAULT_TIME, DEFAULT_THEME);

        assertThat(reservation.toWaiting().getStatus()).isEqualTo(ReservationStatus.WAITING);
    }
}
