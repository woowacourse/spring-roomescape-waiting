package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaThemeRepository;
import roomescape.service.result.TimeAvailabilityResult;

class ReservationAvailabilityServiceTest {

    private final ReservationRepository reservationRepository = mock();
    private final JpaReservationTimeRepository reservationTimeRepository = mock();
    private final JpaThemeRepository themeRepository = mock();
    private final ReservationAvailabilityService service = new ReservationAvailabilityService(
            reservationRepository,
            reservationTimeRepository,
            themeRepository);

    private final LocalDate date = LocalDate.now().plusDays(1);

    @Test
    void 예약_가능한_시간을_조회한다() {
        // given
        Long themeId = 1L;
        ReservationTime reservedTime = new ReservationTime(1L, LocalTime.parse("08:00"));
        ReservationTime availableTime = new ReservationTime(2L, LocalTime.parse("10:00"));
        Theme theme = new Theme(themeId, "테스트 테마", "테마 설명", "썸네일 주소");
        Reservation reservation = new Reservation(1L, "브라운", date, reservedTime, theme);

        when(themeRepository.findById(themeId))
                .thenReturn(Optional.of(theme));
        when(reservationTimeRepository.findAll())
                .thenReturn(List.of(reservedTime, availableTime));
        when(reservationRepository.findReservationsByThemeAndDate(themeId, date))
                .thenReturn(List.of(reservation));

        // when
        List<TimeAvailabilityResult> result = service.findAvailableTime(themeId, date);

        // then
        assertThat(result).extracting(TimeAvailabilityResult::available)
                .containsExactly(false, true);
    }

    @Test
    void 존재하지_않는_테마의_예약_가능_시간_조회시_예외_발생() {
        // given
        Long themeId = 1L;
        when(themeRepository.findById(themeId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.findAvailableTime(themeId, date))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 테마입니다.");

    }
}
