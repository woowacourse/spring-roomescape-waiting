package roomescape.domain.waitingreservation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.MemberErrorCode;
import roomescape.support.exception.RoomescapeException;

class WaitingReservationTest {

    @Test
    void 예약_대기가_정상적으로_생성된다() {
        Member member = Member.of(1L, "고래");
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2026, 5, 27));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        Theme theme = Theme.createWithoutId("공포", "테마 내용", "themes/theme");
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 26, 11, 0);

        assertThatCode(() -> WaitingReservation.createWithoutId(member, date, time, theme, createdAt))
            .doesNotThrowAnyException();
    }

    @Test
    void 멤버가_null이면_예외가_발생한다() {
        Member member = null;
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2026, 5, 27));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        Theme theme = Theme.createWithoutId("공포", "테마 내용", "themes/theme");
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 26, 11, 0);

        assertThatThrownBy(() -> WaitingReservation.createWithoutId(member, date, time, theme, createdAt))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage(MemberErrorCode.INVALID_MEMBER.getMessage());
    }

    @Test
    void 생성_시간이_null이면_예외가_발생한다() {
        Member member = Member.of(1L, "고래");
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2026, 5, 27));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        Theme theme = Theme.createWithoutId("공포", "테마 내용", "themes/theme");
        LocalDateTime createdAt = null;

        assertThatThrownBy(() -> WaitingReservation.createWithoutId(member, date, time, theme, createdAt))
            .isInstanceOf(RoomescapeException.class)
            .hasMessageContaining("생성 시간이 유효하지 않습니다.");
    }
}
