package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.dao.ReservationTimeDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.ReservationRequest;
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
      when(reservationDao.findByDateTimeTheme(DATE, TIME_ID, THEME_ID)).thenReturn(false);
      when(reservationDao.insert(NAME, LocalDate.parse(DATE), TIME_ID, THEME_ID, ReservationStatus.RESERVED))
          .thenReturn(
              Reservation.of(1L, NAME, DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.RESERVED));

      // when
      ReservationCreateResponse response = reservationService.create(request);

      // then
      assertThat(response.status()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    void 예약_대기_성공() {
      // given
      ReservationRequest request = new ReservationRequest(NAME, DATE, TIME_ID, THEME_ID);

      // stubbing
      when(reservationTimeDao.findById(any())).thenReturn(null);
      when(reservationDao.findByDateTimeTheme(DATE, TIME_ID, THEME_ID)).thenReturn(true);
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
          .isInstanceOf(IllegalStateException.class);
    }
  }
}