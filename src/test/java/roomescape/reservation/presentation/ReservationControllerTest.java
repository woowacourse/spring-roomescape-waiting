package roomescape.reservation.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import roomescape.presentation.BaseControllerUnitTest;
import roomescape.presentation.fixture.ReservationRequestFixture;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationWaitingInfo;
import roomescape.reservation.domain.Status;
import roomescape.reservation.presentation.ReservationController;
import roomescape.reservation.presentation.dto.ReservationChangeRequest;
import roomescape.reservation.presentation.dto.ReservationWaitingResponse;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.time.application.dto.ReservationTimeInfo;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private ReservationService reservationService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @ParameterizedTest(name = "요청 정보가 {0} 일 때, 예외 메세지 \"{1}\"가 발생한다.")
    @MethodSource("roomescape.presentation.fixture.ReservationRequestFixture#reserveFailRequestFixture")
    void 예약_요청_시_형식_검증에_실패하면_예외가_발생한다(ReservationRequest body, String exceptionMessage) {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/reservations")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString(exceptionMessage));
    }

    @Test
    void 예약_요청에_성공하면_201_CREATED와_정상_응답이_반환된다() {
        // given
        ReservationRequest request = ReservationRequestFixture.reserveSuccessRequestFixture();
        ReservationInfo expectedInfo = reservationInfo(1L, "바니", Status.RESERVED);
        when(reservationService.create(any(ReservationCreateCommand.class))).thenReturn(expectedInfo);

        // when & then
        ReservationResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .status(HttpStatus.CREATED)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).isEqualTo(ReservationResponse.from(expectedInfo));
    }

    @Test
    void 예약_목록_조회_요청에_성공하면_200_OK와_예약_목록이_반환된다() {
        // given
        ReservationWaitingInfo expectedInfo = new ReservationWaitingInfo(1L, "바니", LocalDate.now().plusDays(1),
                new ReservationTimeInfo(1L, LocalTime.of(10, 0)),
                new ThemeInfo(1L, "공포테마", "https://image.com/image.png", "설명", true),
                Status.WAITING, 1L);
        when(reservationService.getReservationsByName("바니")).thenReturn(List.of(expectedInfo));

        // when & then
        List<ReservationWaitingResponse> response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("username", "바니")
                .when().get("/reservations")
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).containsExactly(ReservationWaitingResponse.from(expectedInfo));
    }

    @Test
    void 예약_목록_조회_요청_시_사용자_이름이_없으면_400_BAD_REQUEST를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().get("/reservations")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("username 파라미터가 누락 되었습니다."));
    }

    @Test
    void 정상적인_예약_ID로_예약_취소_요청_시_204_NO_CONTENT를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("username", "바니")
                .when().delete("/reservations/1")
                .then().log().all()
                .status(HttpStatus.NO_CONTENT);

        verify(reservationService, times(1)).cancel(anyLong(), anyString());
    }

    @Test
    void 예약_취소_요청_시_사용자_이름이_없으면_400_BAD_REQUEST를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("username 파라미터가 누락 되었습니다."));
    }

    @ParameterizedTest(name = "요청 정보가 {0} 일 때, 예외 메세지 \"{1}\"가 발생한다.")
    @MethodSource("roomescape.presentation.fixture.ReservationRequestFixture#modifyFailRequestFixture")
    void 예약_수정_요청_시_형식_검증에_실패하면_예외가_발생한다(ReservationChangeRequest body, String exceptionMessage) {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().patch("/reservations/1")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString(exceptionMessage));
    }

    @Test
    void 정상적인_예약_ID로_예약_수정_요청_시_200_OK와_정상_응답을_반환한다() {
        // given
        ReservationChangeRequest request = ReservationRequestFixture.modifySuccessRequestFixture();
        ReservationInfo expectedInfo = reservationInfo(1L, "바니", Status.RESERVED);
        when(reservationService.modify(anyLong(), any(ReservationChangeCommand.class))).thenReturn(expectedInfo);

        // when & then
        ReservationResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(request)
                .when().patch("/reservations/1")
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).isEqualTo(ReservationResponse.from(expectedInfo));
    }

    private ReservationInfo reservationInfo(Long id, String name, Status status) {
        return new ReservationInfo(id, name, LocalDate.now().plusDays(1),
                new ReservationTimeInfo(1L, LocalTime.of(10, 0)),
                new ThemeInfo(1L, "공포테마", "https://image.com/image.png", "설명", true),
                status);
    }
}
