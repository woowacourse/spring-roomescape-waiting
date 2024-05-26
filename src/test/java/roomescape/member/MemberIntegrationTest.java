package roomescape.member;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.util.IntegrationTest;

class MemberIntegrationTest extends IntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @LocalServerPort
    private int port;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;

    @BeforeEach
    void init() {
        RestAssured.port = this.port;
    }

    private String getTokenByLogin() {
        return RestAssured
                .given().log().all()
                .body(new LoginRequest("login@naver.com", "hihi"))
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @Test
    @DisplayName("회원 목록을 조회한다.")
    void getReservationTimes() {
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));
        memberRepository.save(new Member("로키", Role.USER, "qwer@naver.com", "hihi"));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/members")
                .then().log().all()

                .statusCode(200)
                .body("id", hasItems(1, 2))
                .body("size()", equalTo(2));
    }

    @Test
    @DisplayName("회원의 예약 목록을 조회한다.")
    void getMembersWithReservations() {
        Member member1 = memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));
        Member member2 = memberRepository.save(new Member("로키", Role.USER, "qwer@naver.com", "hihi"));
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        List<Theme> themes = ThemeFixture.get(3).stream().map(themeRepository::save).toList();
        reservationRepository.save(
                ReservationFixture.getOneWithMemberTimeTheme(member1, reservationTime, themes.get(0)));
        reservationRepository.save(
                ReservationFixture.getOneWithMemberTimeTheme(member1, reservationTime, themes.get(1)));
        reservationRepository.save(
                ReservationFixture.getOneWithMemberTimeTheme(member2, reservationTime, themes.get(2)));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .when().get("/members/reservations")
                .then().log().all()

                .statusCode(200)
                .body("reservationId", hasItems(1, 2))
                .body("size()", equalTo(2));
    }
}
