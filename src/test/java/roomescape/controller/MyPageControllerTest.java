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
import roomescape.auth.JwtTokenProvider;
import roomescape.controller.dto.response.MemberBookingResponse;
import roomescape.domain.Member;
import roomescape.domain.Waiting;
import roomescape.domain.repository.WaitingRepository;
import roomescape.service.dto.result.MemberResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MyPageControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private WaitingRepository waitingRepository;

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
    @DisplayName("내 예약 목록을 조회한다")
    void getMyReservations() {
        // given
        Member member = createDefaultMember();
        dbHelper.insertReservation(createNewReservation(member, DEFAULT_DATE, createDefaultReservationTime(), createDefaultTheme()));
        dbHelper.insertReservation(createNewReservation(member, DEFAULT_DATE.plusDays(1), createDefaultReservationTime(), createDefaultTheme()));

        String token = jwtTokenProvider.createToken(MemberResult.from(member));

        // when & then
        List<MemberBookingResponse> responses = given().log().all()
                .cookie("token", token)
                .when()
                .get("/mypage/bookings")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath().getList(".", MemberBookingResponse.class);

        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("나의 대기 예약을 취소한다")
    void deleteWaitingReservation() {
        // given
        Waiting waiting = dbHelper.insertWaiting(createDefaultWaiting_1());

        String token = jwtTokenProvider.createToken(MemberResult.from(waiting.getMember()));

        // when & then
        given().log().all()
                .cookie("token", token)
                .when()
                .delete("/mypage/waitings/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(waitingRepository.findById(waiting.getId())).isEmpty();
    }

}
