package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.ReservationErrorCode;
import roomescape.exception.ReservationSlotErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.exception.WaitingErrorCode;

class WaitingTest {

    @Test
    void 이름이_비어있으면_예외가_발생한다() {
        // given
        ReservationTime time = ReservationTime.create(LocalTime.parse("10:00"));
        Theme theme = Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png");

        // when & then
        ReservationSlot slot = ReservationSlot.of(LocalDate.parse("2026-08-05"), time, theme);

        assertThatThrownBy(() -> Waiting.create("", slot, 1L))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationErrorCode.INVALID_NAME);
    }

    @Test
    void 날짜가_null이면_예외가_발생한다() {
        // given
        ReservationTime time = ReservationTime.create(LocalTime.parse("10:00"));
        Theme theme = Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png");

        // when & then
        assertThatThrownBy(() -> Waiting.create("네오", ReservationSlot.of(null, time, theme), 1L))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationErrorCode.INVALID_DATE);
    }

    @Test
    void 시간이_null이면_예외가_발생한다() {
        // given
        Theme theme = Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png");

        // when & then
        assertThatThrownBy(
                () -> Waiting.create("네오",
                        ReservationSlot.of(LocalDate.parse("2026-08-05"), null, theme), 1L))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationErrorCode.INVALID_TIME);
    }

    @Test
    void 테마가_null이면_예외가_발생한다() {
        // given
        ReservationTime time = ReservationTime.create(LocalTime.parse("10:00"));

        // when & then
        assertThatThrownBy(
                () -> Waiting.create("네오",
                        ReservationSlot.of(LocalDate.parse("2026-08-05"), time, null), 1L))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationErrorCode.INVALID_THEME);
    }

    @Test
    void 대기번호가_null이면_예외가_발생한다() {
        // given
        ReservationTime time = ReservationTime.create(LocalTime.parse("10:00"));
        Theme theme = Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png");

        // when & then
        assertThatThrownBy(
                () -> Waiting.create("네오",
                        ReservationSlot.of(LocalDate.parse("2026-08-05"), time, theme), null))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(WaitingErrorCode.INVALID_WAITING_NUMBER);
    }

    @Test
    void 과거_대기시간이면_예외가_발생한다() {
        // given
        ReservationTime time = ReservationTime.create(LocalTime.parse("10:00"));
        Theme theme = Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png");
        Waiting waiting = Waiting.create(
                "브라운",
                ReservationSlot.of(LocalDate.parse("2026-05-06"), time, theme),
                1L
        );

        // when & then
        assertThatThrownBy(() -> waiting.validateNotPastTime(
                LocalDateTime.parse("2026-05-07T00:00:00")
        ))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationSlotErrorCode.SLOT_PAST_TIME);
    }

}
