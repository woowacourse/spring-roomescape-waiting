package roomescape.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.utils.TestFixture;

class WaitingTest {
    private static final Member member = TestFixture.getAdmin();
    private static final Theme theme = TestFixture.getTheme("테마");
    private static final ReservationTime time = TestFixture.getReservationTimeAfterMinute(1);

    @Test
    @DisplayName("예약 대기 생성 시, 잘못된 예약 대기 날짜 형식이면 예외가 발생한다.")
    void validateDateFormat() {
        final String date = "2222222222";

        assertThatThrownBy(() -> new Waiting(member, date, time, theme))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Waiting.DATE_FORMAT_EXCEPTION_MESSAGE);
    }

    @Test
    @DisplayName("예약 대기의 주인과 멤버가 일치하는지 검증한다.")
    void validateMember() {
        final Member waitingOwner = TestFixture.getAdmin();
        final Member anotherMember = TestFixture.getMember();
        final Waiting waiting = new Waiting(waitingOwner, TestFixture.getTodayDate(), time, theme);

        assertThat(waiting.isNotOwner(anotherMember)).isTrue();
    }
}
