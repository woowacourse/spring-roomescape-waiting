package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.infrastructure.db.ReservationThemeDbRepository;
import roomescape.reservation.infrastructure.db.dao.ReservationThemeH2Dao;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.global.exception.ResourceNotFoundException;
import roomescape.support.JdbcTestSupport;

@Import({ReservationThemeDbRepository.class, ReservationThemeH2Dao.class})
class ReservationThemeDbRepositoryTest extends JdbcTestSupport {

    @Autowired
    private ReservationThemeDbRepository reservationThemeDbRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("존재하지 않는 id를 조회하면 예외를 발생시킨다")
    @Test
    void getByGetGetIdException() {
        // given
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름1", "설명1", "썸네일1");

        // when & then
        assertThatThrownBy(() -> reservationThemeDbRepository.getById(2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @DisplayName("존재하는 id를 조회하면 조회된 예약 객체를 반환한다")
    @Test
    void getByGetGetId() {
        // given
        String name = "이름";
        String description = "설명";
        String thumbnail = "썸네일";
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", name, description, thumbnail);

        // when
        ReservationTheme reservationTheme = reservationThemeDbRepository.getById(1L);

        // then
        assertSoftly(softly -> {
            softly.assertThat(reservationTheme.getName()).isEqualTo(name);
            softly.assertThat(reservationTheme.getDescription()).isEqualTo(description);
            softly.assertThat(reservationTheme.getThumbnail()).isEqualTo(thumbnail);
        });
    }
}
