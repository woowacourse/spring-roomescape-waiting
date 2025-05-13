package roomescape.theme.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.global.exception.impl.BadRequestException;
import roomescape.theme.presentation.request.ThemeRequest;
import roomescape.theme.presentation.response.ThemeResponse;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 테마를_모두_조회한다() {
        // given

        // when & then
        assertThat(themeService.findAll()).hasSize(3);
    }

    @Test
    void 테마를_생성한다() {
        // given
        final ThemeRequest request = new ThemeRequest("아이언맨", "", "");

        // when & then
        assertThat(themeService.add(request)).isEqualTo(new ThemeResponse(4L, "아이언맨", "", ""));
    }

    @Test
    void id로_테마를_삭제한다() {
        // given
        final ThemeRequest request = new ThemeRequest("아이언맨", "", "");
        themeService.add(request);

        // when
        final Long id = 4L;

        // then
        assertThatCode(() -> themeService.deleteById(id)).doesNotThrowAnyException();
    }

    @Test
    void 사용중인_테마를_삭제하면_에러를_발생시킨다() {
        // given
        final Long id = 1L;

        // when & then
        assertThatThrownBy(() -> themeService.deleteById(id))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이 테마의 예약이 존재합니다.");
    }

    @Test
    void 많이_예약된_순서로_테마를_조회한다() {
        // given

        // when & then
        assertThat(themeService.sortByRank())
                .extracting(ThemeResponse::id)
                .containsExactly(2L, 1L, 3L);

    }
}
