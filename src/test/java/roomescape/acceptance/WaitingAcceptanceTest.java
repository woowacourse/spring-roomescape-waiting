package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.application.provider.JwtTokenProvider;
import roomescape.infrastructure.db.MemberJpaRepository;
import roomescape.model.Member;
import roomescape.model.Role;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitingAcceptanceTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("웨이팅을 등록할 수 있다")
    void test1() {
        Member member = memberJpaRepository.save(new Member("name", "email@gmail.com", "password", Role.ADMIN));

        Map<String, String> params = new HashMap<>();
        params.put("theme", "1");
        params.put("time", "1");
        params.put("date", String.valueOf(LocalDate.now().plusDays(1)));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", jwtTokenProvider.createToken(member.getEmail()))
                .body(params)
                .when().post("/waiting")
                .then().log().all()
                .statusCode(201);
    }

}
