package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.Fixture.HORROR_DESCRIPTION;
import static roomescape.Fixture.HORROR_THEME;
import static roomescape.Fixture.HORROR_THEME_NAME;
import static roomescape.Fixture.KAKI_EMAIL;
import static roomescape.Fixture.KAKI_NAME;
import static roomescape.Fixture.KAKI_PASSWORD;
import static roomescape.Fixture.MEMBER_JOJO;
import static roomescape.Fixture.RESERVATION_TIME_10_00;
import static roomescape.Fixture.THUMBNAIL;
import static roomescape.Fixture.TOMORROW;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;

class ReservationTest {

    @DisplayName("현재 날짜보다 이전 날짜로 예약시 예외가 발생한다.")
    @Test
    void createReservationByLastDate() {
        Theme theme = new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL);
        ReservationTime reservationTime = new ReservationTime(LocalTime.now());

        assertThatThrownBy(() -> new Reservation(
                        new Member(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD),
                        LocalDate.now().minusDays(1),
                        theme,
                        reservationTime,
                        Status.SUCCESS
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 상태를 성공으로 변경한다.")
    @Test
    void updateSuccessStatusWithWaitingReservation() {
        Reservation waitingReservation = new Reservation(
                MEMBER_JOJO,
                TOMORROW,
                HORROR_THEME,
                RESERVATION_TIME_10_00,
                Status.WAIT
        );

        waitingReservation.updateSuccessStatus();

        assertThat(waitingReservation.getStatus()).isEqualTo(Status.SUCCESS);

    }

    @DisplayName("예약 성공으로 변경 시, 이미 성공 상태이면 예외가 발생한다.")
    @Test
    void updateSuccessStatusWithReservation() {
        Reservation reservation = new Reservation(
                MEMBER_JOJO,
                TOMORROW,
                HORROR_THEME,
                RESERVATION_TIME_10_00,
                Status.SUCCESS
        );

        assertThatThrownBy(reservation::updateSuccessStatus)
                .isInstanceOf(IllegalArgumentException.class);
    }
}
