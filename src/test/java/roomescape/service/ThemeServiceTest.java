package roomescape.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.theme.ThemeRequest;
import roomescape.repository.ThemeQueryingDao;
import roomescape.repository.ThemeUpdatingDao;
import roomescape.domain.theme.ThemeResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({ThemeService.class, ThemeQueryingDao.class, ThemeUpdatingDao.class})
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Test
    void 테마_생성_성공() {
        ThemeRequest request = new ThemeRequest("인형의 집", "공포 테마", "http://example.com");

        ThemeResponse saved = themeService.create(request);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("인형의 집");
    }

    @Test
    void 전체_테마_조회() {
        themeService.create(new ThemeRequest("무서운 이야기", "공포", "http://example1.com"));
        themeService.create(new ThemeRequest("명탐정의 부재", "탐험", "http://example2.com"));

        List<ThemeResponse> result = themeService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void 테마_삭제() {
        ThemeResponse saved = themeService.create(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));

        themeService.delete(saved.getId());

        assertThat(themeService.findAll()).isEmpty();
    }
}
