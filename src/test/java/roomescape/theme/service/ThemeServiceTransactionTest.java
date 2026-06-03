package roomescape.theme.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.ActiveProfiles;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.service.dto.ThemeCommand;
import roomescape.theme.service.dto.ThemeResult;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = NONE)
public class ThemeServiceTransactionTest {

    @Autowired
    ThemeService themeService;

    @MockitoSpyBean
    ThemeRepository themeRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    void save_rollback() {
        // given
        ThemeCommand command = new ThemeCommand("테스트 테마", "설명", "http://thumbnail.url");

        doThrow(new RuntimeException("저장 중 에러 발생"))
                .when(themeRepository)
                .save(any(Theme.class));

        // when
        assertThatThrownBy(() -> themeService.save(command))
                .isInstanceOf(RuntimeException.class);

        // then
        boolean exists = themeRepository.existsByName(Theme.of("테스트 테마", "설명", "http://thumbnail.url"));
        Assertions.assertFalse(exists);
    }

    @Test
    void delete_rollback() {
        // given
        ThemeCommand command = new ThemeCommand("테스트 테마", "설명", "http://thumbnail.url");
        ThemeResult savedTheme = themeService.save(command);

        doThrow(new RuntimeException("삭제 중 에러 발생"))
                .when(themeRepository)
                .delete(any(Theme.class));

        // when
        assertThatThrownBy(() -> themeService.delete(savedTheme.id()))
                .isInstanceOf(RuntimeException.class);

        // then
        Optional<Theme> result = themeRepository.findById(savedTheme.id());
        Assertions.assertTrue(result.isPresent());
    }
}
