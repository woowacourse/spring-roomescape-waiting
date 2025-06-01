package roomescape.waiting.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WaitingControllerTest {
    @DisplayName("일반 유저는 자신이 생성한 대기 예약에 대해 삭제 요청을 보낼 수 있다.")
    @Test
    void deleteByMember() {
        // given
        String token = getDefaultMemberLoginToken();
        Long themeId = addTheme();
        Long timeId = addReservationTime(LocalTime.of(10, 0));

        Long waitingId = addWaiting(token, LocalDate.now().plusDays(1), themeId, timeId);

        // when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/waiting/" + waitingId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("어드민은 유저의 대기 예약에 대해 삭제 요청을 보낼 수 있다.")
    @Test
    void deleteByAdmin() {
        // given
        String memberLoginToken = getDefaultMemberLoginToken();
        String adminLoginToken = getAdminLoginToken();
        Long themeId = addTheme();
        Long timeId = addReservationTime(LocalTime.of(10, 0));

        Long waitingId = addWaiting(memberLoginToken, LocalDate.now().plusDays(1), themeId, timeId);

        // when & then
        RestAssured.given().log().all()
                .cookie("token", adminLoginToken)
                .when().delete("/waiting/" + waitingId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("일반 유저는 다른 유저가 생성한 대기 예약에 대해 삭제 요청을 보낼 수 없다.")
    @Test
    void disableToDeleteOthersByMember() {
        // given
        Member other = new Member("other", "other@test.com", "12341234", Role.MEMBER);
        signup(other);

        String otherLoginToken = getCustomMemberLoginToken(other);
        String defaultMemberLoginToken = getDefaultMemberLoginToken();

        Long themeId = addTheme();
        Long timeId = addReservationTime(LocalTime.of(10, 0));

        Long waitingId = addWaiting(defaultMemberLoginToken, LocalDate.now().plusDays(1), themeId, timeId);

        // when & then
        RestAssured.given().log().all()
                .cookie("token", otherLoginToken)
                .when().delete("/waiting/" + waitingId)
                .then().log().all()
                .statusCode(403);
    }

    private void signup(Member member) {
        Map<String, Object> params = Map.of(
                "email", member.getEmail(),
                "password", member.getPassword(),
                "name", member.getName()
        );
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when()
                .post("/members")
                .then().statusCode(201);
    }

    private String getCustomMemberLoginToken(Member member) {
        Map<String, String> params = Map.of(
                "email", member.getEmail(),
                "password", member.getPassword()
        );
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract().cookie("token");
    }

    private String getDefaultMemberLoginToken() {
        Map<String, String> params = Map.of(
                "email", "user@woowa.com",
                "password", "12341234"
        );
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract().cookie("token");
    }

    private String getAdminLoginToken() {
        Map<String, String> params = Map.of(
                "email", "admin@woowa.com",
                "password", "12341234"
        );
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract().cookie("token");
    }

    private Long addWaiting(String token, LocalDate date, Long themeId, Long timeId) {
        Map<String, Object> params = Map.of(
                "date", date,
                "themeId", themeId,
                "timeId", timeId
        );
        Number id = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(params)
                .when()
                .post("/waiting")
                .then().extract().body().path("id");
        return id.longValue();
    }

    private Long addTheme() {
        Map<String, String> themeParams = Map.of(
                "name", "테마1", "description", "테마1", "thumbnail", "www.m.com"
        );
        Number id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/themes")
                .then().extract().body().path("id");
        return id.longValue();
    }

    private Long addReservationTime(final LocalTime time) {
        Map<String, Object> timeParams = Map.of("startAt", time);

        Number id = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(timeParams)
                .when().post("/times")
                .then().extract().body().path("id");
        return id.longValue();
    }
}
