package roomescape.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.reservation.domain.ReservationTime;
import roomescape.support.dto.TokenCookieDto;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;

public class ReservationTimeControllerTest extends IntegrationTest {

    @Test
    @DisplayName("처음으로 등록하는 시간의 id는 1이다.")
    void firstPost() {
        Map<String, String> params = Map.of(
                "startAt", "17:00"
        );
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("email@email.com", "password", port);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .body("data.id", is(1))
                .header("Location", "/times/1");
    }

    @Test
    @DisplayName("아무 시간도 등록 하지 않은 경우, 시간 목록 조회 결과 개수는 0개이다.")
    void readEmptyTimes() {
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("email@email.com", "password", port);

        RestAssured.given().log().all()
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("data.times.size()", is(0));
    }

    @Test
    @DisplayName("등록된 예약시간을 모두 조회한다.")
    void readTimesSizeAfterFirstPost() {
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("email@email.com", "password", port);
        reservationTimeFixture.createTime();

        RestAssured.given().log().all()
                .port(port)
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("data.times.size()", is(1));
    }

    @Test
    @DisplayName("관리자는 예약시간을 삭제할 수 있다.")
    void readTimesSizeAfterPostAndDelete() {
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("email@email.com", "password", port);
        ReservationTime time = reservationTimeFixture.createTime();

        RestAssured.given().log().all()
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .when().delete("/admin/times/" + time.getId())
                .then().log().all()
                .statusCode(204);

        int reservationTimeSize = reservationTimeFixture.findAll().size();
        Assertions.assertThat(reservationTimeSize).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("validateRequestDataFormatSource")
    @DisplayName("예약 시간 생성 시, 시간 요청 데이터에 시간 포맷이 아닌 값이 입력되어오면 400 에러를 발생한다.")
    void validateRequestDataFormat(Map<String, String> request) {
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("email@email.com", "password", port);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .body(request)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400);
    }

    static Stream<Map<String, String>> validateRequestDataFormatSource() {
        return Stream.of(
                Map.of(
                        "startAt", "24:59"
                ),
                Map.of(
                        "startAt", "hihi")
        );
    }

    @ParameterizedTest
    @MethodSource("validateBlankRequestSource")
    @DisplayName("예약 시간 생성 시, 요청 값에 공백 또는 null이 포함되어 있으면 400 에러를 발생한다.")
    void validateBlankRequest(Map<String, String> request) {
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("email@email.com", "password", port);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .body(request)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400);
    }

    static Stream<Map<String, String>> validateBlankRequestSource() {
        return Stream.of(
                Map.of(),
                Map.of("startAt", ""),
                Map.of("startAt", " ")
        );
    }
}
