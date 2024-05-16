package roomescape.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.Theme;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CustomThemeRepositoryTest {

    @Autowired
    private CustomThemeRepository customThemeRepository;

    @DisplayName("특정 기간 중 가장 예약 개수가 많은 상위 10개의 테마 정보를 인기순으로 조회한다.")
    @Test
    void findPopularThemes() {
        // When
        final ReservationDate startAt = new ReservationDate(LocalDate.now().minusDays(7));
        final ReservationDate endAt = new ReservationDate(LocalDate.now().minusDays(1));
        final int maximumThemeCount = 10;

        final List<Theme> popularThemes = customThemeRepository.findPopularThemes(startAt, endAt, maximumThemeCount);

        // Then
        assertAll(
                () -> assertThat(popularThemes.size()).isLessThanOrEqualTo(10),
                () -> assertThat(popularThemes.get(0).getId()).isEqualTo(1),
                () -> assertThat(popularThemes.get(1).getId()).isEqualTo(2),
                () -> assertThat(popularThemes.get(2).getId()).isEqualTo(10)
        );
    }
}
