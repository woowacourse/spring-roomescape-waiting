package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.dto.LoginRequest;
import roomescape.waiting.dto.WaitingSimpleResponse;
import roomescape.waiting.repository.WaitingRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql({"/test-time-data.sql", "/test-theme-data.sql", "/test-member-data.sql", "/test-waiting-data.sql"})
public class AdminWaitingApiTest {

    public static final String AUTH_COOKIE_NAME = "token";
    private static String ADMIN_TOKEN;

    @BeforeEach
    void setUp() {
        ADMIN_TOKEN = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("admin@gmail.com", "1234"))
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie(AUTH_COOKIE_NAME);
    }

    @DisplayName("어드민 예약 대기 목록 조회 테스트")
    @Nested
    class FindAllTest {

        @Autowired
        private WaitingRepository waitingRepository;

        @DisplayName("어드민 예약 목록 조회에 성공하면 200을 반환한다.")
        @Test
        void testFindAll() {
            // given
            // when
            WaitingSimpleResponse[] responses = RestAssured.given().log().all()
                    .cookie(AUTH_COOKIE_NAME, ADMIN_TOKEN)
                    .when().get("/admin/waitings")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .as(WaitingSimpleResponse[].class);
            // then
            assertThat(responses.length).isEqualTo(waitingRepository.count());
        }

        @DisplayName("인증 정보가 올바르지 않을 경우 401을 반환한다.")
        @Test
        void testUnauthorized() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .when().get("/admin/waitings")
                    .then().log().all()
                    .statusCode(401);
        }

        @DisplayName("어드민 권한이 없을 경우 403을 반환한다.")
        @Test
        void testForbidden() {
            // given
            String memberToken = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(new LoginRequest("aaa@gmail.com", "1234"))
                    .when().post("/login")
                    .then().log().all()
                    .extract().cookie(AUTH_COOKIE_NAME);
            // when
            // then
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(AUTH_COOKIE_NAME, memberToken)
                    .when().get("/admin/waitings")
                    .then().log().all()
                    .statusCode(403);
        }
    }

    @DisplayName("어드민 예약 대기 거절 테스트")
    @Nested
    class DeleteWaitingTest {

        @Autowired
        private WaitingRepository waitingRepository;

        @DisplayName("어드민 예약 대기 거절 성공하면 204를 반환한다.")
        @Test
        void testDeleteWaiting() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(AUTH_COOKIE_NAME, ADMIN_TOKEN)
                    .when().delete("/admin/waitings/{id}", 1L)
                    .then().log().all()
                    .statusCode(204);
            assertThat(waitingRepository.count()).isEqualTo(1);
        }

        @DisplayName("인증 정보가 올바르지 않을 경우 401을 반환한다.")
        @Test
        void testUnauthorized() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .when().delete("/admin/waitings/{id}", 1L)
                    .then().log().all()
                    .statusCode(401);
        }

        @DisplayName("어드민 권한이 없을 경우 403을 반환한다.")
        @Test
        void testForbidden() {
            // given
            String memberToken = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(new LoginRequest("aaa@gmail.com", "1234"))
                    .when().post("/login")
                    .then().log().all()
                    .extract().cookie(AUTH_COOKIE_NAME);
            // when
            // then
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(AUTH_COOKIE_NAME, memberToken)
                    .when().delete("/admin/waitings/{id}", 1L)
                    .then().log().all()
                    .statusCode(403);
        }
    }
}
