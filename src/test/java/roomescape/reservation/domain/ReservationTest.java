package roomescape.reservation.domain;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class ReservationTest {

    private static Member createMember() {
        return Member.signUpUser(
                "user1",
                Email.create("user1@email.com"),
                Password.encrypt("1234", rawPassword -> rawPassword)
        );
    }

    private static ReservationDate createReservationDate() {
        return new ReservationDate(LocalDate.now().plusDays(1));
    }

    private static ReservationTime createReservationTime() {
        return ReservationTime.open(LocalTime.of(10, 0));
    }

    private static ReservationDateTime createReservationDateTime() {
        return ReservationDateTime.create(
                createReservationDate(),
                createReservationTime()
        );
    }

    private static Theme createTheme() {
        return Theme.create("공포", "공포 테마 설명", "공포 테마 이미지 URL");
    }

    @Test
    void 예약을_생성한다() {
        Member reserver = createMember();
        ReservationDateTime reservationDateTime = createReservationDateTime();
        Theme theme = createTheme();
        LocalDateTime reservedAt = LocalDateTime.now();

        Reservation reservation = Reservation.reserve(
                reserver, reservationDateTime, theme, reservedAt
        );

        assertSoftly(softly -> {
            softly.assertThat(reservation.getReserver()).isEqualTo(reserver);
            softly.assertThat(reservation.getReservationDatetime()).isEqualTo(reservationDateTime);
            softly.assertThat(reservation.getTheme()).isEqualTo(theme);
            softly.assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
            softly.assertThat(reservation.getReservedAt()).isEqualTo(reservedAt);
        });
    }
}
