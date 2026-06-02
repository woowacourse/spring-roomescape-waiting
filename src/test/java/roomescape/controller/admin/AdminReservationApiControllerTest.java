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
import roomescape.controller.BaseControllerUnitTest;
import roomescape.controller.admin.dto.request.AdminReservationRequest;
import roomescape.controller.admin.dto.response.AdminReservationResponse;
import roomescape.controller.admin.fixture.AdminReservationApiRequestFixture;
import roomescape.exception.DuplicateEntityException;
import roomescape.exception.RoomEscapeException;
import roomescape.service.ReservationService;
import roomescape.service.command.ReservationCommand;
import roomescape.service.fixture.ReservationServiceFixture;
import roomescape.service.result.ReservationResult;

@WebMvcTest(AdminReservationApiController.class)
class AdminReservationApiControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private ReservationService reservationService;

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
        ReservationResult result = ReservationServiceFixture.createReservationResult();
        when(reservationService.reserve(any(ReservationCommand.class))).thenReturn(result);

        // when & then
        AdminReservationResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/admin/reservations")
                .then().log().all()
                .status(HttpStatus.CREATED)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).isEqualTo(AdminReservationResponse.from(result));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void 예약_취소를_요청하는_예약_엔트리_Id가_양수가_아니라면_예외가_발생한다(int reservationEntryId) {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().delete("/api/admin/reservations/entries/" + reservationEntryId)
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("예약 엔트리 식별자는 양수여야 합니다."));
    }

    @Test
    void 정상적인_예약_엔트리_ID로_예약_취소_요청시_204_응답을_한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().delete("/api/admin/reservations/entries/1")
                .then().log().all()
                .status(HttpStatus.NO_CONTENT);
        verify(reservationService, times(1)).cancelReservation(anyLong());
    }

    @Test
    void 중복_예약_요청시_409를_반환한다() {
        // given
        AdminReservationRequest body = AdminReservationApiRequestFixture.reserveSuccessRequestFixture();
        when(reservationService.reserve(any(ReservationCommand.class)))
                .thenThrow(new DuplicateEntityException("이미 예약이 존재하는 시간입니다."));

        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/admin/reservations")
                .then().log().all()
                .status(HttpStatus.CONFLICT);
    }

    @Test
    void 비즈니스_예외_발생시_400을_반환한다() {
        // given
        AdminReservationRequest body = AdminReservationApiRequestFixture.reserveSuccessRequestFixture();
        when(reservationService.reserve(any(ReservationCommand.class)))
                .thenThrow(new RoomEscapeException("비즈니스 예외"));

        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/admin/reservations")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 전체_예약_정보_조회_요청시_200OK와_예약_정보들을_응답한다() {
        // given
        when(reservationService.getAllReservations()).thenReturn(List.of());

        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().get("/api/admin/reservations")
                .then().log().all()
                .status(HttpStatus.OK)
                .body("size()", is(0));
    }
}
