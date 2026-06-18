package roomescape.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
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
import roomescape.application.service.command.ReservationCommand;
import roomescape.application.service.result.ReservationSlotResult;
import roomescape.controller.BaseControllerUnitTest;
import roomescape.controller.admin.api.AdminReservationApiController;
import roomescape.controller.admin.api.dto.request.AdminReservationRequest;
import roomescape.controller.admin.api.dto.response.AdminReservationSlotResponse;
import roomescape.controller.admin.api.query.AdminReservationQuery;
import roomescape.controller.admin.fixture.AdminReservationApiRequestFixture;
import roomescape.service.fixture.ReservationServiceFixture;

@WebMvcTest(AdminReservationApiController.class)
class AdminReservationApiControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private ReservationService reservationService;
    @MockitoBean
    private AdminReservationQuery reservationQuery;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @ParameterizedTest(name = "요청 정보가 {0} 일 때, 예외 메세지 \"{1}\"가 발생한다.")
    @MethodSource("roomescape.controller.admin.fixture.AdminReservationApiRequestFixture#reserveFailRequestFixture")
    void 예약_요청_시_형식_검증에_실패하면_예외가_발생한다(AdminReservationRequest body, String exceptionMessage) {
        // given: 실패하는 request body가 주어짐
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/admin/reservations")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString(exceptionMessage));
    }

    @Test
    void 예약_요청에_성공하면_201_Created_상태와_정상_응답이_반환된다() {
        // given
        AdminReservationRequest body = AdminReservationApiRequestFixture.reserveSuccessRequestFixture();
        ReservationSlotResult result = ReservationServiceFixture.createReservationSlotResult();
        when(reservationService.reserve(any(ReservationCommand.class))).thenReturn(result);

        // when & then
        AdminReservationSlotResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/admin/reservations")
                .then().log().all()
                .status(HttpStatus.CREATED)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).isEqualTo(AdminReservationSlotResponse.from(result));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void 예약_취소를_요청하는_예약_엔트리_Id가_양수가_아니라면_예외가_발생한다(int reservationReservationId) {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().delete("/api/admin/reservations/" + reservationReservationId)
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("예약 식별자는 양수여야 합니다."));
    }

    @Test
    void 정상적인_예약_엔트리_ID로_예약_취소_요청시_204_응답을_한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().delete("/api/admin/reservations/1")
                .then().log().all()
                .status(HttpStatus.NO_CONTENT);
        verify(reservationService, times(1)).cancelReservation(anyLong());
    }

    @Test
    void 전체_예약_정보_조회_요청시_200OK와_예약_정보들을_응답한다() {
        // given
        List<AdminReservationSlotResponse> result = List.of();
        when(reservationQuery.getAllReservations()).thenReturn(result);
        
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().get("/api/admin/reservations")
                .then().log().all()
                .status(HttpStatus.OK)
                .body("size()", is(0));
    }
}
