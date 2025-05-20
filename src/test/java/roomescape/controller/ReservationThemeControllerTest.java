package roomescape.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.ReservationTheme;
import roomescape.service.dto.ReservationThemeRequest;
import roomescape.service.dto.ReservationThemeResponse;
import roomescape.service.ReservationThemeService;

@WebMvcTest(ReservationThemeController.class)
@Disabled
class ReservationThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationThemeService reservationThemeService;


    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @DisplayName("모든 테마를 성공적으로 가져오므로 200을 던진다.")
    @Test
    void reservationThemeList() {
        //given
        final ReservationTheme reservationTheme1 = new ReservationTheme(1L, "테마1", "디스크립션1", "썸네일1");
        final ReservationTheme reservationTheme2 = new ReservationTheme(2L, "테마2", "디스크립션2", "썸네일2");
        final List<ReservationThemeResponse> reservationThemeResponses = List.of(
                ReservationThemeResponse.from(reservationTheme1), ReservationThemeResponse.from(reservationTheme2));
        given(reservationThemeService.findReservationThemes()).willReturn(reservationThemeResponses);

        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then().log().all()
                .body("size()", Matchers.greaterThan(0))
                .statusCode(HttpStatus.OK.value());
    }

    @DisplayName("주간 인기 테마를 성공적으로 가져오므로 200을 던진다.")
    @Test
    void reservationThemeRankingList() {
        //given
        final ReservationTheme reservationTheme1 = new ReservationTheme(1L, "테마1", "디스크립션1", "썸네일1");
        final ReservationTheme reservationTheme2 = new ReservationTheme(2L, "테마2", "디스크립션2", "썸네일2");
        final List<ReservationThemeResponse> reservationThemeResponses = List.of(
                ReservationThemeResponse.from(reservationTheme1), ReservationThemeResponse.from(reservationTheme2));
        given(reservationThemeService.findPopularThemes()).willReturn(reservationThemeResponses);

        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes/ranking")
                .then().log().all()
                .body("size()", Matchers.greaterThan(0))
                .statusCode(HttpStatus.OK.value());
    }

    @DisplayName("테마를 성공적으로 추가하여 201을 던진다.")
    @Test
    void reservationThemeAdd() {
        //given
        final ReservationTheme reservationTheme1 = new ReservationTheme(1L, "테마1", "디스크립션1", "썸네일1");
        final ReservationThemeResponse reservationThemeResponse = ReservationThemeResponse.from(reservationTheme1);
        final ReservationThemeRequest reservationThemeRequest = new ReservationThemeRequest("테마1", "디스크립션1", "썸네일1");

        given(reservationThemeService.addReservationTheme(any(ReservationThemeRequest.class))).willReturn(reservationThemeResponse);

        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationThemeRequest)
                .when().post("/themes")
                .then().log().all()
                .body("themeId",is(1))
                .statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("테마를 성공적으로 삭제하여 204를 던진다.")
    @Test
    void reservationThemeRemove() {
        //given
        final long pathVariable = 1L;
        doNothing().when(reservationThemeService).removeReservationTheme(pathVariable);

        // when & then
        RestAssuredMockMvc.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

    }
}
