package roomescape.controller;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationThemeResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationController.class)
public class ReservationControllerTestV2 {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private MemberService memberService;


    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Nested
    class FailureTest {
        @DisplayName("날짜 포맷이 올바르지 않아 400 Bad request를 던진다")
        @Test
        void reservationAddFormatTest() {
            //given
            final Map<String, String> request = Map.of("themeId", "1", "date", "2023/08/05", "timeId", "1");

            //when & then
            RestAssuredMockMvc.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    class SuccessTest {
        @DisplayName("날짜 포맷이 올바르므로 201 created를 던진다")
        @Test
        @Disabled
        void reservationAddFormatTest() {
            //given
            final ReservationResponse response = new ReservationResponse(1L, "제프리", LocalDate.now(),
                    new ReservationTimeResponse(1L, LocalTime.now()),
                    new ReservationThemeResponse(1L, "테마", "설명", "썸네일"));
//            given(reservationService.addReservation(any(ReservationRequest.class))).willReturn(response);
            final Map<String, String> request = Map.of("themeId", "1", "date", "2023-08-05", "timeId", "1");

            RestAssuredMockMvc.given().log().all()
                    .contentType(ContentType.JSON)
                    .sessionId("test")
                    .sessionAttr("id", 1)
                    .body(request)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(HttpStatus.CREATED.value());
        }

    }


}
