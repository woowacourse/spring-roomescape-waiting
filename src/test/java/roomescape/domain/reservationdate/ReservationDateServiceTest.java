package roomescape.domain.reservationdate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.reservationdate.admin.dto.AdminReservationDateResponse;
import roomescape.domain.reservationdate.admin.dto.CreateReservationDateRequest;
import roomescape.domain.reservationdate.admin.dto.CreateReservationDateResponse;
import roomescape.domain.reservationslot.JpaReservationSlotRepository;
import roomescape.support.exception.RoomescapeException;

@ExtendWith(MockitoExtension.class)
class ReservationDateServiceTest {

    @Mock
    private JpaReservationSlotRepository reservationSlotRepository;

    @Mock
    private JpaReservationDateRepository reservationDateRepository;

    @InjectMocks
    private ReservationDateService reservationDateService;

    @Test
    @DisplayName("예약 날짜를 생성한다.")
    void createReservationDate() {
        // given
        ReservationDate savedReservationDate = ReservationDate.of(1L, LocalDate.of(2026, 5, 4));
        given(reservationDateRepository.save(any(ReservationDate.class)))
            .willReturn(savedReservationDate);

        // when
        CreateReservationDateResponse response = reservationDateService.createReservationDate(
            new CreateReservationDateRequest(LocalDate.of(2026, 5, 4))
        );

        // then
        assertSoftly(softly -> {
            assertThat(response.id()).isEqualTo(savedReservationDate.getId());
            assertThat(response.reservationDate()).isEqualTo(LocalDate.of(2026, 5, 4));
        });
    }

    @Test
    @DisplayName("예약 날짜 목록을 조회한다.")
    void getReservationDateList() {
        // given
        ReservationDate reservationDate = ReservationDate.of(1L, LocalDate.of(2026, 5, 4));
        given(reservationDateRepository.findAll()).willReturn(List.of(reservationDate));

        // when
        List<AdminReservationDateResponse> responses = reservationDateService.getAllReservationDateForAdmin();

        // then
        assertSoftly(softly -> {
            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().id()).isEqualTo(reservationDate.getId());
            assertThat(responses.getFirst().reservationDate()).isEqualTo(LocalDate.of(2026, 5, 4));
        });
    }

    @Test
    @DisplayName("이미 예약이 존재하는 날짜는 삭제할 수 없다.")
    void throwExceptionWhenDeletingDateInUse() {
        // given
        Long reservationDateId = 1L;
        given(reservationSlotRepository.countByDateId(reservationDateId)).willReturn(1);

        // when & then
        assertThatThrownBy(() -> reservationDateService.deleteReservationDate(reservationDateId))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이미 예약이 존재하는 날짜는 삭제할 수 없습니다.");
        verify(reservationDateRepository, never()).deleteById(reservationDateId);
    }

    @Test
    @DisplayName("예약이 없는 날짜는 삭제한다.")
    void deleteDateWhenNoReservationExists() {
        // given
        Long reservationDateId = 1L;
        given(reservationSlotRepository.countByDateId(reservationDateId)).willReturn(0);

        // when
        reservationDateService.deleteReservationDate(reservationDateId);

        // then
        verify(reservationDateRepository).deleteById(reservationDateId);
    }
}
