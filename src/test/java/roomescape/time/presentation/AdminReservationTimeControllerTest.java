package roomescape.time.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import roomescape.presentation.BaseControllerUnitTest;
import roomescape.presentation.fixture.ReservationTimeRequestFixture;
import roomescape.time.application.ReservationTimeService;
import roomescape.time.application.dto.ReservationTimeCommand;
import roomescape.time.application.dto.ReservationTimeInfo;
import roomescape.time.presentation.AdminReservationTimeController;
import roomescape.time.presentation.dto.ReservationTimeRequest;
import roomescape.time.presentation.dto.ReservationTimeResponse;

@WebMvcTest(AdminReservationTimeController.class)
class AdminReservationTimeControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @ParameterizedTest(name = "요청 정보가 {0} 일 때, 예외 메세지 \"{1}\"가 발생한다.")
    @MethodSource("roomescape.presentation.fixture.ReservationTimeRequestFixture#registerFailRequestFixture")
    void 시간_등록_요청_시_형식_검증에_실패하면_예외가_발생한다(ReservationTimeRequest request, String exceptionMessage) {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(request)
                .when().post("/admin/times")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString(exceptionMessage));
    }

    @Test
    void 시간_등록에_성공하면_201_CREATED와_정상_응답이_반환된다() {
        // given
        ReservationTimeRequest request = ReservationTimeRequestFixture.registerSuccessRequestFixture();
        ReservationTimeInfo expectedInfo = new ReservationTimeInfo(1L, request.startAt());
        when(reservationTimeService.create(any(ReservationTimeCommand.class))).thenReturn(expectedInfo);

        // when & then
        ReservationTimeResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(request)
                .when().post("/admin/times")
                .then().log().all()
                .status(HttpStatus.CREATED)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).isEqualTo(ReservationTimeResponse.from(expectedInfo));
    }

    @Test
    void 정상적인_ID로_시간_비활성화_요청_시_204_NO_CONTENT를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().delete("/admin/times/1")
                .then().log().all()
                .status(HttpStatus.NO_CONTENT);

        verify(reservationTimeService, times(1)).deactivate(anyLong());
    }
}
