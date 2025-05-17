package roomescape.integrate.domain;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.entity.Member;
import roomescape.global.Role;
import roomescape.jwt.JwtTokenProvider;
import roomescape.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AvailableReservationTimeTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String token;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(new Member("어드민", "test_admin@test.com", "test", Role.ADMIN));
        token = jwtTokenProvider.createTokenByMember(member);
    }

    @Test
    void 예약_가능한_시간을_확인할_수_있다() {
        String todayDateString = LocalDate.now().plusDays(1).toString();

        Map<String, String> timeParam = Map.of(
                "startAt", "20:00"
        );

        Map<String, String> timeParam2 = Map.of(
                "startAt", "21:00"
        );

        Map<String, String> timeParam3 = Map.of(
                "startAt", "22:00"
        );

        Map<String, String> themeParam = Map.of(
                "name", "테마 명",
                "description", "description",
                "thumbnail", "thumbnail"
        );

        long timeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(timeParam)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(timeParam2)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(timeParam3)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        long themeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(themeParam)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        Map<String, Object> reservation = Map.of(
                "date", todayDateString,
                "timeId", timeId,
                "themeId", themeId
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/times/available?date=" + todayDateString + "&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .extract().response();

        List<Boolean> alreadyBooked = response.jsonPath().getList("alreadyBooked", Boolean.class);
        List<Boolean> booleans = List.of(false, false, true);
        assertThat(alreadyBooked).containsAnyElementsOf(booleans);
    }
}


