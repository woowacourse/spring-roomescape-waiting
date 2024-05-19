package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ThemeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("성공: 최근 일주일 내 인기 테마를 조회한다.")
    @Test
    void findPopular() {
        jdbcTemplate.update("""
            INSERT INTO member(name, email, password, role)
            VALUES ('러너덕', 'user@a.com', '123a!', 'USER');
                        
            INSERT INTO theme(name, description, thumbnail)
            VALUES ('테마1', 'd1', 'https://test.com/test1.jpg'),
                   ('테마2', 'd2', 'https://test.com/test2.jpg'),
                   ('테마3', 'd3', 'https://test.com/test3.jpg'),
                   ('테마4', 'd4', 'https://test.com/test4.jpg'),
                   ('테마5', 'd5', 'https://test.com/test5.jpg');
                   
            INSERT INTO reservation_time(start_at)
            VALUES ('08:00'),
                   ('09:00'),
                   ('10:00'),
                   ('11:00'),
                   ('12:00');
                   
            INSERT INTO reservation(member_id, reserved_date, time_id, theme_id, status)
            VALUES (1, TIMESTAMPADD(DAY, -1, CURRENT_DATE), 1, 3, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -1, CURRENT_DATE), 2, 3, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -1, CURRENT_DATE), 3, 3, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -1, CURRENT_DATE), 4, 3, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -1, CURRENT_DATE), 5, 3, 'RESERVED'),
                   
                   (1, TIMESTAMPADD(DAY, -6, CURRENT_DATE), 1, 2, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -6, CURRENT_DATE), 2, 2, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -6, CURRENT_DATE), 3, 2, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -6, CURRENT_DATE), 4, 2, 'RESERVED'),
                   
                   (1, TIMESTAMPADD(DAY, -7, CURRENT_DATE), 1, 4, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -7, CURRENT_DATE), 2, 4, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -7, CURRENT_DATE), 3, 4, 'RESERVED'),
                   
                   (1, TIMESTAMPADD(DAY, -8, CURRENT_DATE), 1, 5, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -8, CURRENT_DATE), 2, 5, 'RESERVED'),
                   
                   (1, TIMESTAMPADD(DAY, -9, CURRENT_DATE), 1, 1, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -9, CURRENT_DATE), 2, 1, 'RESERVED'),
                   (1, TIMESTAMPADD(DAY, -9, CURRENT_DATE), 3, 1, 'RESERVED')
            """);

        LocalDate start = LocalDate.now().minusDays(8);
        LocalDate end = LocalDate.now().minusDays(1);
        List<Theme> themes = themeRepository.findPopular(start, end, 10);

        assertThat(themes).extracting(Theme::getId).containsExactly(3L, 2L, 4L, 5L, 1L);
    }
}
