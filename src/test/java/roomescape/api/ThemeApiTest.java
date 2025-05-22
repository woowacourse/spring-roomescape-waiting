package roomescape.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ThemeRepository;
import roomescape.reservation.infrastructure.TimeSlotRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class ThemeApiTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 테마를_추가한다() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", "테마1");
        params.put("description", "테마1 성공하자");
        params.put("thumbnail", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/api/themes")
                .then().log().all()
                .statusCode(201)
                .body("name", equalTo("테마1"));
    }

    @Test
    void 테마를_전체조회한다() {
        // given
        themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .thumbnail("thumbnail1")
                        .description("description1").build()
        );
        themeRepository.save(
                Theme.builder()
                        .name("theme2")
                        .thumbnail("thumbnail2")
                        .description("description2").build()
        );
        themeRepository.save(
                Theme.builder()
                        .name("theme3")
                        .thumbnail("thumbnail3")
                        .description("description3").build()
        );
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/api/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(3));
    }

    @Test
    void 테마를_삭제한다() {
        // given
        themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .thumbnail("thumbnail1")
                        .description("description1").build()
        );
        Theme savedTheme = themeRepository.save(
                Theme.builder()
                        .name("theme2")
                        .thumbnail("thumbnail2")
                        .description("description2").build()
        );
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/api/themes/{themeId}", savedTheme.getId())
                .then().log().all()
                .statusCode(204);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/api/themes")
                .then().body("size()", is(1));
    }

    @Test
    void 예약이_존재하는_테마를_삭제하는_경우_400에러가_발생한다() {
        // given
        Theme theme = themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .thumbnail("thumbnail1")
                        .description("description1").build()
        );
        TimeSlot time = timeSlotRepository.save(TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build());
        Member member = memberRepository.save(
                Member.builder()
                        .name("member1")
                        .password("password1")
                        .email("email1@domain.com")
                        .role(Role.MEMBER).build()
        );
        reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.now().minusDays(1))
                        .timeSlot(time)
                        .theme(theme).build()
        );
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/api/themes/{themeId}", theme.getId())
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("예약이 존재합니다."));
    }


    @Test
    @Sql(value = "/sql/data.sql")
    void 인기테마_상위10개_조회_테스트() {
        // given
        Member member1 = memberRepository.findById(1L).get();
        Member member2 = memberRepository.findById(2L).get();
        TimeSlot time1 = timeSlotRepository.findById(1L).get();
        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        Theme theme3 = themeRepository.findById(3L).get();

        reservationRepository.save(
                Reservation.builder()
                        .member(member1)
                        .date(LocalDate.now().minusDays(1))
                        .timeSlot(time1)
                        .theme(theme1).build()
        );
        reservationRepository.save(
                Reservation.builder()
                        .member(member1)
                        .date(LocalDate.now().minusDays(2))
                        .timeSlot(time1)
                        .theme(theme1).build()
        );
        reservationRepository.save(
                Reservation.builder()
                        .member(member1)
                        .date(LocalDate.now().minusDays(3))
                        .timeSlot(time1)
                        .theme(theme1).build()
        );

        reservationRepository.save(
                Reservation.builder()
                        .member(member1)
                        .date(LocalDate.now().minusDays(1))
                        .timeSlot(time1)
                        .theme(theme2).build()
        );
        reservationRepository.save(
                Reservation.builder()
                        .member(member1)
                        .date(LocalDate.now().minusDays(2))
                        .timeSlot(time1)
                        .theme(theme2).build()
        );

        reservationRepository.save(
                Reservation.builder()
                        .member(member1)
                        .date(LocalDate.now().minusDays(3))
                        .timeSlot(time1)
                        .theme(theme3).build()
        );
        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/api/themes/rank")
                .then().statusCode(200)
                .log().all()
                .body("size()", is(3))
                .body("[0].name", equalTo("테마1"))
                .body("[1].name", equalTo("테마2"))
                .body("[2].name", equalTo("테마3"));
    }
}
