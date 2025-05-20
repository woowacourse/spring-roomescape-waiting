package roomescape.reservation.ui;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.ui.dto.LoginRequest;
import roomescape.fixture.ui.LoginApiFixture;
import roomescape.fixture.ui.MemberApiFixture;
import roomescape.fixture.ui.ReservationTimeApiFixture;
import roomescape.member.ui.dto.SignUpRequest;
import roomescape.reservation.ui.dto.request.CreateReservationTimeRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ReservationTimeRestControllerTest {

    @Test
    void 관리자_권한으로_예약_시간을_추가한다() {
        final Map<String, String> cookies = LoginApiFixture.adminLoginAndGetCookies();

        final CreateReservationTimeRequest reservationTimeRequest = ReservationTimeApiFixture.reservationTimeRequest1();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTimeRequest)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 회원_권한으로_예약_시간을_추가할_수_없다() {
        final SignUpRequest signUpRequest = MemberApiFixture.signUpRequest1();
        MemberApiFixture.signUp(signUpRequest);
        final Map<String, String> cookies = LoginApiFixture.memberLoginAndGetCookies(
                new LoginRequest(signUpRequest.email(), signUpRequest.password()));

        final CreateReservationTimeRequest reservationTimeRequest = ReservationTimeApiFixture.reservationTimeRequest1();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTimeRequest)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 전체_예약_시간을_조회한다() {
        final Map<String, String> cookies = LoginApiFixture.adminLoginAndGetCookies();

        final CreateReservationTimeRequest reservationTimeRequest1 = ReservationTimeApiFixture.reservationTimeRequest1();
        final CreateReservationTimeRequest reservationTimeRequest2 = ReservationTimeApiFixture.reservationTimeRequest2();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTimeRequest1)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTimeRequest2)
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

        final CreateReservationTimeRequest reservationTimeRequest = ReservationTimeApiFixture.reservationTimeRequest1();

        final Integer id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .body(reservationTimeRequest)
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
