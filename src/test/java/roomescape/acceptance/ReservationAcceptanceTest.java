package roomescape.acceptance;

import static org.hamcrest.Matchers.is;
import static roomescape.support.ReservationApiSteps.내_예약목록_조회;
import static roomescape.support.ReservationApiSteps.예약_변경_요청;
import static roomescape.support.ReservationApiSteps.예약_생성_요청;
import static roomescape.support.ReservationApiSteps.예약_취소_요청;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import roomescape.support.AcceptanceTest;
import roomescape.support.FixedClockConfig;

/**
 * 예약 인수 테스트 (RestAssured E2E).
 *
 * <p>핵심 시나리오 하나를 끝까지 태운다: 예약 → 내 목록 조회 → 변경 → 취소.
 * 이건 사용자가 실제로 밟는 경로이고, 여러 API가 한 흐름으로 이어질 때 깨지지 않는지를 본다.
 *
 * <p>에러 응답은 "사용자가 받는 구체 메시지가 올바른가"를 대표 케이스로 본다.
 * 인수 테스트의 시선은 사용자라서, 추상적인 형식("message 키가 있는가")보다 사용자가 실제로 읽는 문장이 도달하는지가 더 중요하다. 다만 모든 에러 케이스를 여기서 반복하지는 않는다 — 케이스별 정확성은
 * 서비스 통합 테스트와 컨트롤러 슬라이스가 책임지고, 여기서는 대표 케이스만 본다. (응답의 단일 형식 {"message":...} 자체는 전역 @RestControllerAdvice가 만드는 계약이라,
 * 슬라이스/Advice 테스트에서 한 번 검증하는 것으로 충분하다.)
 *
 * <p>HTTP 호출은 ReservationApiSteps에 가둔다 — 테스트 본문은 시나리오로 읽고, 요청 형식 변경은 한 곳에 모은다.
 *
 * <p>시간 결정성: @Import(FixedClockConfig)로 고정 시계를 주입해 과거/미래 판정을 안정화한다.
 */
@Import(FixedClockConfig.class)
class ReservationAcceptanceTest extends AcceptanceTest {

    private static final LocalDate FUTURE = FixedClockConfig.TODAY.plusDays(10);
    private static final LocalDate FUTURE_2 = FixedClockConfig.TODAY.plusDays(20);

    private Long timeId10;
    private Long timeId11;
    private Long themeId;

    @BeforeEach
    void setUpSlot() {
        timeId10 = fixture.insertTime(LocalTime.of(10, 0));
        timeId11 = fixture.insertTime(LocalTime.of(11, 0));
        themeId = fixture.insertTheme("테마A");
    }

    @Test
    @DisplayName("사용자는 예약하고, 내 목록에서 보고, 변경하고, 취소할 수 있다 (전체 흐름)")
    void 예약_조회_변경_취소_흐름() {
        // 1) 예약한다
        long reservationId = 예약_생성_요청("브라운", FUTURE, timeId10, themeId)
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // 2) 내 목록에 예약으로 보인다
        내_예약목록_조회("브라운")
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("RESERVED"))
                .body("[0].time.startAt", is("10:00"));

        // 3) 날짜·시간을 변경한다
        예약_변경_요청(reservationId, "브라운", FUTURE_2, timeId11)
                .statusCode(200)
                .body("date", is(FUTURE_2.toString()))
                .body("time.startAt", is("11:00"));

        // 4) 변경이 조회에 반영된다
        내_예약목록_조회("브라운")
                .statusCode(200)
                .body("[0].date", is(FUTURE_2.toString()))
                .body("[0].time.startAt", is("11:00"));

        // 5) 취소하면 목록이 빈다
        예약_취소_요청(reservationId, "브라운")
                .statusCode(204);

        내_예약목록_조회("브라운")
                .statusCode(200)
                .body("size()", is(0));
    }


    @Nested
    @DisplayName("에러가 사용자에게 올바른 메시지로 도달한다 (사용자 관점)")
    class ErrorResponseToUser {

        @Test
        @DisplayName("이미 예약된 슬롯에 예약하면 400과 안내 메시지가 사용자에게 도달한다")
        void 중복_예약_메시지_도달() {
            // 같은 슬롯에 이미 예약 1건 존재 → 중복 유발 (date가 충돌 키라 명시)
            fixture.reservation(timeId10, themeId).date(FUTURE).insert();

            예약_생성_요청("모카", FUTURE, timeId10, themeId)
                    .statusCode(400)
                    .body("message", is("해당 시간은 이미 예약되었습니다. 다른 시간을 선택해 주세요."));
        }

        @Test
        @DisplayName("존재하지 않는 예약을 취소하면 404와 안내 메시지가 도달한다")
        void 리소스_부재_메시지_도달() {
            예약_취소_요청(9999L, "브라운")
                    .statusCode(404)
                    .body("message", is("존재하지 않는 예약입니다."));
        }
    }
}
