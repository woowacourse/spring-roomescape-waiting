package roomescape.theme.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.theme.domain.Theme;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 지난_7일간_예약_수_기준_인기_테마를_limit_개수만큼_조회한다() {
        // given
        int limit = 2;

        // when
        List<Theme> result = themeRepository.findByRank(limit);

        // then
        // 예약 수 기준 1순위는 '라라랜드'(theme_id = 2), 2순위는 '인터스텔라'(theme_id = 1)
        assertThat(result).hasSize(limit);
        assertThat(result.get(0).getName()).isEqualTo("라라랜드");
        assertThat(result.get(1).getName()).isEqualTo("인터스텔라");
    }
}
