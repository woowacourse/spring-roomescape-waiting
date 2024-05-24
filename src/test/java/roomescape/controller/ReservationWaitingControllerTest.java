package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.*;
import roomescape.infrastructure.*;
import roomescape.web.auth.JwtProvider;
import roomescape.web.controller.request.ReservationWaitingRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.equalTo;
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

    @DisplayName("로그인 되지 않은 사용자가 예약 대기를 저장한다. -> 400")
    @Test
    void unAuthorizedWaiting() {
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        Theme theme = themeRepository.save(VALID_THEME);
        String date = LocalDate.now().plusMonths(1).toString();
        Member reservedMember = memberRepository.save(new Member(new MemberName("감자"), new MemberEmail("111@aaa.com"), new MemberPassword("asd"), MemberRole.USER));
        reservationRepository.save(new Reservation(reservedMember, new ReservationDate(date), time, theme));

        ReservationWaitingRequest request = new ReservationWaitingRequest(date, time.getId(), theme.getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservation-waitings")
                .then().log().all()
                .statusCode(400);
    }

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

        ReservationWaitingRequest request = new ReservationWaitingRequest(date, time.getId(), theme.getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/reservation-waitings")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("사용자 예약 대기를 삭제한다. -> 204")
    @Test
    void deleteById() {
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        Theme theme = themeRepository.save(VALID_THEME);
        String date = LocalDate.now().plusMonths(1).toString();
        LocalDateTime createdDateTime = LocalDateTime.now().minusMonths(2);
        Member waitingMember = memberRepository.save(new Member(new MemberName("감자"), new MemberEmail("111@aaa.com"), new MemberPassword("asd"), MemberRole.USER));
        ReservationWaiting waiting = reservationWaitingRepository.save(new ReservationWaiting(createdDateTime, waitingMember, new ReservationDate(date), time, theme));
        String token = jwtProvider.createToken(waitingMember.getEmail().getEmail());

        ReservationWaitingRequest request = new ReservationWaitingRequest(date, time.getId(), theme.getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().delete("/reservation-waitings/" + waiting.getId())
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("내 예약 대기 목록을 조회한다. -> 200")
    @Test
    void findAllByMember() {
        ReservationTime time = reservationTimeRepository.save(VALID_RESERVATION_TIME);
        Theme theme = themeRepository.save(VALID_THEME);
        LocalDate date = LocalDate.now().plusMonths(1);
        LocalDateTime createdDateTime = LocalDateTime.now().minusMonths(2);
        Member waitingMember = memberRepository.save(new Member(new MemberName("감자"), new MemberEmail("111@aaa.com"), new MemberPassword("asd"), MemberRole.USER));
        Member otherMember = memberRepository.save(new Member(new MemberName("고구마"), new MemberEmail("222@aaa.com"), new MemberPassword("asd"), MemberRole.USER));
        reservationWaitingRepository.save(new ReservationWaiting(createdDateTime, waitingMember, new ReservationDate(date), time, theme));
        reservationWaitingRepository.save(new ReservationWaiting(createdDateTime, otherMember, new ReservationDate(date.plusMonths(1)), time, theme));
        reservationWaitingRepository.save(new ReservationWaiting(createdDateTime, otherMember, new ReservationDate(date.plusMonths(2)), time, theme));
        String token = jwtProvider.createToken(waitingMember.getEmail().getEmail());

        ReservationWaitingRequest request = new ReservationWaitingRequest(date.toString(), time.getId(), theme.getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().get("/reservation-waitings/mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", equalTo("감자"))
                .body("[0].id", equalTo(1))
                .body("[0].date", equalTo(date.toString()));
    }
}
