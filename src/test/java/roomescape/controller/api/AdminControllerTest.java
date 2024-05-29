package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.controller.BaseControllerTest;
import roomescape.controller.dto.request.AdminReservationRequest;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

class AdminControllerTest extends BaseControllerTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));
    }

    @TestFactory
    @DisplayName("어드민이 예약을 생성한다.")
    Stream<DynamicTest> adminReservation() {
        return Stream.of(
                DynamicTest.dynamicTest("어드민이 로그인한다.", this::adminLogin),
                DynamicTest.dynamicTest("어드민이 예약에 성공한다.", this::addAdminReservation)
        );
    }

    @TestFactory
    @DisplayName("어드민이 아니면 예약을 생성할 수 없다.")
    Stream<DynamicTest> failWhenNotAdminReservation() {
        return Stream.of(
                DynamicTest.dynamicTest("유저가 로그인한다.", this::userLogin),
                DynamicTest.dynamicTest("어드민이 아니면 예약에 실패한다.", this::addAdminReservationFailWhenNotAdmin)
        );
    }

    void addAdminReservation() {
        AdminReservationRequest request = new AdminReservationRequest(
                LocalDate.of(2024, 6, 22),
                1L,
                1L,
                ADMIN_ID
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("token", token)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    void addAdminReservationFailWhenNotAdmin() {
        AdminReservationRequest request = new AdminReservationRequest(
                LocalDate.of(2024, 6, 22),
                1L,
                1L,
                1L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie("token", token)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
