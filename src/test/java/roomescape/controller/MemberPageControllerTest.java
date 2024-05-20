package roomescape.controller;

import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import roomescape.TestFixture;
import roomescape.domain.Member;
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
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            memberRepository.deleteById(member.getId());
        }
    }

    @DisplayName("Reservation Page 접근 성공 테스트")
    @Test
    void responseReservationPage() {
        String accessToken = TestFixture.getMemberToken(memberRepository);

        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/reservation")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("메인 페이지 접근 성공 테스트")
    @Test
    void responseMainPage() {
        RestAssured.given().log().all()
                .when().get("")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("회원 예약 페이지 접근 성공 테스트")
    @Test
    void responseUserReservation() {
        String accessToken = TestFixture.getMemberToken(memberRepository);

        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/reservation-mine")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }
}
