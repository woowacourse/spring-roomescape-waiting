package roomescape.controller.view;

import static roomescape.TestFixture.ADMIN_ZEZE;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import roomescape.domain.Member;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.RoomThemeRepository;
import roomescape.service.dto.request.LoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminViewControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private RoomThemeRepository roomThemeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        clearTable();
    }

    @DisplayName("Admin Page 홈화면 접근 성공 테스트")
    @Test
    void responseAdminPage() {
        // given
        String accessToken = getTokenByLoginRequest();

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/admin")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("Admin Reservation Page 접근 성공 테스트")
    @Test
    void responseAdminReservationPage() {
        // given
        String accessToken = getTokenByLoginRequest();

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/admin/reservation")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("Admin Time Page 접근 성공 테스트")
    @Test
    void responseAdminTimePage() {
        // given
        String accessToken = getTokenByLoginRequest();

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/admin/time")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("Admin Theme Page 접근 성공 테스트")
    @Test
    void responseAdminThemePage() {
        // given
        String accessToken = getTokenByLoginRequest();

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/admin/theme")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    private void clearTable() {
        reservationRepository.findAll()
                .forEach(reservation -> reservationRepository.deleteById(reservation.getId()));
        reservationTimeRepository.findAll()
                .forEach(reservationTime -> reservationTimeRepository.deleteById(reservationTime.getId()));
        roomThemeRepository.findAll()
                .forEach(roomTheme -> roomThemeRepository.deleteById(roomTheme.getId()));
        memberRepository.findAll()
                .forEach(member -> memberRepository.deleteById(member.getId()));
    }

    private String getTokenByLoginRequest() {
        Member member = memberRepository.save(ADMIN_ZEZE);

        return RestAssured
                .given().log().all()
                .body(new LoginRequest(member.getEmail(), member.getPassword()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];
    }
}
