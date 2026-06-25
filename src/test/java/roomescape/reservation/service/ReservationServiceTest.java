package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.InvalidReservationStateException;
import roomescape.exception.UnauthorizedReservationException;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.dao.ReservationTimeDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.UpdateMyReservation;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.theme.domain.Theme;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

  private static final ReservationTime DEFAULT_TIME = ReservationTime.of(1L, LocalTime.of(10, 0));
  private static final LocalDate DEFAULT_DATE = LocalDate.of(2025, 1, 1);
  private static final Theme DEFAULT_THEME = Theme.of(1L, "name", "description", "url");

  @Mock
  ReservationDao reservationDao;

  @Mock
  ReservationTimeDao reservationTimeDao;

  ReservationService reservationService;

  @BeforeEach
  void setUp() {
    reservationService = new ReservationService(reservationDao, reservationTimeDao);
  }

  @Nested
  class 예약하기 {

    public static final String NAME = "누누";
    public static final String DATE = "9999-01-01";
    public static final long TIME_ID = 1L;
    public static final Long THEME_ID = 1L;

    @Test
    void 예약_생성_성공() {
      // given
      ReservationRequest request = new ReservationRequest(NAME, DATE, TIME_ID, THEME_ID);

      // stubbing
      when(reservationTimeDao.findById(any())).thenReturn(null);
      when(reservationDao.findByDateTimeThemeStatus(DATE, TIME_ID, THEME_ID)).thenReturn(false);
      when(reservationDao.insert(NAME, LocalDate.parse(DATE), TIME_ID, THEME_ID, ReservationStatus.PENDING))
          .thenReturn(
              Reservation.of(1L, NAME, DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.PENDING));

      // when
      ReservationCreateResponse response = reservationService.create(request);

      // then
      assertThat(response.status()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void 예약_대기_성공() {
      // given
      ReservationRequest request = new ReservationRequest(NAME, DATE, TIME_ID, THEME_ID);

      // stubbing
      when(reservationTimeDao.findById(any())).thenReturn(null);
      when(reservationDao.findByDateTimeThemeStatus(DATE, TIME_ID, THEME_ID)).thenReturn(true);
      when(reservationDao.insert(NAME, LocalDate.parse(DATE), TIME_ID, THEME_ID, ReservationStatus.WAITING))
          .thenReturn(Reservation.of(1L, NAME, DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.WAITING));

      // when
      ReservationCreateResponse response = reservationService.create(request);

      // then
      assertThat(response.status()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void 중복_예약_시도_예외() {
      // given
      ReservationRequest request = new ReservationRequest(NAME, DATE, TIME_ID, THEME_ID);

      // stubbing
      when(reservationTimeDao.findById(any())).thenReturn(null);
      when(reservationDao.findByNameAndDateAndTimeAndTheme(NAME, DATE, TIME_ID, THEME_ID)).thenReturn(true);

      // when // then
      assertThatThrownBy(() -> reservationService.create(request))
          .isInstanceOf(DuplicateReservationException.class);
    }
  }

  @Nested
  class 확정_예약_삭제 {

    public static final String NAME = "누누";
    public static final long RESERVATION_ID = 1L;
    public static final long WAITING_ID = 2L;

    @Test
    void 확정_예약이_삭제되면_첫_대기가_승격한다() {
      // given
      Reservation waitingReservation = Reservation.of(WAITING_ID, "대기자", DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.WAITING);

      when(reservationDao.findFirstWaitingByDateTimeTheme(DEFAULT_DATE, DEFAULT_TIME.getId(), DEFAULT_THEME.getId()))
          .thenReturn(Optional.of(waitingReservation));

      // when
      reservationService.promoteFirstWaiting(waitingReservation);

      // then
      verify(reservationDao).updateStatus(WAITING_ID, ReservationStatus.RESERVED);
    }

    @Test
    void 다른_사람의_예약을_삭제하면_예외를_발생한다() {
      // given
      Reservation reservation = Reservation.of(RESERVATION_ID, "다른사람", DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.RESERVED);
      when(reservationDao.findById(RESERVATION_ID)).thenReturn(reservation);

      // when // then
      assertThatThrownBy(() -> reservationService.deleteMyReservation(RESERVATION_ID, NAME))
          .isInstanceOf(UnauthorizedReservationException.class);
    }

    @Test
    void 예약_상태가_아닌_예약을_삭제하면_예외를_발생한다() {
      // given
      Reservation reservation = Reservation.of(RESERVATION_ID, NAME, DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.WAITING);
      when(reservationDao.findById(RESERVATION_ID)).thenReturn(reservation);

      // when // then
      assertThatThrownBy(() -> reservationService.deleteMyReservation(RESERVATION_ID, NAME))
          .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void 대기가_없는_확정_예약_삭제_시_기존_예약만_CANCELED된다() {
      // given
      Reservation reservation = Reservation.of(RESERVATION_ID, NAME, DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.RESERVED);
      when(reservationDao.findById(RESERVATION_ID)).thenReturn(reservation);
      when(reservationDao.findFirstWaitingByDateTimeTheme(DEFAULT_DATE, DEFAULT_TIME.getId(), DEFAULT_THEME.getId()))
          .thenReturn(Optional.empty());

      // when
      Reservation deleted = reservationService.deleteMyReservation(RESERVATION_ID, NAME);
      reservationService.promoteFirstWaiting(deleted);

      // then
      verify(reservationDao).delete(RESERVATION_ID);
      verify(reservationDao, never()).updateStatus(any(), any());
    }
  }

  @Nested
  class 예약_변경 {

    public static final String NAME = "누누";
    public static final long RESERVATION_ID = 1L;
    public static final long WAITING_ID = 2L;
    private static final LocalDate NEW_DATE = LocalDate.of(2025, 2, 1);
    private static final long NEW_TIME_ID = 2L;

    @Test
    void 다른_사람의_예약을_변경하면_예외를_발생한다() {
      // given
      Reservation reservation = Reservation.of(RESERVATION_ID, "다른사람", DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.RESERVED);
      when(reservationDao.findById(RESERVATION_ID)).thenReturn(reservation);
      UpdateMyReservation request = new UpdateMyReservation(NEW_DATE, NEW_TIME_ID);

      // when // then
      assertThatThrownBy(() -> reservationService.updateMyReservation(request, NAME, RESERVATION_ID))
          .isInstanceOf(UnauthorizedReservationException.class);
    }

    @Test
    void 이미_예약된_시간대로_변경하면_예외를_발생한다() {
      // given
      Reservation reservation = Reservation.of(RESERVATION_ID, NAME, DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.RESERVED);
      when(reservationDao.findById(RESERVATION_ID)).thenReturn(reservation);
      when(reservationDao.existsByTimeIdAndThemeId(NEW_DATE, NEW_TIME_ID, DEFAULT_THEME.getId())).thenReturn(true);
      UpdateMyReservation request = new UpdateMyReservation(NEW_DATE, NEW_TIME_ID);

      // when // then
      assertThatThrownBy(() -> reservationService.updateMyReservation(request, NAME, RESERVATION_ID))
          .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    void 예약_변경_후_기존_슬롯의_대기가_승격된다() {
      // given
      Reservation reservation = Reservation.of(RESERVATION_ID, NAME, DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.RESERVED);
      Reservation waitingReservation = Reservation.of(WAITING_ID, "대기자", DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.WAITING);
      UpdateMyReservation request = new UpdateMyReservation(NEW_DATE, NEW_TIME_ID);

      when(reservationDao.findById(RESERVATION_ID)).thenReturn(reservation);
      when(reservationDao.existsByTimeIdAndThemeId(NEW_DATE, NEW_TIME_ID, DEFAULT_THEME.getId())).thenReturn(false);
      when(reservationDao.findFirstWaitingByDateTimeTheme(DEFAULT_DATE, DEFAULT_TIME.getId(), DEFAULT_THEME.getId()))
          .thenReturn(Optional.of(waitingReservation));

      // when
      Reservation updated = reservationService.updateMyReservation(request, NAME, RESERVATION_ID);
      reservationService.promoteFirstWaiting(updated);

      // then
      verify(reservationDao).updateReservation(NEW_DATE, NEW_TIME_ID, NAME, RESERVATION_ID);
      verify(reservationDao).updateStatus(WAITING_ID, ReservationStatus.RESERVED);
    }

    @Test
    void 대기가_없는_예약_변경_시_승격_없이_예약만_변경된다() {
      // given
      Reservation reservation = Reservation.of(RESERVATION_ID, NAME, DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.RESERVED);
      UpdateMyReservation request = new UpdateMyReservation(NEW_DATE, NEW_TIME_ID);

      when(reservationDao.findById(RESERVATION_ID)).thenReturn(reservation);
      when(reservationDao.existsByTimeIdAndThemeId(NEW_DATE, NEW_TIME_ID, DEFAULT_THEME.getId())).thenReturn(false);
      when(reservationDao.findFirstWaitingByDateTimeTheme(DEFAULT_DATE, DEFAULT_TIME.getId(), DEFAULT_THEME.getId()))
          .thenReturn(Optional.empty());

      // when
      Reservation updated = reservationService.updateMyReservation(request, NAME, RESERVATION_ID);
      reservationService.promoteFirstWaiting(updated);

      // then
      verify(reservationDao).updateReservation(NEW_DATE, NEW_TIME_ID, NAME, RESERVATION_ID);
      verify(reservationDao, never()).updateStatus(any(), any());
    }
  }
}