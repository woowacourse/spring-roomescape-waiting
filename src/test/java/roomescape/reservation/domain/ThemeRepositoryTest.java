package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.fixture.ThemeFixture;
import roomescape.reservation.domain.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    ThemeRepository themeRepository;

    @DisplayName("인기 테마를 조회한다.")
    @Test
    void findPopularThemes() {
        //given
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now().plusMonths(2);

        //when & then
        List<Theme> popularThemes = themeRepository.findPopularThemes(startDate, endDate, 7);
        assertThat(popularThemes).containsExactly(ThemeFixture.getTheme2());
    }
}
