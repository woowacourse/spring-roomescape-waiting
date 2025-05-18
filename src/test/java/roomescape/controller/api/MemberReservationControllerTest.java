package roomescape.controller.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaThemeRepository;
import roomescape.util.JwtTokenProvider;

import java.time.LocalDate;
import java.time.LocalTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberReservationControllerTest {

    @Autowired
    JpaMemberRepository memberRepository;
    @Autowired
    JpaReservationRepository reservationRepository;
    @Autowired
    JpaThemeRepository themeRepository;
    @Autowired
    JpaReservationTimeRepository reservationTimeRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @DisplayName("자신의 예약 정보를 불러올 수 있다")
    @Test
    void myReservationTest() {
        Member member = new Member(null, "가이온","hello@woowa.com", Role.USER,"password");
        ReservationTime time = new ReservationTime(null, LocalTime.now());
        Theme theme = new Theme(null,"테마A","","");
        Reservation reservation = new Reservation(null, member, LocalDate.now() ,time, theme);

        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
        reservationRepository.save(reservation);

        Member findMember = memberRepository.findById(member.getId()).get();
        String token = jwtTokenProvider.createToken(findMember);

        RestAssured.given()
                .cookie("token",token).log().all()
                .when().get("/reservations/me")
                .then().log().all().statusCode(200);
    }
}
