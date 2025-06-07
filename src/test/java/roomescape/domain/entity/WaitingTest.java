package roomescape.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.ReservationStatus;
import roomescape.testFixture.Fixture;

class WaitingTest {

    @Test
    @DisplayName("예약대기는 RESERVED 상태로 생성될 수 없다")
    void validateNotReserved() {
        // given
        Member member = Fixture.MEMBER1_ADMIN;
        GameSchedule gameSchedule = Fixture.GAME_SCHEDULE_1;

        // when & then
        assertThatThrownBy(() -> Waiting.withId(1L, member, gameSchedule, ReservationStatus.RESERVED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("예약대기는 예약 상태일 수 없습니다.");
    }

    @Test
    @DisplayName("예약대기는 WAITING 상태로 생성될 수 있다")
    void canCreateWithWaitingStatus() {
        // given
        Member member = Fixture.MEMBER1_ADMIN;
        GameSchedule gameSchedule = Fixture.GAME_SCHEDULE_1;

        // when
        Waiting waiting = Waiting.withoutId(member, gameSchedule);

        // then
        assertThat(waiting.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }
} 
