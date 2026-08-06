package roomescape.domain.reservationdate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdate.dto.ReservationDateCreationRequest;
import roomescape.domain.reservationdate.dto.ReservationDateCreationResponse;
import roomescape.domain.reservationdate.dto.ReservationDateResponse;
import roomescape.support.exception.RoomescapeException;

class ReservationDateServiceTest {

    private ReservationDateService reservationDateService;
    private ReservationDateRepository reservationDateRepository;
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationDateRepository = mock(ReservationDateRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        reservationDateService = new ReservationDateService(reservationRepository, reservationDateRepository);
    }

    @Test
    @DisplayName("예약 날짜를 생성한다.")
    void createReservationDate() {
        ReservationDateCreationRequest request = new ReservationDateCreationRequest(LocalDate.now().plusDays(1));
        when(reservationDateRepository.existsByPlayDay(request.playDay())).thenReturn(false);
        when(reservationDateRepository.save(any(ReservationDate.class)))
            .thenReturn(ReservationDate.of(1L, request.playDay()));

        ReservationDateCreationResponse response = reservationDateService.createReservationDate(request);

        assertThat(response.playDay()).isEqualTo(request.playDay());
        verify(reservationDateRepository).save(any(ReservationDate.class));
    }

    @Test
    @DisplayName("중복된 날짜 생성 시 예외가 발생한다.")
    void createDuplicateDate() {
        LocalDate playDay = LocalDate.now().plusDays(1);
        when(reservationDateRepository.existsByPlayDay(playDay)).thenReturn(true);

        assertThatThrownBy(
            () -> reservationDateService.createReservationDate(new ReservationDateCreationRequest(playDay)))
            .isInstanceOf(RoomescapeException.class);
        verify(reservationDateRepository, never()).save(any(ReservationDate.class));
    }

    @Test
    @DisplayName("오늘 이후의 날짜만 조회한다.")
    void getAllAvailableReservationDate() {
        when(reservationDateRepository.findAll()).thenReturn(List.of(
            ReservationDate.of(1L, LocalDate.now().minusDays(1)),
            ReservationDate.of(2L, LocalDate.now().plusDays(1))
        ));

        List<ReservationDateResponse> responses = reservationDateService.getAllAvailableReservationDate();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).playDay()).isEqualTo(LocalDate.now().plusDays(1));
    }

    @Test
    @DisplayName("사용 중인 날짜를 삭제하려 하면 예외가 발생한다.")
    void deleteInUseDate() {
        ReservationDate date = ReservationDate.of(1L, LocalDate.now().plusDays(1));
        when(reservationDateRepository.findById(date.getId())).thenReturn(Optional.of(date));
        when(reservationRepository.countByDateId(date.getId())).thenReturn(1);

        assertThatThrownBy(() -> reservationDateService.deleteReservationDate(date.getId()))
            .isInstanceOf(RoomescapeException.class);
        verify(reservationDateRepository, never()).delete(date);
    }
}
