package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.config.DatabaseCleaner;
import roomescape.reservation.dto.ThemeCreateRequest;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ThemeServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ThemeService themeService;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
    }

    @Test
    @DisplayName("중복된 테마 이름을 추가할 수 없다.")
    void duplicateThemeNameExceptionTest() {
        ThemeCreateRequest theme1 = new ThemeCreateRequest("공포", "무서운 테마", "https://ab.com/1x.png");
        themeService.save(theme1);

        ThemeCreateRequest theme2 = new ThemeCreateRequest("공포", "무서움", "https://cd.com/2x.jpg");
        assertThatThrownBy(() -> themeService.save(theme2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("테마 아이디로 조회 시 존재하지 않는 아이디면 예외가 발생한다.")
    void findByIdExceptionTest() {
        assertThatThrownBy(() -> themeService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
