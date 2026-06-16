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
import roomescape.dao.ReservationRepository;
import roomescape.dao.ReservationTimeRepository;
import roomescape.dao.ThemeRepository;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private ReservationTimeRepository reservationTimeRepository;
    @Mock private ThemeRepository themeRepository;
    @Mock private Clock clock;
    @InjectMocks private ReservationService reservationService;

    private final ReservationTime sampleTime = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme sampleTheme = new Theme(1L, "공포의 저택", "버려진 저택에서 탈출하라!", "https://example.com/img.jpg");
    private final LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 14, 12, 0);

    private void fixClock() {
        given(clock.getZone()).willReturn(ZoneOffset.UTC);
        given(clock.instant()).willReturn(fixedNow.toInstant(ZoneOffset.UTC));
    }

    @Test
    void save_정상_예약_저장() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationRepository.existsByDateAndTime_IdAndTheme_IdAndStatus(futureDate, 1L, 1L, ReservationStatus.CONFIRMED)).willReturn(false);
        given(reservationRepository.save(any(Reservation.class)))
                .willReturn(new Reservation(10L, "브라운", futureDate, fixedNow, sampleTime, sampleTheme));

        Reservation result = reservationService.save("브라운", futureDate, 1L, 1L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("브라운");
        assertThat(result.getDate()).isEqualTo(futureDate);
    }

    @Test
    void save_존재하지_않는_시간이면_예외() {
        given(reservationTimeRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.save("브라운", fixedNow.toLocalDate().plusDays(1), 99L, 1L))
                .isInstanceOf(ReservationTimeNotFoundException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");
    }

    @Test
    void save_존재하지_않는_테마이면_예외() {
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.save("브라운", fixedNow.toLocalDate().plusDays(1), 1L, 99L))
                .isInstanceOf(ThemeNotFoundException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }

    @Test
    void save_이미_예약된_시간이면_예외() {
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationRepository.existsByDateAndTime_IdAndTheme_IdAndStatus(futureDate, 1L, 1L, ReservationStatus.CONFIRMED)).willReturn(true);

        assertThatThrownBy(() -> reservationService.save("브라운", futureDate, 1L, 1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 예약된 시간입니다.");
    }

    @Test
    void delete_정상_삭제() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        given(reservationRepository.findByIdForUpdate(1L)).willReturn(Optional.of(reservation));

        reservationService.delete(1L);

        then(reservationRepository).should().deleteById(1L);
    }

    @Test
    void delete_존재하지_않는_예약이면_예외() {
        assertThatThrownBy(() -> reservationService.delete(999L))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @Test
    void delete_예약_취소_시_대기자_있으면_첫_번째_자동_승인() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        Reservation waiting = new Reservation(5L, "이영희", futureDate, fixedNow.minusHours(1), sampleTime, sampleTheme, ReservationStatus.WAITING);

        given(reservationRepository.findByIdForUpdate(1L)).willReturn(Optional.of(reservation));
        given(reservationRepository.findFirstByDateAndTime_IdAndTheme_IdAndStatusOrderByCreatedAtAscIdAsc(
                futureDate, 1L, 1L, ReservationStatus.WAITING))
                .willReturn(Optional.of(waiting));

        reservationService.delete(1L);

        then(reservationRepository).should().updateStatus(5L, ReservationStatus.CONFIRMED);
    }

    @Test
    void update_예약_변경_시_원래_슬롯_첫_번째_대기자_자동_승인() {
        fixClock();
        LocalDate oldDate = fixedNow.toLocalDate().plusDays(1);
        LocalDate newDate = fixedNow.toLocalDate().plusDays(2);
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(11, 0));
        Reservation reservation = new Reservation(1L, "브라운", oldDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        Reservation waiting = new Reservation(5L, "이영희", oldDate, fixedNow.minusHours(1), sampleTime, sampleTheme, ReservationStatus.WAITING);
        Reservation updated = new Reservation(1L, "브라운", newDate, fixedNow.minusHours(1), newTime, sampleTheme);

        given(reservationRepository.findByIdForUpdate(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeRepository.findById(2L)).willReturn(Optional.of(newTime));
        given(reservationRepository.existsByDateAndTime_IdAndTheme_IdAndStatus(newDate, 2L, 1L, ReservationStatus.CONFIRMED)).willReturn(false);
        given(reservationRepository.findFirstByDateAndTime_IdAndTheme_IdAndStatusOrderByCreatedAtAscIdAsc(
                oldDate, 1L, 1L, ReservationStatus.WAITING))
                .willReturn(Optional.of(waiting));
        given(reservationRepository.findById(1L)).willReturn(Optional.of(updated));

        reservationService.update(1L, newDate, 2L);

        then(reservationRepository).should().updateStatus(5L, ReservationStatus.CONFIRMED);
    }
}
