package roomescape.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultTheme;

import io.restassured.RestAssured;
import java.util.List;
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
import roomescape.TestFixture;
import roomescape.auth.JwtTokenProvider;
import roomescape.controller.dto.request.CreatBookingRequest;
import roomescape.controller.dto.response.BookingResponse;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.service.dto.result.MemberResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReservationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ReservationRepository reservationRepository;

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
    @DisplayName("예약 목록을 조회한다")
    void getReservations() {
        // given
        dbHelper.insertReservation(TestFixture.createDefaultReservation_1());
        dbHelper.insertReservation(TestFixture.createDefaultReservation_2());

        // when & then
        List<BookingResponse> responses = given().log().all()
                .when()
                .get("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList(".", BookingResponse.class);

        assertThat(responses).hasSize(2);
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

        CreatBookingRequest request = new CreatBookingRequest(
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
