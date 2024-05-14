package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.util.DatabaseCleaner;

import java.util.HashMap;
import java.util.Map;

//@formatter:off
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberApiControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.initialize();
    }

    @Test
    @DisplayName("멤버 생성에 성공하면, 201을 반환한다")
    void return_200_when_member_create_success(){
        final Map<String, Object> member = new HashMap<>();
        member.put("name", "조이썬");
        member.put("email", "i894@naver.com");
        member.put("password", "password");

        RestAssured.given().contentType(ContentType.JSON).body(member)
                   .when().post("/members")
                   .then().statusCode(201);
    }

}
