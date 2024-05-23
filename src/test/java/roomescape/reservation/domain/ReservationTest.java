package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.util.Fixture.HORROR_DESCRIPTION;
import static roomescape.util.Fixture.HORROR_THEME_NAME;
import static roomescape.util.Fixture.KAKI_EMAIL;
import static roomescape.util.Fixture.KAKI_NAME;
import static roomescape.util.Fixture.KAKI_PASSWORD;
import static roomescape.util.Fixture.THUMBNAIL;

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
                        Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD),
                        LocalDate.now().minusDays(1),
                        theme,
                        reservationTime,
                        ReservationStatus.SUCCESS
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
