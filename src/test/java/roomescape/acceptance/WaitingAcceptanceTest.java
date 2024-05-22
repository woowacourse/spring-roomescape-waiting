package roomescape.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.acceptance.config.AcceptanceTest;

@AcceptanceTest
class WaitingAcceptanceTest {
    @Test
    @DisplayName("예약자가 취소하면 첫 번째 예약 대기자가 자동으로 예약이 된다.")
    void flow1(){
        // A가 예약을 한다.

        // B가 예약을 실패한다.

        // B가 대기를 한다.

        // A가 취소를 한다.

        // B의 상태가 예약 상태가 된다.
    }
    @Test
    @DisplayName("운영자는 예약 대기를 취소할 수 있다.")
    void flow2(){
        // A가 예약을 한다.

        // B가 예약을 실패한다.

        // B가 대기를 한다.

        // C가 대기를 한다.

        // 운영자가 B의 대기를 취소한다.

        // A가 취소를 한다.

        // C의 상태가 예약 상태가 된다.
    }
    @Test
    @DisplayName("첫 번째 예약대기를 취소하면, 두 번째 예약대기자가 첫 번째 예약 대기자가 된다.")
    void flow3(){
        // A가 예약을 한다.

        // B가 예약을 실패한다.

        // B가 대기를 한다.

        // C가 대기를 한다.

        // B가 대기를 취소한다.

        // A가 취소를 한다.

        // C의 상태가 예약 상태가 된다.
    }
}
