package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    // theme 3의 예약 2건, theme 1의 예약 1건
    @DisplayName("현재 날짜를 기준으로 일주일 동안 예약이 가장 많았던 순으로 정렬된 테마를 지정한 개수만큼 조회한다.")
    @Test
    void findThemesWithReservationsBetweenDates() {
        //given
        LocalDate fromDate = LocalDate.of(2024, 5, 18);
        LocalDate toDate = LocalDate.of(2024, 5, 20);
        Pageable pageable = PageRequest.of(0, 2);

        //when
        List<Theme> themes = themeRepository.findThemesWithReservationsBetweenDates(fromDate, toDate, pageable);

        //then
        assertAll(
                () -> assertThat(themes).hasSize(2),
                () -> assertThat(themes.get(0).getId()).isEqualTo(3),
                () -> assertThat(themes.get(1).getId()).isEqualTo(1)
        );
    }
}
