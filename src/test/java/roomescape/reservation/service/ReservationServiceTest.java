package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import roomescape.reservation.dao.ReservationDAO;
import roomescape.reservation.dao.ReservationTimeDAO;
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
  ReservationDAO reservationDAO;

  @Mock
  ReservationTimeDAO reservationTimeDAO;

  ReservationService reservationService;

  @BeforeEach
  void setUp() {
    reservationService = new ReservationService(reservationDAO, reservationTimeDAO);
  }

  @Nested
  class 예약하기 {
    @Test
    void 예약_생성_성공() {
      // given
      String name = "누누";
      String date = "9999-01-01";
      Long timeId = 1L;
      Long themeId = 1L;
      ReservationRequest request = new ReservationRequest(name, date, timeId, themeId);

      // stubbing
      when(reservationTimeDAO.findById(any())).thenReturn(null);
      when(reservationDAO.findByDateTimeTheme(date, timeId, themeId)).thenReturn(false);
      when(reservationDAO.insert(name, LocalDate.parse(date), timeId, themeId, ReservationStatus.RESERVED))
          .thenReturn(
              Reservation.of(1L, name, DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.RESERVED));

      // when
      ReservationCreateResponse response = reservationService.create(request);

      // then
      assertThat(response.status()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    void 예약_대기_성공() {
      // given
      String name = "누누";
      String date = "9999-01-01";
      Long timeId = 1L;
      Long themeId = 1L;
      ReservationRequest request = new ReservationRequest(name, date, timeId, themeId);

      // stubbing
      when(reservationTimeDAO.findById(any())).thenReturn(null);
      when(reservationDAO.findByDateTimeTheme(date, timeId, themeId)).thenReturn(true);
      when(reservationDAO.insert(name, LocalDate.parse(date), timeId, themeId, ReservationStatus.WAITING))
          .thenReturn(Reservation.of(1L, name, DEFAULT_DATE, DEFAULT_TIME, DEFAULT_THEME, ReservationStatus.WAITING));

      // when
      ReservationCreateResponse response = reservationService.create(request);

      // then
      assertThat(response.status()).isEqualTo(ReservationStatus.WAITING);
    }
  }
}