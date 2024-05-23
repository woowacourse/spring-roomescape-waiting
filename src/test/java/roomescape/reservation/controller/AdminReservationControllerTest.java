package roomescape.reservation.controller;

import static roomescape.Fixture.TOMORROW;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.common.config.IntegrationTest;
import roomescape.common.util.CookieUtils;
import roomescape.reservation.dto.request.AdminReservationSaveRequest;

class AdminReservationControllerTest extends IntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("관리자가 예약을 성공적으로 추가하면 201 응답과 Location 헤더에 리소스 저장 경로를 받는다.")
    @Test
    void saveAdminReservation() throws JsonProcessingException {
        saveAdminMember();
        saveMemberAsKaki();
        saveThemeAsHorror();
        saveReservationTimeAsTen();

        AdminReservationSaveRequest adminRequest = new AdminReservationSaveRequest(2L, TOMORROW, 1L, 1L);

        RestAssured.given().log().all()
                .cookie(CookieUtils.TOKEN_KEY, getAdminToken())
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(adminRequest))
                .accept(ContentType.JSON)
                .when()
                .post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .header("Location", "/reservations/1");
    }
}
