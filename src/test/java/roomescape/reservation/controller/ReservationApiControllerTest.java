package roomescape.reservation.controller;

import static org.hamcrest.Matchers.hasSize;
import static roomescape.util.Fixture.JOJO_EMAIL;
import static roomescape.util.Fixture.JOJO_NAME;
import static roomescape.util.Fixture.JOJO_PASSWORD;
import static roomescape.util.Fixture.KAKI_EMAIL;
import static roomescape.util.Fixture.KAKI_NAME;
import static roomescape.util.Fixture.KAKI_PASSWORD;
import static roomescape.util.Fixture.MEMBER_JOJO;
import static roomescape.util.Fixture.MEMBER_KAKI;

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
import roomescape.config.IntegrationTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.util.CookieUtils;

class ReservationApiControllerTest extends IntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("예약 목록 조회에 성공하면 200 응답을 받는다.")
    @Test
    void findAll() {
        RestAssured.given().log().all()
                .cookie(CookieUtils.TOKEN_KEY, getMemberToken())
                .accept(ContentType.JSON)
                .when()
                .get("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @DisplayName("회원별 예약 목록 조회에 성공하면 200 응답을 받는다.")
    @Test
    void findMemberReservations() {
        RestAssured.given().log().all()
                .cookie(CookieUtils.TOKEN_KEY, getMemberToken())
                .accept(ContentType.JSON)
                .when()
                .get("/reservations/mine")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @DisplayName("예약 대기 목록 조회에 성공하면 200 응답을 받는다.")
    @Test
    void findWaitingReservations() {
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

    @DisplayName("테마 아이디, 회원 아이디, 기간 조건 조회에 성공하면 200 응답을 받는다.")
    @Test
    void findAllBySearchCond() {
        saveAdminMember();
        saveThemeAsHorror();
        saveReservationTimeAsTen();
        saveSuccessReservationAsDateNow();

        String yesterday = LocalDate.now().minusDays(1).toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();
        RestAssured.given()
                .queryParam("themeId", 1)
                .queryParam("memberId", 1)
                .queryParam("dateFrom", yesterday)
                .queryParam("dateTo", tomorrow)
                .log().all()
                .cookie(CookieUtils.TOKEN_KEY, getMemberToken())
                .when()
                .get("/reservations/search")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("resources.$", hasSize(1));
    }

    @DisplayName("회원이 예약을 성공적으로 추가하면 201 응답과 Location 헤더에 리소스 저장 경로를 받는다.")
    @Test
    void saveMemberReservation() throws JsonProcessingException {
        saveMemberAsKaki();
        saveThemeAsHorror();
        saveReservationTimeAsTen();

        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(LocalDate.now(), 1L, 1L);

        RestAssured.given().log().all()
                .cookie(CookieUtils.TOKEN_KEY, getMemberToken())
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(reservationSaveRequest))
                .accept(ContentType.JSON)
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .header("Location", "/reservations/1");
    }

    @DisplayName("관리자가 예약을 성공적으로 추가하면 201 응답과 Location 헤더에 리소스 저장 경로를 받는다.")
    @Test
    void saveAdminReservation() throws JsonProcessingException {
        saveAdminMember();
        saveThemeAsHorror();
        saveReservationTimeAsTen();

        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(1L, LocalDate.now(), 1L, 1L);

        RestAssured.given().log().all()
                .cookie(CookieUtils.TOKEN_KEY, getAdminToken())
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(reservationSaveRequest))
                .accept(ContentType.JSON)
                .when()
                .post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .header("Location", "/reservations/1");
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

    @DisplayName("예약을 성공적으로 제거하면 204 응답을 받는다.")
    @Test
    void delete() {
        RestAssured.given().log().all()
                .cookie(CookieUtils.TOKEN_KEY, getMemberToken())
                .accept(ContentType.JSON)
                .when()
                .delete("/reservations/{id}", 1L)
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
