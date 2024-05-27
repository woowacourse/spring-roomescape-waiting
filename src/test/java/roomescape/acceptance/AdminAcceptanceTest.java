package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.reservation.dto.AdminReservationRequest;
import roomescape.service.theme.dto.ThemeRequest;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@Sql("/truncate-with-reservations.sql")
class AdminAcceptanceTest extends AcceptanceTest {
    private LocalDate date;
    private long timeId;
    private long themeId;
    private long guestId;
    private String adminToken;
    private String guestToken;

    @BeforeEach
    void init() {
        date = LocalDate.now().plusDays(1);
        timeId = 1;
        themeId = 1;
        guestId = 2;

        adminToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("admin123", "admin@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");

        guestToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("guest123", "guest@email.com"))
                .when().post("/login")
                .then().log().all().extract().cookie("token");
    }

    @DisplayName("예약 추가 성공 테스트")
    @Test
    void createReservation() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .body(new AdminReservationRequest(date, guestId, timeId, themeId))
                .when().post("/admin/reservations")
                .then().log().all()
                .assertThat().statusCode(201).body("id", is(greaterThan(0)));
    }

    @DisplayName("조건별 예약 내역 조회 테스트 - 사용자, 테마")
    @Test
    void findByMemberAndTheme() {
        //when & then
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .queryParam("memberId", 1)
                .queryParam("themeId", 2)
                .when().get("/admin/reservations/search")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(0));
    }

    @DisplayName("조건별 예약 내역 조회 테스트 - 시작 날짜")
    @Test
    void findByDateFrom() {
        //when & then
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .queryParam("dateFrom", LocalDate.now().plusDays(7).toString())
                .when().get("/admin/reservations/search")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(2));
    }

    @DisplayName("조건별 예약 내역 조회 테스트 - 테마")
    @Test
    void findByTheme() {
        //when & then
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .queryParam("themeId", 1)
                .when().get("/admin/reservations/search")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(1));
    }


    @DisplayName("테마 삭제 성공 테스트")
    @TestFactory
    Stream<DynamicTest> deleteTheme() {
        AtomicLong themeId = new AtomicLong();
        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        return Stream.of(
                DynamicTest.dynamicTest("테마를 생성한다.", () -> {
                    themeId.set((int) RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .contentType(ContentType.JSON).body(themeRequest)
                            .when().post("/admin/themes")
                            .then().extract().response().jsonPath().get("id"));
                }),
                DynamicTest.dynamicTest("테마를 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .when().delete("/admin/themes/" + themeId)
                            .then().log().all().statusCode(204);

                }),
                DynamicTest.dynamicTest("모든 테마를 조회하면 0개이다.", () -> {
                    RestAssured.given().log().all()
                            .when().get("/themes")
                            .then().log().all()
                            .assertThat().statusCode(200).body("size()", is(3));
                })
        );
    }

    @DisplayName("일반 사용자는 테마를 삭제할 수 없다.")
    @TestFactory
    Stream<DynamicTest> cannotDeleteThemeByGuest() {
        AtomicLong themeId = new AtomicLong();
        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        return Stream.of(
                DynamicTest.dynamicTest("테마를 생성한다.", () -> {
                    themeId.set((int) RestAssured.given().log().all()
                            .cookie("token", adminToken)
                            .contentType(ContentType.JSON).body(themeRequest)
                            .when().post("/admin/themes")
                            .then().extract().response().jsonPath().get("id"));
                }),
                DynamicTest.dynamicTest("테마를 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookie("token", guestToken)
                            .when().delete("/admin/themes/" + themeId)
                            .then().log().all()
                            .assertThat().statusCode(403).body("message", is("권한이 없습니다. 관리자에게 문의해주세요."));

                })
        );
    }

    @DisplayName("관리자가 테마를 추가한다.")
    @Test
    void createThemeByAdmin() {
        //given
        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

        //when&then
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON).body(themeRequest)
                .when().post("/admin/themes")
                .then().log().all().statusCode(201).body("id", is(greaterThan(0)));
    }

    @DisplayName("일반 사용자가 테마를 추가한다.")
    @Test
    void createThemeByMember() {
        //given
        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

        //when&then
        RestAssured.given().log().all()
                .cookie("token", guestToken)
                .contentType(ContentType.JSON).body(themeRequest)
                .when().post("/admin/themes")
                .then().log().all()
                .assertThat().statusCode(403)
                .body("message", is("권한이 없습니다. 관리자에게 문의해주세요."));
    }

    @DisplayName("테마 추가 실패 테스트 - 썸네일 형식 오류")
    @Test
    void cannotCreateThemeByThumbnail() {
        //given
        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "//i.pinimg.com/236x/6e/bc/4");

        //when&then
        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON).body(themeRequest)
                .when().post("/admin/themes")
                .then().log().all().statusCode(400).body("message", is("올바르지 않은 썸네일 형식입니다."));
    }
}
