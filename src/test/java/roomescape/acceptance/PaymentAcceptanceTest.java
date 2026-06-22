package roomescape.acceptance;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static roomescape.support.PaymentApiSteps.결제_승인_콜백_요청;
import static roomescape.support.ReservationApiSteps.내_예약목록_조회;
import static roomescape.support.ReservationApiSteps.예약_생성_요청;

import io.restassured.path.json.JsonPath;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.domain.payment.PaymentGateway;
import roomescape.exception.server.PaymentTimeoutException;
import roomescape.support.AcceptanceTest;

/**
 * 결제 안정화 인수 테스트 (RestAssured E2E).
 *
 * <p>대표 시나리오 하나: 예약(결제 대기) → 승인 중 게이트웨이 read timeout → 예약은 사라지지 않고
 * 내 예약 목록에 '확인 필요(IN_DOUBT)'로 남는다. 사용자 시선에서 "타임아웃이 곧 실패·삭제가 아니다"를 끝까지 확인한다. (예외→동작 라우팅은 PaymentController 슬라이스가,
 * markInDoubt 가드는 PaymentServiceTest가 따로 책임진다.)
 */
class PaymentAcceptanceTest extends AcceptanceTest {

    private static final LocalDate FUTURE = LocalDate.of(2050, 12, 31);

    private Long timeId;
    private Long themeId;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @BeforeEach
    void setUpSlot() {
        timeId = fixture.insertTime(LocalTime.of(10, 0));
        themeId = fixture.insertTheme("테마A");
    }

    @Test
    void read_timeout이면_예약은_남고_내역에_확인필요로_표시된다() {
        // 예약 생성 → 결제 대기 주문(orderId) 확보
        JsonPath created = 예약_생성_요청("브라운", FUTURE, timeId, themeId)
                .statusCode(201).extract().jsonPath();
        String orderId = created.getString("orderId");
        long amount = created.getLong("amount");

        // 승인 콜백 도중 게이트웨이가 read timeout
        given(paymentGateway.confirm(any()))
                .willThrow(new PaymentTimeoutException("결제 결과를 확인하지 못했습니다."));
        결제_승인_콜백_요청("test_pk_1", orderId, amount).statusCode(302);

        // 내 예약: 사라지지 않고 '확인 필요'로 보인다
        내_예약목록_조회("브라운")
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("RESERVED"))
                .body("[0].paymentStatus", is("IN_DOUBT"));
    }
}
