package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.config.TestTimeConfig;
import roomescape.common.exception.RoomEscapeException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Slot;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.SlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.theme.repository.ThemeRepository;

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
    @Autowired
    private SlotRepository slotRepository;

    @Test
    @Sql(scripts = {"/empty.sql", "/data.sql"})
    void 최근_1주_동안의_예약_상위_10개의_테마를_조회한다() {
        // when
        List<ThemeResponse> popularThemes = themeService.getPopularThemes(1L, 10L);

        // then
        assertThat(popularThemes).map(ThemeResponse::id)
                .containsExactly(1L, 2L, 3L, // 1순위: 테마의 예약 수 내림차순 정렬
                        6L, 5L, 4L, 8L, 7L, // 2순위: 예약 수가 같으면 테마 이름 오름차순 정렬
                        10L, 9L // 예약 개수가 0개여도, 상위 10위 이내라면 조회되어야 함
                );
    }

    @Test
    void 중복된_테마를_추가하면_예외가_발생한다() {
        // given
        ThemeRequest request = new ThemeRequest("귀신찾기", "귀신을 찾는다", "https://image.png");
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
        Slot slot = slotRepository.findOrCreate(LocalDate.parse("2026-08-05"), time, theme);
        reservationRepository.save(
                Reservation.create(slot, "브라운", ReservationStatus.CONFIRMED, LocalDateTime.now()));

        // when & then
        assertThatThrownBy(() -> themeService.deleteTheme(theme.getId())).isInstanceOf(
                        RoomEscapeException.class).extracting("errorCode")
                .isEqualTo(ThemeErrorCode.RESERVATION_EXIST_ON_THEME);
    }

    @Test
    void 예약이_모두_취소되어_슬롯만_남은_테마도_삭제할_수_있다() {
        // given : 예약 생성 후 취소하면 슬롯 행만 남는다
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png"));
        Slot slot = slotRepository.findOrCreate(LocalDate.parse("2026-08-05"), time, theme);
        Reservation saved = reservationRepository.save(
                Reservation.create(slot, "브라운", ReservationStatus.CONFIRMED, LocalDateTime.now()));
        reservationRepository.delete(saved.getId());

        // when
        themeService.deleteTheme(theme.getId());

        // then
        assertThat(themeRepository.findById(theme.getId())).isEmpty();
    }
}
