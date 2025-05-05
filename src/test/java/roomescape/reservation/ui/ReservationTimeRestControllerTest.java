package roomescape.reservation.ui;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.fixture.ui.LoginApiFixture;
import roomescape.fixture.ui.MemberApiFixture;
import roomescape.fixture.ui.ReservationTimeApiFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationTimeRestControllerTest {

    @Test
    void 관리자_권한으로_예약_시간을_추가한다() {
        final Map<String, String> cookies = LoginApiFixture.adminLoginAndGetCookies();

        final Map<String, String> reservationTimeParams = ReservationTimeApiFixture.reservationTimeParams1();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTimeParams)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 회원_권한으로_예약_시간을_추가할_수_없다() {
        final Map<String, String> singUpParams = MemberApiFixture.signUpParams1();
        MemberApiFixture.signUp(singUpParams);
        final Map<String, String> cookies = LoginApiFixture.memberLoginAndGetCookies(singUpParams);

        final Map<String, String> reservationTimeParams = ReservationTimeApiFixture.reservationTimeParams1();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTimeParams)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 전체_예약_시간을_조회한다() {
        final Map<String, String> cookies = LoginApiFixture.adminLoginAndGetCookies();

        final Map<String, String> reservationTimeParams = ReservationTimeApiFixture.reservationTimeParams1();
        final Map<String, String> reservationTimeParams2 = ReservationTimeApiFixture.reservationTimeParams2();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTimeParams)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTimeParams2)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(2));
    }

    @Test
    void 관리자_권한으로_예약_시간을_삭제한다() {
        final Map<String, String> cookies = LoginApiFixture.adminLoginAndGetCookies();

        final Map<String, String> reservationTimeParams = ReservationTimeApiFixture.reservationTimeParams1();

        final Integer id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTimeParams)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().path("id");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .when().delete("/times/{id}", id)
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
