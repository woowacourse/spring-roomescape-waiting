package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.common.UserName;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.domain.reservation.Schedule;
import roomescape.domain.reservation.Slot;
import roomescape.domain.theme.Theme;
import roomescape.exception.ForbiddenException;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationWaitingTest {

    @Test
    @DisplayName("새로운 예약 대기를 성공적으로 생성한다.")
    void createWaitingTest() {
        // given
        UserName name = UserName.from("파도");
        LocalDateTime now = LocalDateTime.of(2026, 5, 29, 10, 0);
        Slot slot = createValidSlot(now.plusDays(1));

        // when
        ReservationWaiting waiting = ReservationWaiting.create(name, slot, now);

        // then
        assertThat(waiting.getId()).isNull();
        assertThat(waiting.getUserName()).isEqualTo(name);
        assertThat(waiting.getWaitingDate()).isEqualTo(now.plusDays(1).toLocalDate());
        assertThat(waiting.createAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("예약 대기 취소 시 슬롯의 시간이 현재 시간보다 미래라면 검증을 통과한다.")
    void validateCancelableSuccessTest() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 5, 29, 10, 0);
        Slot slot = createValidSlot(now.plusDays(1));
        ReservationWaiting waiting = ReservationWaiting.from(1L, UserName.from("파도"), slot, now);

        // when & then
        waiting.validateCancelable(now);
    }

    @Test
    @DisplayName("본인의 예약 대기가 맞는지 검증하고, 타인의 대기라면 예외를 발생시킨다.")
    void validateOwnedByTest() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 5, 29, 10, 0);
        ReservationWaiting waiting = ReservationWaiting.from(1L, UserName.from("파도"), createAnySlot(), now);

        // when & then
        assertThatThrownBy(() -> waiting.validateOwnedBy(UserName.from("다른사람")))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("타인의 예약대기는 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("동일한 ID를 가진 예약 대기는 같은 객체로 판단한다.")
    void equalsAndHashCodeTest() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 5, 29, 10, 0);
        Slot slot = createAnySlot();

        ReservationWaiting waiting1 = ReservationWaiting.from(1L, UserName.from("파도"), slot, now);
        ReservationWaiting waiting2 = ReservationWaiting.from(1L, UserName.from("다른사람"), slot, now.plusMinutes(1));

        // when & then
        assertThat(waiting1).isEqualTo(waiting2);
        assertThat(waiting1.hashCode()).isEqualTo(waiting2.hashCode());
    }


    private Slot createValidSlot(LocalDateTime dateTime) {
        ReservationTime time = ReservationTime.from(1L, LocalTime.from(dateTime));
        Theme theme = Theme.from(1L, "테마", "썸네일", "설명");

        return Slot.from(
                Schedule.from(dateTime.toLocalDate(), time),
                theme
        );
    }

    private Slot createAnySlot() {
        return createValidSlot(LocalDateTime.of(2026, 6, 5, 10, 0));
    }
}
