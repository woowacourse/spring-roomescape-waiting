package roomescape.reservation.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import roomescape.presentation.BaseControllerUnitTest;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.domain.Status;
import roomescape.reservation.presentation.AdminReservationController;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.time.application.dto.ReservationTimeInfo;

@WebMvcTest(AdminReservationController.class)
class AdminReservationControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private ReservationService reservationService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @Test
    void 전체_예약_정보_조회_요청에_성공하면_200_OK와_예약_정보들을_응답한다() {
        // given
        ReservationTimeInfo timeInfo = new ReservationTimeInfo(1L, LocalTime.of(10, 0));
        ThemeInfo themeInfo = new ThemeInfo(1L, "바니의 집", "http://image.png.image.com", "바니의 테마입니다.", true);
        List<ReservationInfo> expectedInfos = List.of(
                new ReservationInfo(1L, "웨지", LocalDate.of(2028, 5, 9), timeInfo, themeInfo, Status.RESERVED),
                new ReservationInfo(2L, "바니", LocalDate.of(2028, 5, 10), timeInfo, themeInfo, Status.RESERVED)
        );
        when(reservationService.getReservations(0, 10)).thenReturn(expectedInfos);

        // when & then
        List<ReservationResponse> response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("page", "0")
                .queryParam("size", "10")
                .when().get("/admin/reservations")
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {
                });

        assertThat(response).containsExactlyElementsOf(expectedInfos.stream().map(ReservationResponse::from).toList());
    }

    @Test
    void 전체_예약_정보_조회_요청_시_페이지가_음수이면_400_BAD_REQUEST를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("page", "-1")
                .queryParam("size", "10")
                .when().get("/admin/reservations")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("페이지 번호는 0 이상이어야 합니다."));
    }

    @Test
    void 전체_예약_정보_조회_요청_시_조회_개수가_양수가_아니면_400_BAD_REQUEST를_응답한다() {
        // when & then
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .queryParam("page", "0")
                .queryParam("size", "0")
                .when().get("/admin/reservations")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString("조회 개수는 양수여야 합니다."));
    }
}
