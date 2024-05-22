package roomescape.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.acceptance.config.AcceptanceTest;
import roomescape.controller.api.dto.response.MemberReservationResponse;
import roomescape.controller.api.dto.response.MemberReservationsResponse;
import roomescape.controller.api.dto.response.ReservationResponse;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static roomescape.acceptance.step.MemberStep.본인_예약_조회;
import static roomescape.acceptance.step.MemberStep.이메일로_멤버_생성후_로그인;
import static roomescape.acceptance.step.ReservationStep.예약_생성;
import static roomescape.acceptance.step.ReservationStep.예약_취소;
import static roomescape.acceptance.step.WaitingStep.대기_생성;

@AcceptanceTest
class WaitingAcceptanceTest {
    @Test
    @DisplayName("예약자가 취소하면 첫 번째 예약 대기자가 자동으로 예약이 된다.")
    void flow1() {

        // A가 예약을 한다.
        final String firstUserToken = 이메일로_멤버_생성후_로그인("alphaka@gmail.com");
        final ReservationResponse response = 예약_생성("2024-10-01", "공포", "12:00", firstUserToken);

        // B가 대기를 한다.
        final String secondUserToken = 이메일로_멤버_생성후_로그인("joyson5582@gmail.com");
        대기_생성("2024-10-01", response.theme().id(),response.time().id(), secondUserToken);

        // A가 취소를 한다.
        예약_취소(firstUserToken, response.id());

        // B의 상태가 예약 상태가 된다.
        final MemberReservationsResponse result = 본인_예약_조회(secondUserToken);

        final List<Integer> orderList = result.data()
                .stream()
                .map(MemberReservationResponse::order)
                .toList();

        assertThat(orderList).containsExactly(0);
    }

    @Test
    @DisplayName("운영자는 예약 대기를 취소할 수 있다.")
    void flow2() {
        // A가 예약을 한다.

        // B가 대기를 한다.

        // C가 대기를 한다.

        // 운영자가 B의 대기를 취소한다.

        // A가 취소를 한다.

        // C의 상태가 예약 상태가 된다.
    }

    @Test
    @DisplayName("첫 번째 예약대기를 취소하면, 두 번째 예약대기자가 첫 번째 예약 대기자가 된다.")
    void flow3() {
        // A가 예약을 한다.

        // B가 예약을 실패한다.

        // B가 대기를 한다.

        // C가 대기를 한다.

        // B가 대기를 취소한다.

        // A가 취소를 한다.

        // C의 상태가 예약 상태가 된다.
    }
}
