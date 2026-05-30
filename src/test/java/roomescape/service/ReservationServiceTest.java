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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.exception.DataConflictException;
import roomescape.domain.MyReservation;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationType;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationDao reservationDao;
    @Mock private ReservationTimeDao reservationTimeDao;
    @Mock private ThemeDao themeDao;
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
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(false);
        given(reservationDao.save(any(Reservation.class)))
                .willReturn(new Reservation(10L, "브라운", futureDate, fixedNow, sampleTime, sampleTheme));

        Reservation result = reservationService.save("브라운", futureDate, 1L, 1L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("브라운");
        assertThat(result.getDate()).isEqualTo(futureDate);
    }

    @Test
    void save_존재하지_않는_시간이면_예외() {
        given(reservationTimeDao.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.save("브라운", fixedNow.toLocalDate().plusDays(1), 99L, 1L))
                .isInstanceOf(ReservationTimeNotFoundException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");
    }

    @Test
    void save_존재하지_않는_테마이면_예외() {
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.save("브라운", fixedNow.toLocalDate().plusDays(1), 1L, 99L))
                .isInstanceOf(ThemeNotFoundException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }

    @Test
    void save_이미_예약된_시간이면_예외() {
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(true);

        assertThatThrownBy(() -> reservationService.save("브라운", futureDate, 1L, 1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 예약된 시간입니다.");
    }

    @Test
    void save_저장_충돌이면_예약_충돌_예외() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(false);
        given(reservationDao.save(any(Reservation.class)))
                .willThrow(new DataConflictException(new RuntimeException()));

        assertThatThrownBy(() -> reservationService.save("브라운", futureDate, 1L, 1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 예약된 시간입니다.");
    }

    @Test
    void delete_정상_삭제() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        given(reservationDao.findById(1L)).willReturn(Optional.of(reservation));

        reservationService.delete(1L);

        then(reservationDao).should().delete(1L);
    }

    @Test
    void delete_존재하지_않는_예약이면_예외() {
        given(reservationDao.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.delete(999L))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다.");
        then(reservationDao).should().findById(999L);
    }

    @Test
    void saveWaiting_정상_예약_대기_저장() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(true);
        given(reservationDao.existsReservationByDateAndTimeIdAndThemeIdAndName(futureDate, 1L, 1L, "브라운"))
                .willReturn(false);
        given(reservationDao.existsByDateAndTimeIdAndThemeIdAndName(futureDate, 1L, 1L, "브라운"))
                .willReturn(false);
        given(reservationDao.saveWaiting(any(Reservation.class)))
                .willReturn(new Reservation(10L, "브라운", futureDate, fixedNow, sampleTime, sampleTheme));

        Reservation result = reservationService.saveWaiting("브라운", futureDate, 1L, 1L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("브라운");
        assertThat(result.getDate()).isEqualTo(futureDate);
    }

    @Test
    void saveWaiting_이미_대기_신청한_시간이면_예외() {
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationDao.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(true);
        given(reservationDao.existsReservationByDateAndTimeIdAndThemeIdAndName(futureDate, 1L, 1L, "브라운")).willReturn(false);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeIdAndName(futureDate, 1L, 1L, "브라운")).willReturn(true);

        assertThatThrownBy(() -> reservationService.saveWaiting("브라운", futureDate, 1L, 1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 대기 신청한 시간입니다.");
    }

    @Test
    void saveWaiting_빈_예약_슬롯이면_예외() {
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(false);

        assertThatThrownBy(() -> reservationService.saveWaiting("브라운", futureDate, 1L, 1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("예약 가능한 시간입니다. 일반 예약 API를 이용해주세요.");
    }

    @Test
    void saveWaiting_저장_충돌이면_대기_충돌_예외() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(true);
        given(reservationDao.existsReservationByDateAndTimeIdAndThemeIdAndName(futureDate, 1L, 1L, "브라운"))
                .willReturn(false);
        given(reservationDao.existsByDateAndTimeIdAndThemeIdAndName(futureDate, 1L, 1L, "브라운"))
                .willReturn(false);
        given(reservationDao.saveWaiting(any(Reservation.class)))
                .willThrow(new DataConflictException(new RuntimeException()));

        assertThatThrownBy(() -> reservationService.saveWaiting("브라운", futureDate, 1L, 1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 대기 신청한 시간입니다.");
    }

    @Test
    void deleteWaiting_정상_삭제() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        given(reservationDao.findByWaitingId(1L)).willReturn(Optional.of(reservation));

        reservationService.deleteWaiting(1L);

        then(reservationDao).should().deleteWaiting(1L);
    }

    @Test
    void deleteWaiting_존재하지_않는_대기이면_예외() {
        given(reservationDao.findByWaitingId(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.deleteWaiting(999L))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessage("존재하지 않는 대기입니다.");
        then(reservationDao).should().findByWaitingId(999L);
    }

    @Test
    void getMyReservations_예약과_대기를_함께_조회한다() {
        Reservation laterReservation = new Reservation(
                1L, "브라운", LocalDate.of(2026, 5, 16), fixedNow, sampleTime, sampleTheme);
        Reservation earlierWaiting = new Reservation(
                2L, "브라운", LocalDate.of(2026, 5, 15), fixedNow, sampleTime, sampleTheme);
        given(reservationDao.findByName("브라운")).willReturn(List.of(laterReservation));
        given(reservationDao.findAllWaitingByName("브라운"))
                .willReturn(List.of(new ReservationWaiting(earlierWaiting, 1)));

        List<MyReservation> myReservations = reservationService.getMyReservations("브라운");

        assertThat(myReservations).hasSize(2);
        assertThat(myReservations)
                .extracting(MyReservation::reservationType)
                .containsExactly(ReservationType.WAITING, ReservationType.RESERVED);
        assertThat(myReservations.getFirst().waitingNumber()).isEqualTo(1);
        assertThat(myReservations.getLast().waitingNumber()).isNull();
    }
}
