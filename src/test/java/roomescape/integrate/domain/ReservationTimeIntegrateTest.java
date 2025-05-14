package roomescape.integrate.domain;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTimeIntegrateTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    private String token;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(new Member("어드민", "test_admin@test.com", "test", Role.ADMIN));
        token = jwtTokenProvider.createTokenByMember(member);
    }

    @Test
    void 시간_추가_테스트() {
        Map<String, String> timeParam = Map.of(
                "startAt", "12:35"
        );

        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("token", token)
            .body(timeParam)
            .when().post("/times")
            .then().log().all()
            .statusCode(201)
            .extract().jsonPath().getLong("id");
    }

    @Test
    void 시간_삭제_테스트() {
        Map<String, String> timeParam = Map.of(
                "startAt", "12:34"
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
                .cookie("token", token)
                .when().delete("/times/" + timeId)
                .then().log().all()
                .statusCode(204);
    }
}
