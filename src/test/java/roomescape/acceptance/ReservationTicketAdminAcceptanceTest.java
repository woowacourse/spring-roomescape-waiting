package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.infrastructure.jwt.JjwtJwtTokenProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTicketAdminAcceptanceTest {

    @Autowired
    private JjwtJwtTokenProvider jjwtJwtTokenProvider;

    private String token;

    @BeforeEach
    void setUp() {
        final String email = "example@gmail.com";

        this.token = jjwtJwtTokenProvider.createToken(email);
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 예약 저장을 시도하면 예외가 발생한다.")
    void test1() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("themeId", "1");
        params.put("timeId", "1");
        params.put("memberId", "1");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", "invalidToken")
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("예약이 정상적으로 등록된다.")
    void test2() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("themeId", "1");
        params.put("timeId", "1");
        params.put("memberId", "1");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", this.token)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);
    }
}
