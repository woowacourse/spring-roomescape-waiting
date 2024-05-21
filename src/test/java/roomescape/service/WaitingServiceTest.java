package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.waiting.WaitingRequest;

@Sql("/waiting-service-test-data.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    MemberService memberService;

    @Test
    void 잘못된_사용자_id로_대기를_추가할_시_예외_발생() {
        //given
        List<MemberResponse> allMembers = memberService.getAllMembers();
        Long notExistMemberId = allMembers.size() + 1L;
        WaitingRequest waitingRequest = new WaitingRequest(notExistMemberId, 1L);

        //when, then
        assertThatThrownBy(() -> waitingService.addWaiting(waitingRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 잘못된_예약_id로_대기를_추가할_시_예외_발생() {
        //given
        List<ReservationResponse> allReservations = reservationService.getAllReservations();
        Long notExistReservationId = allReservations.size() + 1L;
        WaitingRequest waitingRequest = new WaitingRequest(1L, notExistReservationId);

        //when, then
        assertThatThrownBy(() -> waitingService.addWaiting(waitingRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미_주어진_사용자_id로_예약_되어_있는_경우_예외_발생() {
        WaitingRequest waitingRequest = new WaitingRequest(1L, 2L);

        //when, then
        assertThatThrownBy(() -> waitingService.addWaiting(waitingRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 지나간_예약에_대한_대기_생성_시_예외_발생() {
        WaitingRequest waitingRequest = new WaitingRequest(2L, 1L);

        //when, then
        assertThatThrownBy(() -> waitingService.addWaiting(waitingRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
