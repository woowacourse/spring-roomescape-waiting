package roomescape.theme.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exception.BadRequestException;
import roomescape.theme.dto.ThemeCreateRequest;
import roomescape.theme.dto.ThemeResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(value = {ThemeService.class})
@Sql(value = "/recreate_table.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("테마 서비스")
class ThemeServiceTest {

    private final ThemeService themeService;

    @Autowired
    public ThemeServiceTest(ThemeService themeService) {
        this.themeService = themeService;
    }

    @DisplayName("테마 서비스는 테마를 생성한다.")
    @Test
    void createTheme() {
        // given
        String name = "새로운 테마";
        String description = "완전 새로운 테마";
        String thumbnail = "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg";
        ThemeCreateRequest request = new ThemeCreateRequest(name, description, thumbnail);

        // when
        ThemeResponse actual = themeService.createTheme(request);

        // then
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(actual.name()).isEqualTo(name);
        softAssertions.assertThat(actual.description()).isEqualTo(description);
        softAssertions.assertThat(actual.thumbnail()).isEqualTo(thumbnail);
        softAssertions.assertAll();
    }

    @DisplayName("테마 서비스는 테마 생성 시 중복된 이름이 들어올 경우 예외가 발생한다.")
    @Test
    void validateDuplicated() {
        // given
        ThemeCreateRequest request = new ThemeCreateRequest(
                "공포", "공포스러운 테마", "http://example.org"
        );

        // when & then
        assertThatThrownBy(() -> themeService.createTheme(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("중복된 테마 이름입니다.");
    }

    @DisplayName("테마 서비스는 모든 테마를 조회한다.")
    @Test
    void findAll() {
        // when
        List<ThemeResponse> themeResponses = themeService.readThemes();

        // then
        assertThat(themeResponses).hasSize(3);
    }

    @DisplayName("테마 서비스는 최근 일주일 간의 인기 있는 테마를 조회힌다.")
    @Test
    void readPopularThemes() {
        // given
        List<Long> expected = List.of(2L, 1L);

        // when
        List<ThemeResponse> popularThemes = themeService.readPopularThemes();
        List<Long> actual = popularThemes.stream()
                .mapToLong(ThemeResponse::id)
                .boxed()
                .toList();

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("테마 서비스는 id에 해당하는 테마를 삭제한다.")
    @Test
    void delete() {
        // given
        Long id = 3L;

        // when & then
        assertThatCode(() -> themeService.deleteTheme(id))
                .doesNotThrowAnyException();
    }

    @DisplayName("테마 서비스는 id에 해당하는 테마 삭제 시 예약이 있는 경우 예외가 발생한다.")
    @Test
    void deleteThemeWithExistsReservation() {
        // given
        Long id = 1L;

        // when & then
        assertThatThrownBy(() -> themeService.deleteTheme(id))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("해당 테마에 예약이 존재합니다.");
    }
}
