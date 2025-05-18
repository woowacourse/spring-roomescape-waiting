package roomescape.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultTheme;
import static roomescape.TestFixture.createDefaultWaiting_1;
import static roomescape.TestFixture.createNewReservation;

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
import roomescape.controller.request.CreateReservationRequest;
import roomescape.controller.response.MemberReservationResponse;
import roomescape.controller.response.ReservationResponse;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.service.result.MemberResult;

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
        List<ReservationResponse> responses = given().log().all()
                .when()
                .get("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().jsonPath().getList(".", ReservationResponse.class);

        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("예약을 생성한다")
    void createReservation() {
        // given
        Member member = createDefaultMember();
        ReservationTime reservationTime = createDefaultReservationTime();
        Theme theme = createDefaultTheme();
        dbHelper.prepareForReservation(member, reservationTime, theme);

        String token = jwtTokenProvider.createToken(MemberResult.from(member));

        CreateReservationRequest request = new CreateReservationRequest(
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

    @Test
    @DisplayName("대기 예약을 생성한다")
    void createWaitingReservation() {
        // given
        Member member = createDefaultMember();
        ReservationTime reservationTime = createDefaultReservationTime();
        Theme theme = createDefaultTheme();
        dbHelper.prepareForReservation(member, reservationTime, theme);

        String token = jwtTokenProvider.createToken(MemberResult.from(member));

        CreateReservationRequest request = new CreateReservationRequest(
                DEFAULT_DATE, reservationTime.getId(), theme.getId()
        );

        // when & then
        given().log().all()
                .cookie("token", token)
                .contentType("application/json")
                .body(request)
                .when()
                .post("/reservations/waitings")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        Reservation saved = reservationRepository.findById(1L).get();
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    @DisplayName("대기 예약을 취소한다")
    void deleteWaitingReservation() {
        // given
        Reservation waiting = createDefaultWaiting_1();
        dbHelper.insertReservation(waiting);

        String token = jwtTokenProvider.createToken(MemberResult.from(waiting.getMember()));

        // when & then
        given().log().all()
                .cookie("token", token)
                .when()
                .delete("/reservations/waitings/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(reservationRepository.findById(1L)).isEmpty();
    }

    @Test
    @DisplayName("내 예약 목록을 조회한다")
    void getMyReservations() {
        // given
        Member member = createDefaultMember();
        dbHelper.insertReservation(createNewReservation(member, DEFAULT_DATE, createDefaultReservationTime(), createDefaultTheme()));
        dbHelper.insertReservation(createNewReservation(member, DEFAULT_DATE.plusDays(1), createDefaultReservationTime(), createDefaultTheme()));

        String token = jwtTokenProvider.createToken(MemberResult.from(member));

        // when & then
        List<MemberReservationResponse> responses = given().log().all()
                .cookie("token", token)
                .when()
                .get("/reservations/mine")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath().getList(".", MemberReservationResponse.class);

        assertThat(responses).hasSize(2);
    }
} 
