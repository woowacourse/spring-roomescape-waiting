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
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class ThemeApiTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

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
        themeRepository.save(Theme.createWithoutId("theme1", "desc", "thumb1"));
        themeRepository.save(Theme.createWithoutId("theme2", "desc", "thumb2"));
        themeRepository.save(Theme.createWithoutId("theme3", "desc", "thumb3"));
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
        themeRepository.save(Theme.createWithoutId("theme1", "desc", "thumb1"));
        Theme savedTheme = themeRepository.save(Theme.createWithoutId("theme2", "desc", "thumb2"));
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
        Theme theme = themeRepository.save(Theme.createWithoutId("theme3", "desc", "thumb3"));
        ReservationTime time = timeRepository.save(ReservationTime.createWithoutId(LocalTime.of(9, 0)));
        Member member = memberRepository.save(new Member(null, "name1", "email@domain.com", "password1", Role.MEMBER));
        reservationRepository.save(Reservation.createWithoutId(member, LocalDate.now().minusDays(1), time, theme));
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
        ReservationTime time1 = timeRepository.findById(1L).get();
        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        Theme theme3 = themeRepository.findById(3L).get();

        reservationRepository.save(Reservation.createWithoutId(member1, LocalDate.now().minusDays(1), time1, theme1));
        reservationRepository.save(Reservation.createWithoutId(member1, LocalDate.now().minusDays(2), time1, theme1));
        reservationRepository.save(Reservation.createWithoutId(member1, LocalDate.now().minusDays(3), time1, theme1));

        reservationRepository.save(Reservation.createWithoutId(member1, LocalDate.now().minusDays(1), time1, theme2));
        reservationRepository.save(Reservation.createWithoutId(member1, LocalDate.now().minusDays(2), time1, theme2));

        reservationRepository.save(Reservation.createWithoutId(member1, LocalDate.now().minusDays(3), time1, theme3));
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
