package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.theme.Theme;
import roomescape.global.exception.RoomescapeException;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ThemeServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeService themeService;

    private final String name = "테마1";
    private final String description = "테마1에 대한 설명입니다.";
    private final String thumbnail = "https://test.com/test.jpg";

    @DisplayName("성공: 테마 추가")
    @Test
    void save() {
        Theme save = themeService.save(name, description, thumbnail);
        assertThat(save.getId()).isEqualTo(1L);
    }

    @DisplayName("성공: 테마 삭제")
    @Test
    void delete() {
        jdbcTemplate.update("""
            INSERT INTO theme(name, description, thumbnail)
            VALUES ('테마1', 'd1', 'https://test.com/test1.jpg'),
                   ('테마2', 'd2', 'https://test.com/test2.jpg'),
                   ('테마3', 'd3', 'https://test.com/test3.jpg');
            """);

        themeService.delete(2L);
        assertThat(themeService.findAll()).extracting(Theme::getId).containsExactly(1L, 3L);
    }

    @DisplayName("성공: 전체 테마 조회")
    @Test
    void findAll() {
        jdbcTemplate.update("""
            INSERT INTO theme(name, description, thumbnail)
            VALUES ('테마1', 'd1', 'https://test.com/test1.jpg'),
                   ('테마2', 'd2', 'https://test.com/test2.jpg'),
                   ('테마3', 'd3', 'https://test.com/test3.jpg');
            """);

        List<Theme> themes = themeService.findAll();
        assertThat(themes).hasSize(3);
    }

    @DisplayName("실패: 이름이 동일한 방탈출 테마를 저장하면 예외 발생")
    @Test
    void save_DuplicatedName() {
        themeService.save(name, description, thumbnail);

        assertThatThrownBy(() -> themeService.save(name, "description", "https://new.com/new.jpg"))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("같은 이름의 테마가 이미 존재합니다.");
    }

    @DisplayName("실패: 예약에 사용되는 테마 삭제 시도 시 예외 발생")
    @Test
    void delete_ReservationExists() {
        jdbcTemplate.update("""
            INSERT INTO member(name, email, password, role)
            VALUES ('러너덕', 'user@a.com', '123a!', 'USER');
                        
            INSERT INTO theme(name, description, thumbnail)
            VALUES ('테마1', 'd1', 'https://test.com/test1.jpg');
                        
            INSERT INTO reservation_time(start_at)
            VALUES ('08:00');
                        
            INSERT INTO reservation(member_id, reserved_date, time_id, theme_id, status)
            VALUES (1, '2060-01-01', 1, 1, 'RESERVED');
            """);

        assertThatThrownBy(() -> themeService.delete(1L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("해당 테마를 사용하는 예약이 존재하여 삭제할 수 없습니다.");
    }
}
