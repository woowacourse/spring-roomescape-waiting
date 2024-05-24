package roomescape.controller;

import static roomescape.TestFixture.MEMBER1;
import static roomescape.TestFixture.MEMBER1_LOGIN_REQUEST;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import roomescape.TestFixture;
import roomescape.repository.MemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberPageControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("멤버 예약 페이지로 이동한다.")
    @Test
    void responseReservationPage() {
        memberRepository.save(MEMBER1);
        String accessToken = TestFixture.getTokenAfterLogin(MEMBER1_LOGIN_REQUEST);

        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/reservation")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("홈 화면은 로그인하지 않아도 접속할 수 있다.")
    @Test
    void responseMainPage() {
        RestAssured.given().log().all()
                .when().get("")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("현재 로그인된 멤버의 예약 내역 조회 페이지로 이동한다.")
    @Test
    void responseUserReservation() {
        // given
        memberRepository.save(MEMBER1);
        String accessToken = TestFixture.getTokenAfterLogin(MEMBER1_LOGIN_REQUEST);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/reservation-mine")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }
}
