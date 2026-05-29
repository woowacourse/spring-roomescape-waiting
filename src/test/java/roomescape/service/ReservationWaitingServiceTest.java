package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ReservationWaitingDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingServiceTest {

    @Mock private ReservationWaitingDao reservationWaitingDao;
    @Mock private ReservationDao reservationDao;
    @Mock private ReservationService reservationService;
    @Mock private ReservationTimeDao reservationTimeDao;
    @Mock private ThemeDao themeDao;
    @Mock private Clock clock;
    @InjectMocks private ReservationWaitingService reservationWaitingService;

    private final ReservationTime sampleTime = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme sampleTheme = new Theme(1L, "공포의 저택", "버려진 저택에서 탈출하라!", "https://example.com/img.jpg");
    private final LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 14, 12, 0);

    private void fixClock() {
        given(clock.getZone()).willReturn(ZoneOffset.UTC);
        given(clock.instant()).willReturn(fixedNow.toInstant(ZoneOffset.UTC));
    }

    @Test
    void saveWaiting_빈_슬롯이면_일반_예약으로_저장() {
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(false);
        given(reservationService.save("브라운", futureDate, 1L, 1L))
                .willReturn(new Reservation(10L, "브라운", futureDate, fixedNow, sampleTime, sampleTheme));

        Reservation result = reservationWaitingService.saveWaiting("브라운", futureDate, 1L, 1L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("브라운");
    }

    @Test
    void saveWaiting_존재하지_않는_시간이면_예외() {
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.saveWaiting("브라운", futureDate, 99L, 1L))
                .isInstanceOf(ReservationTimeNotFoundException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");
    }

    @Test
    void saveWaiting_존재하지_않는_테마이면_예외() {
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationWaitingService.saveWaiting("브라운", futureDate, 1L, 99L))
                .isInstanceOf(ThemeNotFoundException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }

    @Test
    void saveWaiting_이미_예약된_슬롯에_대기_정상_저장() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(true);
        given(reservationDao.existsReservationByDateAndTimeIdAndThemeIdAndName(futureDate, 1L, 1L, "이영희")).willReturn(false);
        given(reservationWaitingDao.existsByDateAndTimeIdAndThemeIdAndName(futureDate, 1L, 1L, "이영희")).willReturn(false);
        Reservation savedReservation = new Reservation(5L, "이영희", futureDate, fixedNow, sampleTime, sampleTheme);
        given(reservationWaitingDao.saveWaiting(any(Reservation.class)))
                .willReturn(new ReservationWaiting(5L, savedReservation, 0));

        Reservation result = reservationWaitingService.saveWaiting("이영희", futureDate, 1L, 1L);

        assertThat(result.getName()).isEqualTo("이영희");
    }

    @Test
    void saveWaiting_이미_예약한_사람이_대기_신청하면_예외() {
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(true);
        given(reservationDao.existsReservationByDateAndTimeIdAndThemeIdAndName(futureDate, 1L, 1L, "브라운")).willReturn(true);

        assertThatThrownBy(() -> reservationWaitingService.saveWaiting("브라운", futureDate, 1L, 1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 예약된 시간입니다.");
    }

    @Test
    void saveWaiting_이미_대기_신청한_사람이면_예외() {
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(true);
        given(reservationDao.existsReservationByDateAndTimeIdAndThemeIdAndName(futureDate, 1L, 1L, "브라운")).willReturn(false);
        given(reservationWaitingDao.existsByDateAndTimeIdAndThemeIdAndName(futureDate, 1L, 1L, "브라운")).willReturn(true);

        assertThatThrownBy(() -> reservationWaitingService.saveWaiting("브라운", futureDate, 1L, 1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 대기 신청한 시간입니다.");
    }

    @Test
    void deleteWaiting_정상_삭제() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        given(reservationWaitingDao.findByWaitingId(1L))
                .willReturn(Optional.of(new ReservationWaiting(1L, reservation, 1)));

        reservationWaitingService.deleteWaiting(1L);

        then(reservationWaitingDao).should().deleteWaiting(1L);
    }
}
