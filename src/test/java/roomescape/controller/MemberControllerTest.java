package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.auth.dto.LoginRequest;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;

@Sql("/truncate-with-admin-and-guest.sql")
class MemberControllerTest extends ControllerTest {
    private String adminToken;

    @DisplayName("모든 사용자 조회 성공 테스트 - 사용자 총 2명")
    @TestFactory
    Stream<DynamicTest> findAllMembers() {
        return Stream.of(
                DynamicTest.dynamicTest("어드민이 로그인한다.", () -> {
                    adminToken = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(new LoginRequest("admin123", "admin@email.com"))
                            .when().post("/login")
                            .then().log().all().extract().cookie("token");
                }),
                DynamicTest.dynamicTest("어드민이 모든 사용자 정보를 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().get("/members")
                            .then().log().all()
                            .assertThat().statusCode(HttpStatus.OK.value()).body("size()", is(2));
                })
        );
    }
}
