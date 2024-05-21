package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.dto.response.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.config.location=classpath:/application.properties"})
class ClientRankTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("랭크를 조회하면 현재 날짜 기준 일주일 동안의 인기 테마를 확인할 수 있다.")
    @Test
    void given_when_ranks_then_statusCodeIsOk() {
        List<ThemeResponse> themeResponses = RestAssured.given().log().all()
                .when().get("/ranks")
                .then().extract().body()
                .jsonPath().getList("", ThemeResponse.class);

        ThemeResponse actual_first = themeResponses.get(0);

        assertThat(actual_first.id()).isEqualTo(3L);
    }
}
