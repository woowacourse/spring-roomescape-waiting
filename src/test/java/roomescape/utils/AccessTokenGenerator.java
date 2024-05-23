package roomescape.utils;

import io.restassured.RestAssured;
import org.springframework.http.MediaType;
import roomescape.core.dto.auth.TokenRequest;

public class AccessTokenGenerator {
    private static final String EMAIL = TestFixture.getEmail();
    private static final String PASSWORD = TestFixture.getPassword();

    public static String generate() {
        return RestAssured
                .given().log().all()
                .body(new TokenRequest(EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }
}
