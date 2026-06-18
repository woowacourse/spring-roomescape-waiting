package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

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
import roomescape.domain.exception.ForbiddenException;
import roomescape.domain.exception.PastReservationException;
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
    void update_예약_변경_시_기존_슬롯의_첫번째_대기를_예약으로_전환한다() {
        fixClock();
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(11, 0));
        LocalDate originalDate = fixedNow.toLocalDate().plusDays(1);
        LocalDate updateDate = fixedNow.toLocalDate().plusDays(2);
        Reservation reservation = new Reservation(
                1L, "브라운", originalDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        Reservation updated = new Reservation(
                1L, "브라운", updateDate, fixedNow.minusHours(1), newTime, sampleTheme);
        Reservation waiting = new Reservation(
                10L, "이든", originalDate, fixedNow, sampleTime, sampleTheme);
        given(reservationDao.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeDao.findById(2L)).willReturn(Optional.of(newTime));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(updateDate, 2L, 1L)).willReturn(false);
        given(reservationDao.update(any(Reservation.class))).willReturn(updated);
        given(reservationDao.findFirstWaitingBySlot(originalDate, 1L, 1L))
                .willReturn(Optional.of(new ReservationWaiting(waiting, 1)));

        Reservation result = reservationService.update(1L, "브라운", updateDate, 2L);

        assertThat(result).isEqualTo(updated);
        then(reservationDao).should().update(any(Reservation.class));
        then(reservationDao).should().save(promotedReservationOf(waiting));
        then(reservationDao).should().deleteWaiting(10L);
    }

    @Test
    void update_지난_예약이면_변경할_수_없다() {
        fixClock();
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(11, 0));
        LocalDate pastDate = fixedNow.toLocalDate().minusDays(1);
        LocalDate updateDate = fixedNow.toLocalDate().plusDays(1);
        Reservation reservation = new Reservation(
                1L, "브라운", pastDate, fixedNow.minusDays(2), sampleTime, sampleTheme);
        given(reservationDao.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeDao.findById(2L)).willReturn(Optional.of(newTime));

        assertThatThrownBy(() -> reservationService.update(1L, "브라운", updateDate, 2L))
                .isInstanceOf(PastReservationException.class)
                .hasMessage("과거 예약은 변경할 수 없습니다.");

        then(reservationDao).should(never()).update(any(Reservation.class));
        then(reservationDao).should(never()).findFirstWaitingBySlot(pastDate, 1L, 1L);
    }

    @Test
    void update_같은_날짜와_시간이면_예외() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        Reservation reservation = new Reservation(
                1L, "브라운", futureDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        given(reservationDao.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));

        assertThatThrownBy(() -> reservationService.update(1L, "브라운", futureDate, 1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("기존 예약과 변경할 예약이 동일한 날짜와 시간입니다.");

        then(reservationDao).should(never()).existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L);
        then(reservationDao).should(never()).update(any(Reservation.class));
    }

    @Test
    void update_예약_변경_중_저장이_충돌하면_예약_충돌_예외() {
        fixClock();
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(11, 0));
        LocalDate originalDate = fixedNow.toLocalDate().plusDays(1);
        LocalDate updateDate = fixedNow.toLocalDate().plusDays(2);
        Reservation reservation = new Reservation(
                1L, "브라운", originalDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        given(reservationDao.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeDao.findById(2L)).willReturn(Optional.of(newTime));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(updateDate, 2L, 1L)).willReturn(false);
        given(reservationDao.update(any(Reservation.class))).willThrow(new DataConflictException(new RuntimeException()));

        assertThatThrownBy(() -> reservationService.update(1L, "브라운", updateDate, 2L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 예약된 시간입니다.");

        then(reservationDao).should(never()).findFirstWaitingBySlot(originalDate, 1L, 1L);
    }

    @Test
    void update_본인_예약이_아니면_예외() {
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        LocalDate updateDate = fixedNow.toLocalDate().plusDays(2);
        Reservation reservation = new Reservation(
                1L, "브라운", futureDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        given(reservationDao.findById(1L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.update(1L, "이든", updateDate, 2L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("본인의 예약 또는 대기만 관리할 수 있습니다.");

        then(reservationTimeDao).should(never()).findById(2L);
        then(reservationDao).should(never()).update(any(Reservation.class));
    }

    @Test
    void delete_정상_삭제() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        given(reservationDao.findById(1L)).willReturn(Optional.of(reservation));

        reservationService.delete(1L, "브라운");

        then(reservationDao).should().delete(1L);
    }

    @Test
    void delete_지난_예약도_삭제() {
        fixClock();
        LocalDate pastDate = fixedNow.toLocalDate().minusDays(1);
        Reservation reservation = new Reservation(1L, "브라운", pastDate, fixedNow.minusDays(2), sampleTime, sampleTheme);
        given(reservationDao.findById(1L)).willReturn(Optional.of(reservation));

        reservationService.delete(1L);

        then(reservationDao).should().delete(1L);
        then(reservationDao).should(never()).findFirstWaitingBySlot(pastDate, 1L, 1L);
        then(reservationDao).should(never()).save(any(Reservation.class));
        then(reservationDao).should(never()).deleteWaiting(10L);
    }

    @Test
    void delete_사용자_예약_취소_시_첫번째_대기를_예약으로_전환한다() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        Reservation waiting = new Reservation(10L, "이든", futureDate, fixedNow, sampleTime, sampleTheme);
        given(reservationDao.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationDao.findFirstWaitingBySlot(futureDate, 1L, 1L))
                .willReturn(Optional.of(new ReservationWaiting(waiting, 1)));

        reservationService.delete(1L, "브라운");

        then(reservationDao).should().delete(1L);
        then(reservationDao).should().save(promotedReservationOf(waiting));
        then(reservationDao).should().deleteWaiting(10L);
    }

    @Test
    void delete_관리자_예약_삭제_시_첫번째_대기를_예약으로_전환한다() {
        fixClock();
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        Reservation waiting = new Reservation(10L, "이든", futureDate, fixedNow, sampleTime, sampleTheme);
        given(reservationDao.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationDao.findFirstWaitingBySlot(futureDate, 1L, 1L))
                .willReturn(Optional.of(new ReservationWaiting(waiting, 1)));

        reservationService.delete(1L);

        then(reservationDao).should().delete(1L);
        then(reservationDao).should().save(promotedReservationOf(waiting));
        then(reservationDao).should().deleteWaiting(10L);
    }

    @Test
    void delete_본인_예약이_아니면_예외() {
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        given(reservationDao.findById(1L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.delete(1L, "이든"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("본인의 예약 또는 대기만 관리할 수 있습니다.");
    }

    @Test
    void delete_존재하지_않는_예약이면_예외() {
        given(reservationDao.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.delete(999L, "브라운"))
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

        reservationService.deleteWaiting(1L, "브라운");

        then(reservationDao).should().deleteWaiting(1L);
    }

    @Test
    void deleteWaiting_본인_대기가_아니면_예외() {
        LocalDate futureDate = fixedNow.toLocalDate().plusDays(1);
        Reservation reservation = new Reservation(1L, "브라운", futureDate, fixedNow.minusHours(1), sampleTime, sampleTheme);
        given(reservationDao.findByWaitingId(1L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.deleteWaiting(1L, "이든"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("본인의 예약 또는 대기만 관리할 수 있습니다.");
    }

    @Test
    void deleteWaiting_존재하지_않는_대기이면_예외() {
        given(reservationDao.findByWaitingId(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.deleteWaiting(999L, "브라운"))
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
                .containsExactly(ReservationType.WAITING, ReservationType.CONFIRMED);
        assertThat(myReservations.getFirst().waitingNumber()).isEqualTo(1);
        assertThat(myReservations.getLast().waitingNumber()).isNull();
    }

    private Reservation promotedReservationOf(Reservation waiting) {
        return argThat(reservation -> reservation.getId() == null
                && reservation.getName().equals(waiting.getName())
                && reservation.getDate().equals(waiting.getDate())
                && reservation.getCreatedAt().equals(fixedNow)
                && reservation.getTime().equals(waiting.getTime())
                && reservation.getTheme().equals(waiting.getTheme()));
    }
}
