package roomescape.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createAdminMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultReservation_1;
import static roomescape.TestFixture.createDefaultTheme;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import roomescape.DBHelper;
import roomescape.DatabaseCleaner;
import roomescape.auth.JwtTokenProvider;
import roomescape.controller.request.CreateReservationAdminRequest;
import roomescape.controller.response.BookingResponse;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.service.result.MemberResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminReservationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DBHelper dbHelper;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @Test
    @DisplayName("관리자가 예약을 생성한다")
    void createReservation() {
        // given
        Member admin = createAdminMember();
        ReservationTime reservationTime = createDefaultReservationTime();
        Theme theme = createDefaultTheme();
        dbHelper.prepareForReservation(admin, reservationTime, theme);

        String token = jwtTokenProvider.createToken(MemberResult.from(admin));

        CreateReservationAdminRequest request = new CreateReservationAdminRequest(
                DEFAULT_DATE,
                theme.getId(),
                reservationTime.getId(),
                admin.getId()
        );

        // when & then
        BookingResponse response = given().log().all()
                .cookie("token", token)
                .contentType("application/json")
                .body(request)
                .when()
                .post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(BookingResponse.class);

        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.memberName()).isEqualTo(admin.getName())
        );
    }

    @Test
    @DisplayName("관리자가 예약을 삭제한다")
    void deleteReservation() {
        // given
        Member admin = createAdminMember();
        dbHelper.insertMember(admin);
        String token = jwtTokenProvider.createToken(MemberResult.from(admin));
        Reservation reservation = createDefaultReservation_1();
        dbHelper.insertReservation(reservation);

        // when & then
        given().log().all()
                .cookie("token", token)
                .when()
                .delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(reservationRepository.findById(1L)).isEmpty();
    }
} 
