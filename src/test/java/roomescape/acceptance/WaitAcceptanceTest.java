package roomescape.acceptance;

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
}
