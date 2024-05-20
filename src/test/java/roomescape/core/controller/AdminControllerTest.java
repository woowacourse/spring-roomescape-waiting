package roomescape.core.controller;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.core.dto.reservation.ReservationRequest;
import roomescape.core.dto.reservationtime.ReservationTimeRequest;
import roomescape.core.dto.theme.ThemeRequest;
import roomescape.core.utils.e2eTest;

/**
 * 로그인 정보 (어드민) { "id": 1 "name": 어드민 "email": test@email.com "password": password "role": ADMIN }
 * <p>
 * 예약 정보 { "date": '2024-05-07', "member_id": 1, "time_id": 1, "theme_id": 1, "status": 'BOOKED' }
 **/

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class AdminControllerTest {
    private static final String TOMORROW_DATE = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);

    private String accessToken;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        accessToken = e2eTest.getAccessToken();
    }

    @Test
    @DisplayName("관리자 페이지로 이동한다.")
    void moveToAdminPage() {
        ValidatableResponse response = e2eTest.get("/admin", accessToken);
        response.statusCode(200);
    }

    @Test
    @DisplayName("예약 관리 페이지로 이동한다.")
    void moveToReservationManagePage() {
        ValidatableResponse response = e2eTest.get("/admin/reservation", accessToken);
        response.statusCode(200);
    }

    @Test
    @DisplayName("시간 관리 페이지로 이동한다.")
    void moveToTimeManagePage() {
        ValidatableResponse response = e2eTest.get("/admin/time", accessToken);
        response.statusCode(200);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "10:89"})
    @DisplayName("시간 생성 시, startAt 값의 형식이 올바르지 않으면 예외가 발생한다.")
    void validateTimeCreateWithEmpty(final String startAt) {
        ReservationTimeRequest request = new ReservationTimeRequest(startAt);

        ValidatableResponse response = e2eTest.post(request, "/admin/times", accessToken);
        response.statusCode(400);
    }

    @Test
    @DisplayName("시간 생성 시, startAt 값이 중복되면 예외가 발생한다.")
    void validateTimeDuplicated() {
        ReservationTimeRequest request = new ReservationTimeRequest("10:00");

        ValidatableResponse successResponse = e2eTest.post(request, "/admin/times", accessToken);
        successResponse.statusCode(201);

        ValidatableResponse failResponse = e2eTest.post(request, "/admin/times", accessToken);
        failResponse.statusCode(400);
    }

    @Test
    @DisplayName("시간 삭제 시, 해당 시간을 참조하는 예약이 있으면 예외가 발생한다.")
    void validateTimeDelete() {
        ValidatableResponse response = e2eTest.delete("/admin/times/1", accessToken);
        response.statusCode(400);
    }

    @Test
    @DisplayName("시간 삭제 시, 해당 시간을 참조하는 예약이 없으면 삭제된다.")
    void deleteTime() {
        ReservationTimeRequest request = new ReservationTimeRequest("10:00");

        ValidatableResponse postResponse = e2eTest.post(request, "/admin/times", accessToken);
        postResponse.statusCode(201);

        ValidatableResponse deleteResponse = e2eTest.delete("/admin/times/4", accessToken);
        deleteResponse.statusCode(204);
    }

    @Test
    @DisplayName("테마 관리 페이지로 이동한다.")
    void moveToThemeManagePage() {
        ValidatableResponse response = e2eTest.get("/admin/theme", accessToken);
        response.statusCode(200);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("테마 생성 시, name 값이 올바르지 않으면 예외가 발생한다.")
    void validateThemeWithNameEmpty(final String name) {
        ThemeRequest request = new ThemeRequest(name, "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

        ValidatableResponse response = e2eTest.post(request, "/admin/themes", accessToken);
        response.statusCode(400);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("테마 생성 시, description 값이 올바르지 않으면 예외가 발생한다.")
    void validateThemeWithDescriptionEmpty(final String description) {
        ThemeRequest request = new ThemeRequest("레벨2 탈출", description,
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

        ValidatableResponse response = e2eTest.post(request, "/admin/themes", accessToken);
        response.statusCode(400);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("테마 생성 시, thumbnail 값이 올바르지 않으면 예외가 발생한다.")
    void validateThemeWithThumbnailEmpty(final String thumbnail) {
        ThemeRequest request = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", thumbnail);

        ValidatableResponse response = e2eTest.post(request, "/admin/themes", accessToken);
        response.statusCode(400);
    }

    @Test
    @DisplayName("테마 생성 시, name 값이 중복이면 예외가 발생한다.")
    void validateThemeWithDuplicatedName() {
        ThemeRequest request = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

        ValidatableResponse successResponse = e2eTest.post(request, "/admin/themes", accessToken);
        successResponse.statusCode(201);

        ValidatableResponse failResponse = e2eTest.post(request, "/admin/themes", accessToken);
        failResponse.statusCode(400);
    }

    @Test
    @DisplayName("테마 삭제 시, 해당 테마를 참조하는 예약이 있으면 예외가 발생한다.")
    void validateThemeDelete() {
        ValidatableResponse failResponse = e2eTest.delete("/admin/themes/1", accessToken);
        failResponse.statusCode(400);
    }

    @Test
    @DisplayName("테마 삭제 시, 해당 테마를 참조하는 예약이 없으면 테마가 삭제된다.")
    void deleteTheme() {
        ValidatableResponse failResponse = e2eTest.delete("/admin/themes/2", accessToken);
        failResponse.statusCode(204);
    }

    @Test
    @DisplayName("예약자를 지정해서 예약을 생성할 수 있다.")
    void createReservationAsAdmin() {
        ReservationRequest request = new ReservationRequest(2L,
                TOMORROW_DATE, 1L, 1L);

        ValidatableResponse failResponse = e2eTest.post(request, "/admin/reservations", accessToken);
        failResponse.statusCode(201);
    }
}
