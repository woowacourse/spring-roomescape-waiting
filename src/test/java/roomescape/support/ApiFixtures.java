package roomescape.support;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.feature.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.theme.dto.request.ThemeCreateRequestDto;
import roomescape.feature.theme.dto.response.ThemeResponseDto;
import roomescape.feature.time.dto.request.TimeCreateRequestDto;
import roomescape.feature.time.dto.response.TimeResponseDto;

public final class ApiFixtures {

    private ApiFixtures() {
    }

    public static TimeResponseDto 시간_등록(LocalTime startAt) {
        return given()
            .contentType(ContentType.JSON)
            .body(new TimeCreateRequestDto(startAt))
            .when().post("/api/admin/times")
            .then().statusCode(201)
            .extract().as(TimeResponseDto.class);
    }

    public static ThemeResponseDto 테마_등록(String name, String description, String imageUrl) {
        return given()
            .contentType(ContentType.JSON)
            .body(new ThemeCreateRequestDto(name, description, imageUrl))
            .when().post("/api/admin/themes")
            .then().statusCode(201)
            .extract().as(ThemeResponseDto.class);
    }

    public static ReservationCreateResponseDto 예약_생성(String name, LocalDate date, Long timeId, Long themeId) {
        return given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto(name, date, timeId, themeId))
            .when().post("/api/reservations")
            .then().statusCode(201)
            .extract().as(ReservationCreateResponseDto.class);
    }

    public static ReservationCreateResponseDto 예약_대기_생성(String name, LocalDate date, Long timeId, Long themeId) {
        return given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto(name, date, timeId, themeId))
            .when().post("/api/reservations/waitings")
            .then().statusCode(201)
            .extract().as(ReservationCreateResponseDto.class);
    }
}
