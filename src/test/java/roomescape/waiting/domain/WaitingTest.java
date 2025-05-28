package roomescape.waiting.domain;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class WaitingTest {

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
    void 예약을_대기한다() {
        Member waiter = createMember();
        ReservationDateTime reservationDateTime = createReservationDateTime();
        Theme theme = createTheme();
        LocalDateTime waitedAt = LocalDateTime.now();

        Waiting waiting = Waiting.wait(
                waiter, reservationDateTime, theme, waitedAt
        );

        assertSoftly(softly -> {
            softly.assertThat(waiting.getWaiter()).isEqualTo(waiter);
            softly.assertThat(waiting.getReservationDatetime()).isEqualTo(reservationDateTime);
            softly.assertThat(waiting.getTheme()).isEqualTo(theme);
            softly.assertThat(waiting.getWaitedAt()).isEqualTo(waitedAt);
        });
    }
}
