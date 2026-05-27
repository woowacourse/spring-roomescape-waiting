package roomescape.reservationwaiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.reservationwaiting.dto.ReservationWaitingResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationWaitingServiceTest {

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    private ReservationWaitingRequest reservationWaitingRequest;
    private ReservationWaitingResponse response;

    @BeforeEach
    void setUp() {
        reservationWaitingRequest = new ReservationWaitingRequest("현미밥", 12L);
        response = reservationWaitingService.createWaiting(reservationWaitingRequest);
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 중복 대기할 수 없다.")
    void 예약_대기_생성_성공() {
        assertThat(response.id()).isNotNull();
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 중복 대기할 수 없다.")
    void 예약_대기_생성_실패() {
        ReservationWaitingRequest reservationWaitingRequest2 = new ReservationWaitingRequest("현미밥", 12L);

        assertThatThrownBy(() -> reservationWaitingService.createWaiting(
                reservationWaitingRequest2))
                .isInstanceOf(BusinessException.class)
                .satisfies(
                        e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_WAITING))
                .hasMessage(ErrorCode.DUPLICATE_WAITING.getMessage());
    }

    @Test
    @DisplayName("예약 대기를 삭제할 수 있다.")
    void 예약_대기_삭제_성공() {
        reservationWaitingService.deleteWaiting(response.id());
        assertThat(reservationWaitingService.getWaitingByName("현미밥").size()).isEqualTo(0);
    }

    @Test
    @DisplayName("지난 예약 대기는 삭제할 수 없다.")
    void 예약_대기_삭제_실패() {
        assertThatThrownBy(() -> reservationWaitingService.deleteWaiting(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(
                        e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                                ErrorCode.PAST_WAITING_CANCEL))
                .hasMessage(ErrorCode.PAST_WAITING_CANCEL.getMessage());
    }

    @Test
    @DisplayName("사용자의 이름으로 대기 현황을 조회한다.")
    void 예약_대기_조회() {
        assertThat(reservationWaitingService.getWaitingByName("현미밥").size()).isEqualTo(1);
    }
}
