package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.time.fixture.DateTimeFixture;

@DataJpaTest
@Sql(scripts = "/test_data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("가장 예약이 많은 인기테마를 조회할 수 있다")
    @Test
    void should_return_top_themes_when_requested() {
        Theme popularTheme = new Theme(1L,
                "잠실 캠퍼스 탈출",
                "미션을 빨리 진행하고 잠실 캠퍼스를 탈출하자!",
                "https://velog.velcdn.com/images/jangws/post/cfe0e548-1242-470d-bfa8-19eeb72debc5/image.jpg");

        Pageable pageRequest = PageRequest.of(0, 1);
        List<Theme> topThemes = themeRepository.findTopByDurationAndCount(DateTimeFixture.TODAY,
                DateTimeFixture.TOMORROW, pageRequest);

        assertThat(topThemes)
                .hasSize(1)
                .containsExactly(popularTheme);
    }
}
