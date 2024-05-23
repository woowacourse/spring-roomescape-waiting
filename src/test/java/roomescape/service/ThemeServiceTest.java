package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.Theme;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ThemeCreateRequest;
import roomescape.service.dto.response.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;
    @Autowired
    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        List<Theme> themes = themeRepository.findAll();
        for (Theme theme : themes) {
            themeRepository.deleteById(theme.getId());
        }
    }

    @DisplayName("테마 저장")
    @Test
    void save() {
        // given
        ThemeCreateRequest themeCreateRequest = new ThemeCreateRequest("레벨2 탈출",
                "우테코 레벨2",
                "https://i.pinimg.com/236x/6e");
        // when
        ThemeResponse themeResponse = themeService.save(themeCreateRequest);
        // then
        assertAll(
                () -> assertThat(themeResponse.name()).isEqualTo(themeCreateRequest.name()),
                () -> assertThat(themeResponse.description()).isEqualTo(
                        themeCreateRequest.description()),
                () -> assertThat(themeResponse.thumbnail()).isEqualTo(
                        themeCreateRequest.thumbnail())
        );
    }

    @DisplayName("모든 테마 조회")
    @Test
    void findAll() {
        // given & when
        List<ThemeResponse> themeRespons = themeService.findAll();
        // then
        assertThat(themeRespons).isEmpty();
    }

    @DisplayName("테마 삭제")
    @Test
    void delete() {
        // given
        ThemeCreateRequest themeCreateRequest = new ThemeCreateRequest("레벨2 탈출",
                "우테코 레벨2",
                "https://i.pinimg.com/236x/6e");
        ThemeResponse themeResponse = themeService.save(themeCreateRequest);
        // when
        themeService.deleteById(themeResponse.id());
        // then
        assertThat(themeService.findAll()).isEmpty();
    }
}
