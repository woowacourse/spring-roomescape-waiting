package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.error.ReservationException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest
@Sql("/data.sql")
class ThemeServiceTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private ThemeService service;

    @BeforeEach
    void setUp() {
        service = new ThemeService(themeRepository, reservationRepository);
    }

    @Test
    void 테마가_저장된다() {
        // given
        ThemeRequest request = new ThemeRequest("이름3", "설명3", "썸네일3");

        // when
        ThemeResponse response = service.saveTheme(request);

        // then
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(response.name()).isEqualTo(request.name());
            soft.assertThat(response.description()).isEqualTo(request.description());
            soft.assertThat(response.thumbnail()).isEqualTo(request.thumbnail());
        });
    }

    @Test
    void 모든_테마를_조회한다() {
        // when
        List<ThemeResponse> all = service.findAll();

        // then
        assertThat(all).hasSize(2);
    }

    @Test
    void 테마가_삭제된다() {
        // given
        final Theme theme = new Theme("테마3", "설명3", "썸네일3");
        final Theme savedTheme = themeRepository.save(theme);
        assertThat(savedTheme.getId()).isEqualTo(3L);

        // when
        service.delete(savedTheme.getId());

        // then
        List<ThemeResponse> afterDelete = service.findAll();
        assertThat(afterDelete)
                .hasSize(2)
                .extracting(ThemeResponse::id)
                .doesNotContain(3L);
    }

    @Test
    void 예약이_존재하는_테마를_삭제하지_못_한다() {
        // given
        // when
        // then
        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ReservationException.class)
                .hasMessage("해당 테마로 예약된 건이 존재합니다.");
    }
}
