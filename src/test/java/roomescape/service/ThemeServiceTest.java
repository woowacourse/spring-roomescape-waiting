package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ThemeRequestDTO;
import roomescape.dto.ThemeResponseDTO;
import roomescape.exception.RoomEscapeException;
import roomescape.exception.ThemeErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest
@Import(TestTimeConfig.class)
@Sql(scripts = "/empty.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @Sql(scripts = "/data.sql")
    void 최근_1주_동안의_예약_상위_10개의_테마를_조회한다() {
        // when
        List<ThemeResponseDTO> popularThemes = themeService.getPopularThemes(1L, 10L);

        // then
        assertThat(popularThemes).map(ThemeResponseDTO::id)
                .containsExactly(1L, 2L, 3L, // 1순위: 테마의 예약 수 내림차순 정렬
                        6L, 5L, 4L, 8L, 7L, // 2순위: 예약 수가 같으면 테마 이름 오름차순 정렬
                        10L, 9L // 예약 개수가 0개여도, 상위 10위 이내라면 조회되어야 함 (예약 개수 0개인 테마들은 2순위 정렬 기준으로 비교)
                );
    }

    @Test
    void 중복된_테마를_추가하면_예외가_발생한다() {
        // given
        ThemeRequestDTO request = new ThemeRequestDTO("귀신찾기", "귀신을 찾는다", "https://image.png");
        themeService.addTheme(request);

        // when & then
        assertThatThrownBy(() -> themeService.addTheme(request)).isInstanceOf(
                        RoomEscapeException.class).extracting("errorCode")
                .isEqualTo(ThemeErrorCode.THEME_DUPLICATE);
    }

    @Test
    void 존재하지_않는_테마를_조회하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> themeService.findById(1L)).isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode").isEqualTo(ThemeErrorCode.THEME_NOT_FOUND);
    }

    @Test
    void 예약이_존재하는_테마를_삭제하면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png"));
        reservationRepository.save(
                Reservation.create("브라운",
                        ReservationSlot.of(LocalDate.parse("2026-08-05"), time, theme)));

        // when & then
        assertThatThrownBy(() -> themeService.deleteTheme(theme.getId())).isInstanceOf(
                        RoomEscapeException.class).extracting("errorCode")
                .isEqualTo(ThemeErrorCode.RESERVATION_EXIST_ON_THEME);
    }
}
