package roomescape.domain.reservation;

import org.junit.jupiter.api.Test;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.member.Member;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ReservationTest {

    private Slot validSlot() {
        ReservationTime time = ReservationTime.load(1L, LocalTime.of(10, 0));
        Theme theme = Theme.load(1L, "공포의 방", "설명", "https://zeze.com/thumb.jpg");
        return Slot.load(1L, LocalDate.of(2099, 1, 1), time, theme);
    }

    @Test
    void 회원이_NULL이면_예외가_발생한다() {
        assertThatThrownBy(() -> Reservation.create(null, validSlot()))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 정상적인_예약_생성은_성공한다() {
        Member member = new Member(1L, "zeze");
        Reservation reservation = Reservation.create(member, validSlot()).withStatus(Status.APPROVED);

        assertThat(reservation.getMember().getName()).isEqualTo("zeze");
        assertThat(reservation.getStatus()).isEqualTo(Status.APPROVED);
    }
}
