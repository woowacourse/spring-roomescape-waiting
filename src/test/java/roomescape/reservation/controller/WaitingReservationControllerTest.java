package roomescape.reservation.controller;

import static org.hamcrest.Matchers.hasSize;
import static roomescape.Fixture.JOJO_EMAIL;
import static roomescape.Fixture.JOJO_NAME;
import static roomescape.Fixture.JOJO_PASSWORD;
import static roomescape.Fixture.KAKI_EMAIL;
import static roomescape.Fixture.KAKI_NAME;
import static roomescape.Fixture.KAKI_PASSWORD;
import static roomescape.Fixture.MEMBER_JOJO;
import static roomescape.Fixture.MEMBER_KAKI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.auth.domain.Role;
import roomescape.common.config.IntegrationTest;
import roomescape.common.util.CookieUtils;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.reservation.dto.request.ReservationSaveRequest;

class WaitingReservationControllerTest extends IntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("예약 대기 목록 조회에 성공하면 200 응답을 받는다.")
    @Test
    void findAll() {
        saveMemberAsKaki();
        saveThemeAsHorror();
        saveReservationTimeAsTen();
        saveWaitReservationAsDateNow();

        RestAssured.given().log().all()
                .cookie(CookieUtils.TOKEN_KEY, getMemberToken())
                .accept(ContentType.JSON)
                .when()
                .get("/reservations/wait")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("resources.$", hasSize(1));
    }

    @DisplayName("회원이 예약 대기를 성공적으로 추가하면 201 응답과 Location 헤더에 리소스 저장 경로를 받는다.")
    @Test
    void saveMemberWaitingReservation() throws JsonProcessingException {
        saveMember(MEMBER_JOJO);
        saveMember(MEMBER_KAKI);
        saveThemeAsHorror();
        saveReservationTimeAsTen();

        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(LocalDate.now(), 1L, 1L);
        String jojoToken = getToken(new Member(1L, Role.MEMBER, new MemberName(JOJO_NAME), JOJO_EMAIL, JOJO_PASSWORD));
        String kakiToken = getToken(new Member(2L, Role.MEMBER, new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        RestAssured.given().log().all()
                .cookie(CookieUtils.TOKEN_KEY, jojoToken)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(reservationSaveRequest))
                .accept(ContentType.JSON)
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .header("Location", "/reservations/1");

        RestAssured.given().log().all()
                .cookie(CookieUtils.TOKEN_KEY, kakiToken)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(reservationSaveRequest))
                .accept(ContentType.JSON)
                .when()
                .post("/reservations/wait")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .header("Location", "/reservations/wait/2");
    }

    @DisplayName("예약 대기을 성공적으로 승인하면 204 응답을 받는다.")
    @Test
    void approveReservation() {
        saveMember(MEMBER_KAKI);
        saveThemeAsHorror();
        saveReservationTimeAsTen();
        saveWaitReservationAsDateNow();

        RestAssured.given().log().all()
                .cookie(CookieUtils.TOKEN_KEY, getMemberToken())
                .accept(ContentType.JSON)
                .when()
                .patch("/reservations/wait/{id}", 1L)
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
