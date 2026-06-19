package roomescape.controller.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import roomescape.application.service.ReservationService;
import roomescape.application.service.command.ReservationChangeCommand;
import roomescape.application.service.command.ReservationCommand;
import roomescape.application.service.result.ReservationSlotResult;
import roomescape.common.Page;
import roomescape.common.Pageable;
import roomescape.controller.BaseControllerUnitTest;
import roomescape.controller.client.api.ReservationApiController;
import roomescape.controller.client.api.dto.condition.ReservationSearchCondition;
import roomescape.controller.client.api.dto.request.ReservationChangeRequest;
import roomescape.controller.client.api.dto.request.ReservationRequest;
import roomescape.controller.client.api.dto.response.ReservationDetailResponse;
import roomescape.controller.client.api.dto.response.ReservationResponse;
import roomescape.controller.client.api.dto.response.ReservationSearchResponse;
import roomescape.controller.client.api.dto.response.ReservationSlotResponse;
import roomescape.controller.client.api.dto.response.ReservationTimeResponse;
import roomescape.controller.client.api.dto.response.ThemeResponse;
import roomescape.controller.client.api.query.ReservationQuery;
import roomescape.controller.client.fixture.ReservationApiRequestFixture;
import roomescape.service.fixture.ReservationServiceFixture;

@WebMvcTest(ReservationApiController.class)
class ReservationApiControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private ReservationService reservationService;
    @MockitoBean
    private ReservationQuery reservationQuery;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @ParameterizedTest(name = "요청 정보가 {0} 일 때, 예외 메세지 \"{1}\"가 발생한다.")
    @MethodSource("roomescape.controller.client.fixture.ReservationApiRequestFixture#reserveFailRequestFixture")
    void 예약_요청_시_형식_검증에_실패하면_예외가_발생한다(ReservationRequest body, String exceptionMessage) {
        // given: 실패하는 request body가 주어짐
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/reservations")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString(exceptionMessage));
    }

    @Test
    void 예약_요청에_성공하면_201_Created_상태와_정상_응답이_반환된다() {
        // given
        ReservationRequest body = ReservationApiRequestFixture.reserveSuccessRequestFixture();
        ReservationSlotResult result = ReservationServiceFixture.createReservationSlotResult();
        when(reservationService.reserve(any(ReservationCommand.class))).thenReturn(result);

        // when & then
        ReservationSlotResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/reservations")
                .then().log().all()
                .status(HttpStatus.CREATED)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).isEqualTo(ReservationSlotResponse.from(result));
    }

    @Test
    void 사용자_이름으로_예약_정보를_조회할_수_있다() {
        // given
        ReservationSearchResponse searchResponse =
                new ReservationSearchResponse(1L, "이름", LocalDate.now(), LocalTime.now(), "테마명", "RESERVED", null);
        Page<ReservationSearchResponse> result = Page.of(10, 10, List.of(searchResponse));
        when(reservationQuery.search(any(ReservationSearchCondition.class), any(Pageable.class))).thenReturn(result);

        // when
        Page<ReservationSearchResponse> response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .params("name", "이프")
                .when().get("/api/reservations")
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });

        // then
        assertThat(response).isEqualTo(result);
    }

    @Test
    void 예약_식별자로_예약_정보를_조회할_수_있다() {
        // given
        ReservationDetailResponse result = new ReservationDetailResponse(
                1L,
                LocalDate.now(),
                new ThemeResponse(1L, "테마명", "설명", "https://image.com/theme.png"),
                new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                new ReservationResponse(1L, "이프", "RESERVED", LocalDate.now().atStartOfDay())
        );
        when(reservationQuery.findByReservationId(anyLong())).thenReturn(result);

        // when
        ReservationDetailResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().get("/api/reservations/{reservationId}", 1L)
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });

        // then
        assertThat(response).isEqualTo(result);
    }

    @Test
    void 사용자_이름_정보없이_예약_목록을_조회한다면_예외가_발생한다() {
        // when
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().get("/api/reservations")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("[name] 예약 정보 검색 시 사용자 명이 필요합니다."));
    }

    @Test
    void 사용자가_예약_취소에_성공하면_삭제_로직_수행_후_204_noContent를_반환한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().delete("/api/reservations/{reservationId}", 1L)
                .then().log().all()
                .status(HttpStatus.NO_CONTENT);
        verify(reservationService, times(1)).cancelReservation(anyLong());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void 사용자가_예약_취소시_식별자가_양수가_아니라면_400_Bad_Request를_반환한다(int invalidId) {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().delete("/api/reservations/{reservationId}", invalidId)
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("예약 식별자는 양수입니다."));
    }

    @Test
    void 대기_신청_요청에_성공하면_201_Created_상태와_WAITING_응답이_반환된다() {
        // given
        ReservationRequest body = ReservationApiRequestFixture.reserveSuccessRequestFixture();
        ReservationSlotResult result = ReservationServiceFixture.createWaitingResult();
        when(reservationService.addWaiting(any(ReservationCommand.class))).thenReturn(result);

        // when & then
        ReservationSlotResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/reservations/waiting")
                .then().log().all()
                .status(HttpStatus.CREATED)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).isEqualTo(ReservationSlotResponse.from(result));
    }

    @Test
    void 예약_변경_식별자와_변경할_시간으로_예약_변경_요청시_변경_로직_실행_후_200OK() {
        // given
        ReservationChangeRequest request = new ReservationChangeRequest(LocalDate.now(), 1L);
        ReservationSlotResult result = ReservationServiceFixture.createReservationSlotResult();
        when(reservationService.change(anyLong(), any(ReservationChangeCommand.class))).thenReturn(result);

        // when
        ReservationSlotResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(request)
                .when().patch("/api/reservations/{reservationId}", 1L)
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });

        // then
        assertThat(response).isEqualTo(ReservationSlotResponse.from(result));
    }
}
