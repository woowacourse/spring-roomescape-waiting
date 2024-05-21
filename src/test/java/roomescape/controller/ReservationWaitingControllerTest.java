package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.*;
import roomescape.infrastructure.*;
import roomescape.web.auth.JwtProvider;
import roomescape.web.controller.request.MemberWaitingWebRequest;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static roomescape.Fixture.VALID_RESERVATION_TIME;
import static roomescape.Fixture.VALID_THEME;

public class ReservationWaitingControllerTest extends ControllerTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @DisplayName("사용자 예약 대기를 저장한다. -> 201")
    @Test
    void waiting() {
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        Theme theme = themeRepository.save(VALID_THEME);
        String date = LocalDate.now().plusMonths(1).toString();
        Member reservedMember = memberRepository.save(new Member(new MemberName("감자"), new MemberEmail("111@aaa.com"), new MemberPassword("asd"), MemberRole.USER));
        Member waitingMember = memberRepository.save(new Member(new MemberName("고구마"), new MemberEmail("222@aaa.com"), new MemberPassword("asd"), MemberRole.USER));
        reservationRepository.save(new Reservation(reservedMember, new ReservationDate(date), time, theme));
        String token = jwtProvider.createToken(waitingMember.getEmail().getEmail());

        MemberWaitingWebRequest request = new MemberWaitingWebRequest(date, time.getId(), theme.getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("사용자 예약 대기를 삭제한다. -> 204")
    @Test
    void deleteById() {
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        Theme theme = themeRepository.save(VALID_THEME);
        String date = LocalDate.now().plusMonths(1).toString();
        Member waitingMember = memberRepository.save(new Member(new MemberName("감자"), new MemberEmail("111@aaa.com"), new MemberPassword("asd"), MemberRole.USER));
        ReservationWaiting waiting = reservationWaitingRepository.save(new ReservationWaiting(waitingMember, new ReservationDate(date), time, theme));
        String token = jwtProvider.createToken(waitingMember.getEmail().getEmail());

        MemberWaitingWebRequest request = new MemberWaitingWebRequest(date, time.getId(), theme.getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().delete("/waitings/" + waiting.getId())
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("사용자 예약 대기를 조회한다. -> 200")
    @Test
    void findAllByMember() {
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        Theme theme = themeRepository.save(VALID_THEME);
        String date = LocalDate.now().plusMonths(1).toString();
        Member waitingMember = memberRepository.save(new Member(new MemberName("감자"), new MemberEmail("111@aaa.com"), new MemberPassword("asd"), MemberRole.USER));
        reservationWaitingRepository.save(new ReservationWaiting(waitingMember, new ReservationDate(date), time, theme));
        String token = jwtProvider.createToken(waitingMember.getEmail().getEmail());

        MemberWaitingWebRequest request = new MemberWaitingWebRequest(date, time.getId(), theme.getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().get("/waitings/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }
}
