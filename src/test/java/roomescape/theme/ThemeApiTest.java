package roomescape.theme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import roomescape.common.CleanUp;
import roomescape.config.AuthServiceTestConfig;

@Import(AuthServiceTestConfig.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ThemeApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CleanUp cleanUp;

    private Map<String, String> theme;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        theme = Map.of("name", "테마1", "description", "설명1", "thumbnail", "썸네일1");

        cleanUp.all();
    }

    @Test
    void 테마를_생성한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);
    }
}
