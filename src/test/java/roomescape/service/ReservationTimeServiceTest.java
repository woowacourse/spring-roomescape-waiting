package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.dto.projection.ReservationTimeStatusProjection;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ReservationTimeStatusResponse;
import roomescape.repository.TempReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private TempReservationTimeRepository reservationTimeRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    void 전체_예약_시간_목록_조회() {
        given(reservationTimeRepository.findAll()).willReturn(List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(11, 0))));

        List<ReservationTimeResponse> result = reservationTimeService.findAll();

        assertThat(result).extracting(ReservationTimeResponse::startAt)
                .containsExactly(LocalTime.of(10, 0), LocalTime.of(11, 0));
    }

    @Test
    void 중복되지_않는_시간_저장() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));
        given(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).willReturn(false);
        given(reservationTimeRepository.save(any())).willReturn(new ReservationTime(1L, LocalTime.of(10, 0)));

        ReservationTimeResponse result = reservationTimeService.save(request);

        assertThat(result.startAt()).isEqualTo(LocalTime.of(10, 0));
        verify(reservationTimeRepository).save(any());
    }

    @Test
    void 중복_시간_저장_시_예외() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));
        given(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).willReturn(true);

        assertThatThrownBy(() -> reservationTimeService.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 시간대이므로 추가할 수 없습니다.");

        verify(reservationTimeRepository, never()).save(any());
    }

    @Test
    void 예약_없는_시간_삭제() {
        given(reservationTimeRepository.existsByTimeId(1L)).willReturn(false);

        reservationTimeService.delete(1L);

        verify(reservationTimeRepository).delete(1L);
    }

    @Test
    void 예약_존재하는_시간_삭제_시_예외() {
        given(reservationTimeRepository.existsByTimeId(1L)).willReturn(true);

        assertThatThrownBy(() -> reservationTimeService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");

        verify(reservationTimeRepository, never()).delete(anyLong());
    }

    @Test
    void 가용_시간_조회_시_예약_상태가_매핑된다() {
        String date = LocalDate.now().toString();
        given(reservationTimeRepository.findAvailableTime(1L, date)).willReturn(List.of(
                new ReservationTimeStatusProjection(new ReservationTime(1L, LocalTime.of(10, 0)), ReservationStatus.CONFIRMED),
                new ReservationTimeStatusProjection(new ReservationTime(2L, LocalTime.of(11, 0)), ReservationStatus.AVAILABLE)));

        List<ReservationTimeStatusResponse> result = reservationTimeService.findAvailableTime(1L, date);

        assertThat(result).extracting(ReservationTimeStatusResponse::status)
                .containsExactly(ReservationStatus.CONFIRMED, ReservationStatus.AVAILABLE);
    }
}
