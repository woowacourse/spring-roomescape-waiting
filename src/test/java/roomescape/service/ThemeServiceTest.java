package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.exception.BadRequestException;
import roomescape.service.dto.request.ThemeCreateRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("이미 같은 이름의 테마가 존재하면 추가할 수 없다.")
    @Test
    void cantSaveWhenSameNameExists() {
        // given
        String name = "테마";
        ThemeCreateRequest request = new ThemeCreateRequest(name, "테마 설명1", "테마 썸네일");
        themeService.save(request);

        // when
        ThemeCreateRequest invalidRequest = new ThemeCreateRequest(name, "테마 설명2", "테마 썸네일1");

        // then
        assertThatThrownBy(() -> themeService.save(invalidRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 존재하는 테마 이름입니다.(" + name + ")");
    }
}
