package roomescape.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import roomescape.BaseControllerTest;
import roomescape.TestFixture;
import roomescape.domain.ReservationTime;
import roomescape.service.dto.request.ReservationTimeRequest;

class AdminTimeControllerTest extends BaseControllerTest {

    @DisplayName("시간을 추가한다.")
    @Test
    void addTime() {
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new ReservationTimeRequest(TestFixture.TIME_10AM))
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        assertThat(timeRepository.count()).isEqualTo(1);
    }

    @DisplayName("시간을 삭제한다.")
    @Test
    void deleteTime() {
        // given
        ReservationTime saved = timeRepository.save(TestFixture.getReservationTime10AM());

        // when & then
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .when().delete("/admin/times/" + saved.getId())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(timeRepository.count()).isZero();
    }

    @DisplayName("시간 생성에서 잘못된 값 입력시 400을 응답한다.")
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "24:01", "12:60"})
    void invalidTypeReservationTime(String startAt) {
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", startAt))
                .when().post("/admin/times")
                .then().log().all().assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("중복된 시간 추가 시도 시, 추가되지 않고 400을 응답한다.")
    @Test
    void duplicateReservationTime() {
        // given
        timeRepository.save(TestFixture.getReservationTime10AM());

        // when & then
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .contentType(ContentType.JSON)
                .body(new ReservationTimeRequest(TestFixture.TIME_10AM))
                .when().post("/admin/times")
                .then().log().all().assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
