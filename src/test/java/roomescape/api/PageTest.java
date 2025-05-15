package roomescape.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.presentation.PageController;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PageTest {

    @Autowired
    private PageController pageController;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 예약_화면_요청을_성공한다() {
        RestAssured.given().log().all()
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);

        assertThat(pageController).isNotNull();
    }

    @Test
    void 예약_시간_화면_요청을_성공한다() {
        Member admin = memberRepository.save(
                new Member(null, "admin", "admin@domain.com", "password1", Role.ADMIN)
        );
        Map<String, Object> body = Map.of(
                "email", admin.getEmail(),
                "password", admin.getPassword()
        );
        String token = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().cookie("token");

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 관리자가_아닌_사용자가_예약시간_화면_요청_시_리다이렉트된다() {
        Member member = memberRepository.save(
                new Member(null, "member1", "member1@domain.com", "password1", Role.MEMBER)
        );
        Map<String, Object> body = Map.of(
                "email", member.getEmail(),
                "password", member.getPassword()
        );
        String token = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().cookie("token");

        RestAssured.given().log().all()
                .cookie("token", token)
                .redirects().follow(false)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(302);

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }


    @Test
    void 사용자_예약_화면_요청을_성공한다() {
        RestAssured.given().log().all()
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 인기테마_화면_요청을_성공한다() {
        RestAssured.given().log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 어드민_홈_화면_요청을_성공한다() {
        Member admin = memberRepository.save(
                new Member(null, "admin", "admin@domain.com", "password1", Role.ADMIN)
        );
        Map<String, Object> body = Map.of(
                "email", admin.getEmail(),
                "password", admin.getPassword()
        );
        String token = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().cookie("token");

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 관리자가_아닌_사용자가_어드민_홈_화면_요청_시_리다이렉트된다() {
        Member member = memberRepository.save(
                new Member(null, "member1", "member1@domain.com", "password1", Role.MEMBER)
        );
        Map<String, Object> body = Map.of(
                "email", member.getEmail(),
                "password", member.getPassword()
        );
        String token = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().cookie("token");

        RestAssured.given().log().all()
                .cookie("token", token)
                .redirects().follow(false)
                .when().get("/admin")
                .then().log().all()
                .statusCode(302);

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }
}
