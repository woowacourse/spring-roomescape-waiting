package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationStatusTest {
    @DisplayName("예약 상태의 메시지를 반환할 경우 '예약' 문자만 반환한다.")
    @Test
    void given_statusWithReserved_when_getMessageWithRank_then_returnStringWithoutRankNumber() {
        //given
        Integer rank = 1000;
        //when,then
        assertThat(ReservationStatus.RESERVED.getMessageWithRank(rank)).doesNotContain(rank.toString());
    }

    @DisplayName("대기 상태의 메시지를 반환할 경우 대기 순번과 함께 반환한다.")
    @Test
    void given_statusWithWaiting_when_getMessageWithRank_then_returnStringWithRankNumber() {
        //given
        Integer rank = 1000;
        //when,then
        assertThat(ReservationStatus.WAITING.getMessageWithRank(rank)).contains(rank.toString());
    }
}
