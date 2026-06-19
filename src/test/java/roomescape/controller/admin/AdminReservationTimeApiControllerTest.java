package roomescape.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
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
import roomescape.application.service.ReservationTimeService;
import roomescape.application.service.command.ReservationTimeCommand;
import roomescape.application.service.result.ReservationTimeResult;
import roomescape.controller.BaseControllerUnitTest;
import roomescape.controller.admin.api.AdminReservationTimeApiController;
import roomescape.controller.admin.api.dto.response.AdminReservationTimeResponse;
import roomescape.controller.admin.api.query.AdminReservationTimeQuery;
import roomescape.controller.admin.fixture.AdminReservationTimeApiRequestFixture;

@WebMvcTest(AdminReservationTimeApiController.class)
class AdminReservationTimeApiControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @MockitoBean
    private AdminReservationTimeQuery reservationTimeQuery;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @ParameterizedTest(name = "요청 정보가 {0} 일 때, 예외 메세지 \"{1}\"가 발생한다.")
    @MethodSource("roomescape.controller.admin.fixture.AdminReservationTimeApiRequestFixture#registerFailRequestFixture")
    void 시간_등록_요청_시_형식_검증에_실패하면_예외가_발생한다(ReservationTimeCommand body, String exceptionMessage) {
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/admin/times")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString(exceptionMessage));
    }

    @Test
    void 시간_등록에_성공하면_201_Created_상태와_정상_응답이_반환된다() {
        // given
        ReservationTimeCommand body = AdminReservationTimeApiRequestFixture.registerSuccessRequestFixture();
        ReservationTimeResult result = new ReservationTimeResult(1L, body.startAt(), "ACTIVE");
        when(reservationTimeService.register(any(ReservationTimeCommand.class))).thenReturn(result);

        // when & then
        AdminReservationTimeResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/admin/times")
                .then().log().all()
                .status(HttpStatus.CREATED)
                .extract().as(new TypeRef<>() {
                });
        assertThat(response).isEqualTo(AdminReservationTimeResponse.from(result));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void 시간_비활성화를_요청하는_식별자가_양수가_아니라면_예외가_발생한다(int timeId) {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().patch("/api/admin/times/{id}/deactivate", timeId)
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("예약 시간 비활성화 식별자는 양수여야 합니다."));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void 시간_활성화를_요청하는_식별자가_양수가_아니라면_예외가_발생한다(int timeId) {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().patch("/api/admin/times/{id}/activate", timeId)
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("예약 시간 활성화 식별자는 양수여야 합니다."));
    }

    @Test
    void 정상적인_ID로_시간_비활성화_요청시_204_응답을_한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().patch("/api/admin/times/{id}/deactivate", 1L)
                .then().log().all()
                .status(HttpStatus.NO_CONTENT);
        verify(reservationTimeService, times(1)).deactivate(anyLong());
    }

    @Test
    void 정상적인_ID로_시간_활성화_요청시_204_응답을_한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().patch("/api/admin/times/{id}/activate", 1L)
                .then().log().all()
                .status(HttpStatus.NO_CONTENT);
        verify(reservationTimeService, times(1)).activate(anyLong());
    }

    @Test
    void 전체_시간_조회_요청시_200OK와_시간_정보들을_응답한다() {
        // given
        List<AdminReservationTimeResponse> result = List.of(new AdminReservationTimeResponse(1L, LocalTime.of(10, 0), "ACTIVE"));
        when(reservationTimeQuery.getAllReservationTimes()).thenReturn(result);

        // when & then
        List<AdminReservationTimeResponse> response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().get("/api/admin/times")
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });
        assertThat(response).isEqualTo(result);
    }
}
