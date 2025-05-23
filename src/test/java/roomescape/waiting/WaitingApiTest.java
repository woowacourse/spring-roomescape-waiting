package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
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
import roomescape.waiting.dto.CreateWaitingRequest;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.repository.WaitingRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql({"/test-time-data.sql", "/test-theme-data.sql", "/test-member-data.sql", "/test-waiting-data.sql"})
public class WaitingApiTest {

    public static final String AUTH_COOKIE_NAME = "token";
    private static String MEMBER_1_TOKEN;
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    @BeforeEach
    void setUp() {
        MEMBER_1_TOKEN = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("aaa@gmail.com", "1234"))
                .when().post("/login")
                .then().log().all()
                .extract().cookie(AUTH_COOKIE_NAME);
    }

    @DisplayName("예약 대기 생성 테스트")
    @Nested
    class CreateWaitingTest {

        private static final CreateWaitingRequest REQUEST = new CreateWaitingRequest(TOMORROW, 1L, 1L);

        @DisplayName("예약 대기 생성에 성공하면 201을 반환한다.")
        @Test
        void testCreateWaiting() {
            WaitingResponse response = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(AUTH_COOKIE_NAME, MEMBER_1_TOKEN)
                    .body(REQUEST)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(201)
                    .extract()
                    .as(WaitingResponse.class);
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(3L),
                    () -> assertThat(response.reservation().id()).isEqualTo(1L),
                    () -> assertThat(response.reservation().member().id()).isEqualTo(2L),
                    () -> assertThat(response.member().id()).isEqualTo(1L)
            );
        }

        @DisplayName("인증 정보가 올바르지 않을 경우 401을 반환한다.")
        @Test
        void testUnauthorized() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(REQUEST)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(401);
        }

        @DisplayName("요청한 예약을 찾을 수 없는 경우 400을 반환한다.")
        @Test
        void testNoMatchReservation() {
            // given
            CreateWaitingRequest invalidRequest = new CreateWaitingRequest(TOMORROW.plusDays(10), 1L, 1L);
            // when
            // then
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(AUTH_COOKIE_NAME, MEMBER_1_TOKEN)
                    .body(invalidRequest)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(400);
        }

        @DisplayName("중복 예약 대기인 경우 400을 반환한다.")
        @Test
        void testDuplicateWaiting() {
            // given
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(AUTH_COOKIE_NAME, MEMBER_1_TOKEN)
                    .body(REQUEST)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(201);
            // when
            // then
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(AUTH_COOKIE_NAME, MEMBER_1_TOKEN)
                    .body(REQUEST)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(400);
        }

        @DisplayName("이미 사용자가 선점한 예약인 경우 400을 반환한다.")
        @Test
        void testPreempted() {
            // given
            CreateWaitingRequest invalidRequest = new CreateWaitingRequest(TOMORROW.minusDays(1), 3L, 3L);
            // when
            // then
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(AUTH_COOKIE_NAME, MEMBER_1_TOKEN)
                    .body(invalidRequest)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(400);
        }

        @DisplayName("지니간 시간과 날짜에 예약 대기를 할 경우 400을 반환한다.")
        @Test
        void testIsBefore() {
            // given
            CreateWaitingRequest invalidRequest = new CreateWaitingRequest(TOMORROW.minusDays(2), 1L, 1L);
            // when
            // then
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie(AUTH_COOKIE_NAME, MEMBER_1_TOKEN)
                    .body(invalidRequest)
                    .when().post("/waitings")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @DisplayName("내 예약 대기 취소 테스트")
    @Nested
    class DeleteWaitingTest {

        private static final int MEMBER_1_WAITING_ID = 1;
        private static final int MEMBER_2_WAITING_ID = 2;

        @Autowired
        private WaitingRepository waitingRepository;

        @DisplayName("내 예약 대기 취소에 성공하면 204를 반환한다.")
        @Test
        void testDeleteWaiting() {
            RestAssured.given().log().all()
                    .cookie(AUTH_COOKIE_NAME, MEMBER_1_TOKEN)
                    .when().delete("/waitings/{id}", MEMBER_1_WAITING_ID)
                    .then().log().all()
                    .statusCode(204);
            assertThat(waitingRepository.count()).isEqualTo(1);
        }

        @DisplayName("인증 정보가 올바르지 않을 경우 401을 반환한다.")
        @Test
        void testUnauthorized() {
            RestAssured.given().log().all()
                    .when().delete("/waitings/{id}", MEMBER_1_WAITING_ID)
                    .then().log().all()
                    .statusCode(401);
        }

        @DisplayName("내가 생성한 예약 대기 ID가 아닐 경우 403을 반환한다.")
        @Test
        void testForbidden() {
            RestAssured.given().log().all()
                    .cookie(AUTH_COOKIE_NAME, MEMBER_1_TOKEN)
                    .when().delete("/waitings/{id}", MEMBER_2_WAITING_ID)
                    .then().log().all()
                    .statusCode(403);
        }
    }
}
