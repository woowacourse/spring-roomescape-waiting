package roomescape.controller;

import static io.restassured.RestAssured.given;
import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
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
import roomescape.controller.dto.request.CreateBookingRequest;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.dto.result.MemberResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReservationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private DBHelper dbHelper;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @Test
    @DisplayName("예약을 생성한다")
    void createReservation() {
        // given
        Member member = createDefaultMember();
        ReservationTime reservationTime = createDefaultReservationTime();
        Theme theme = createDefaultTheme();
        dbHelper.prepareForBooking(member, reservationTime, theme);

        String token = jwtTokenProvider.createToken(MemberResult.from(member));

        CreateBookingRequest request = new CreateBookingRequest(
                DEFAULT_DATE, reservationTime.getId(), theme.getId()
        );

        // when & then
        given().log().all()
                .cookie("token", token)
                .contentType("application/json")
                .body(request)
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

} 
