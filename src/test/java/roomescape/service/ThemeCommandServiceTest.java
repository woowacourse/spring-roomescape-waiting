package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.repository.ThemeDao;

@SpringBootTest
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ThemeCommandServiceTest {

    // reservation-fixture.sql 기준:
    // theme1(공포의 저택): 예약이 참조 → 삭제 불가
    // theme2(예약없는테마): 참조 없음 → 삭제 가능

    @Autowired
    private ThemeCommandService themeCommandService;

    @Autowired
    private ThemeDao themeDao;

    @Test
    @DisplayName("예약이 참조하지 않는 테마는 삭제할 수 있다.")
    void delete_success() {
        assertThatCode(() -> themeCommandService.delete(2L))
                .doesNotThrowAnyException();

        assertThat(themeDao.findById(2L)).isEmpty();
    }

    @Test
    @DisplayName("예약이 참조하는 테마는 삭제할 수 없다.")
    void delete_withReservation() {
        assertThatThrownBy(() -> themeCommandService.delete(1L))
                .isInstanceOf(DeletionNotAllowedException.class);
    }

    @Test
    @DisplayName("테마 생성에 성공한다.")
    void create_success() {
        Theme created = themeCommandService.create("새테마", "url", "설명");

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("새테마");
        assertThat(created.thumbnailUrl()).isEqualTo("url");
        assertThat(created.description()).isEqualTo("설명");
    }
}
