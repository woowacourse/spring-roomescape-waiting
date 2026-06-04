package roomescape.time.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import roomescape.presentation.BaseControllerUnitTest;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.time.application.ReservationTimeService;
import roomescape.time.application.dto.AvailableReservationTimeFindCommand;
import roomescape.time.application.dto.AvailableReservationTimeInfo;
import roomescape.time.application.dto.ReservationTimeInfo;
import roomescape.time.presentation.ReservationTimeController;
import roomescape.time.presentation.dto.AvailableReservationTimeResponse;
import roomescape.time.presentation.dto.ReservationTimeResponse;

@WebMvcTest(ReservationTimeController.class)
class ReservationTimeControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @Test
    void 시간_목록_조회_요청에_성공하면_200_OK와_정상_응답이_반환된다() {
        // given
        List<ReservationTimeInfo> expectedInfos = List.of(
                new ReservationTimeInfo(1L, LocalTime.of(10, 0)),
                new ReservationTimeInfo(2L, LocalTime.of(11, 0))
        );
        when(reservationTimeService.getReservationTimes(0, 10)).thenReturn(expectedInfos);

        // when & then
        List<ReservationTimeResponse> response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .when().get("/times")
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).containsExactlyElementsOf(expectedInfos.stream().map(ReservationTimeResponse::from).toList());
    }

    @Test
    void 시간_목록_조회_요청_시_페이지가_음수이면_400_BAD_REQUEST를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("page", "-1")
                .queryParam("size", "10")
                .when().get("/times")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("페이지 번호는 0 이상이어야 합니다."));
    }

    @Test
    void 시간_목록_조회_요청_시_조회_개수가_양수가_아니면_400_BAD_REQUEST를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("page", "0")
                .queryParam("size", "0")
                .when().get("/times")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("조회 개수는 양수여야 합니다."));
    }

    @Test
    void 예약_가능_시간_조회_요청에_성공하면_200_OK와_정상_응답이_반환된다() {
        // given
        ThemeInfo themeInfo = new ThemeInfo(1L, "공포테마", "https://image.com/image.png", "설명", true);
        AvailableReservationTimeInfo expectedInfo = new AvailableReservationTimeInfo(themeInfo,
                List.of(new ReservationTimeInfo(1L, LocalTime.of(10, 0))));
        when(reservationTimeService.getAvailableReservationTime(any(AvailableReservationTimeFindCommand.class)))
                .thenReturn(expectedInfo);

        // when & then
        AvailableReservationTimeResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("themeId", "1")
                .queryParam("date", "2026-05-06")
                .when().get("/times/available")
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).isEqualTo(AvailableReservationTimeResponse.from(expectedInfo));
    }

    @Test
    void 예약_가능_시간_조회_요청_시_테마_ID가_없으면_400_BAD_REQUEST를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("date", "2026-05-06")
                .when().get("/times/available")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("[themeId] 테마 ID는 필수입니다."));
    }

    @Test
    void 예약_가능_시간_조회_요청_시_날짜가_없으면_400_BAD_REQUEST를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("themeId", "1")
                .when().get("/times/available")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("[date] 날짜는 필수입니다."));
    }
}
