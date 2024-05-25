package roomescape.acceptance;

import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

class WaitAcceptanceTest extends AcceptanceFixture {

    @Test
    @DisplayName("예약 대기를 생성한다")
    void generate_new_reservation_wait() {
        // given
        Member member = new Member("name", "email@email.com", "password");
        Theme theme = new Theme("n", "d", "t");
        ReservationTime time = new ReservationTime(LocalTime.of(1, 0));
        Reservation reservation = new Reservation(LocalDate.now().plusDays(2L), time, theme);

        memberRepository.save(member);
        themeRepository.save(theme);
        timeRepository.save(time);
        reservationRepository.save(reservation);

        Map<String, String> requestBody = Map.of("date",
                LocalDate.now().plusDays(2).toString(),
                "timeId", time.getId().toString()
                , "themeId", theme.getId().toString());

        // when
        RestAssured
                .given().contentType(ContentType.JSON).body(requestBody).cookie("token", jwtProvider.encode(member))
                .when().post("/reservation-wait")
                .then().statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    @DisplayName("예약 취소 발생 시 자동으로 최상우 우선순위가 예약상태로 변경된다.")
    void if_reservation_cancellation_then_top_priority_wait_will_be_reservation() {
        // given
        Theme theme = new Theme("name", "desc", "thumb");
        Member member = new Member("fram", "aa@aa.aa", "aa");
        Member member2 = new Member("fram2", "bb@bb.bb", "bb");
        ReservationTime time = new ReservationTime(LocalTime.of(1, 0));

        themeRepository.save(theme);
        memberRepository.save(member);
        memberRepository.save(member2);
        timeRepository.save(time);

        Map<String, String> reservationBody = Map.of("date", LocalDate.now().plusDays(1).toString(),
                "themeId", theme.getId().toString(),
                "memberId", member.getId().toString(),
                "timeId", time.getId().toString());
        String encodedMember = jwtProvider.encode(new Member(1L, "fram", "aa@aa.aa", "aa", Role.NORMAL));

        RestAssured
                .given().contentType(ContentType.JSON).body(reservationBody).cookie("token", encodedMember)
                .when().post("/reservations")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1));

        Map<String, String> requestBody = Map.of("date",
                LocalDate.now().plusDays(1).toString(),
                "timeId", time.getId().toString()
                , "themeId", theme.getId().toString());

        RestAssured
                .given().contentType(ContentType.JSON).body(requestBody).cookie("token", jwtProvider.encode(member2))
                .when().post("/reservation-wait")
                .then().statusCode(HttpStatus.SC_CREATED);

        // when
        RestAssured
                .given().cookie("token", encodedMember)
                .when().delete("/reservation-wait/1")
                .then().statusCode(HttpStatus.SC_NO_CONTENT);

        // then
        RestAssured
                .given().cookie("token", jwtProvider.encode(member2))
                .then().statusCode(HttpStatus.SC_OK)
                .body("[0].status", is("예약"))
                .log().all();
    }
}

