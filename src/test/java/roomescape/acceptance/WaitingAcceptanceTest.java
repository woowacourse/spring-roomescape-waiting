package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.application.provider.JwtTokenProvider;
import roomescape.dto.LoginMember;
import roomescape.infrastructure.db.MemberJpaRepository;
import roomescape.infrastructure.db.ReservationTicketJpaRepository;
import roomescape.infrastructure.db.ReservationTimeJpaRepository;
import roomescape.infrastructure.db.ThemeJpaRepository;
import roomescape.infrastructure.db.WaitingJpaRepository;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTicket;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;
import roomescape.model.Waiting;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitingAcceptanceTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    private WaitingJpaRepository waitingJpaRepository;

    @Autowired
    private ThemeJpaRepository themeJpaRepository;
    @Autowired
    private ReservationTimeJpaRepository reservationTimeJpaRepository;
    @Autowired
    private ReservationTicketJpaRepository reservationTicketJpaRepository;

    @Test
    @DisplayName("웨이팅을 등록할 수 있다")
    void test1() {
        Member member = memberJpaRepository.save(new Member("name", "email@gmail.com", "password", Role.ADMIN));
        LoginMember loginMember = new LoginMember(member);

        Theme theme = themeJpaRepository.save(new Theme("새로운 테마", "새로운 설명", "썸네일"));
        ReservationTime reservationTime = reservationTimeJpaRepository.save(
                new ReservationTime(LocalTime.of(12, 30)));

        LocalDate date = LocalDate.now().plusDays(1);

        ReservationTicket reservationTicket = reservationTicketJpaRepository.save(
                new ReservationTicket(new Reservation(
                        date,
                        reservationTime,
                        theme,
                        member,
                        LocalDate.now()
                )));

        Map<String, String> params = new HashMap<>();
        params.put("theme", theme.getId().toString());
        params.put("time", reservationTime.getId().toString());
        params.put("date", String.valueOf(date));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", jwtTokenProvider.createToken(member.getEmail()))
                .body(params)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("웨이팅을 삭제할 수 있다")
    void test2() {
        // given
        Member member = memberJpaRepository.save(new Member("name", "email@gmail.com", "password", Role.ADMIN));
        Theme theme = themeJpaRepository.save(new Theme("새로운 테마", "설명", "썸네일"));
        ReservationTime reservationTime = reservationTimeJpaRepository.save(new ReservationTime(LocalTime.of(12, 30)));

        Waiting waiting = waitingJpaRepository.save(
                new Waiting(
                        LocalDateTime.now(),
                        new Reservation(LocalDate.now().plusDays(1), reservationTime, theme, member,
                                LocalDate.now())
                )
        );

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", jwtTokenProvider.createToken(member.getEmail()))
                .when().delete("/waiting/" + waiting.getId())
                .then().log().all()
                .statusCode(204);
    }

}
