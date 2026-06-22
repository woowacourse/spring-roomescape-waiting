package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.ReservationTime;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.dto.request.ReservationTimeCreateRequest;
import roomescape.dto.response.ReservationTimeResult;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    void 예약시간_목록_조회() {
        // given
        ReservationTime time1 = ReservationTime.createWithId(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        ReservationTime time2 = ReservationTime.createWithId(2L, LocalTime.of(12, 0), LocalTime.of(13, 0));
        given(reservationTimeRepository.findAll()).willReturn(List.of(time1, time2));

        // when
        List<ReservationTimeResult> responses = reservationTimeService.getTimes();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(1).id()).isEqualTo(2L);

        verify(reservationTimeRepository).findAll();
    }

    @Test
    void 예약시간_생성() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);
        LocalTime endAt = LocalTime.of(11, 0);
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(startAt, endAt);

        ReservationTime savedTime = ReservationTime.createWithId(1L, startAt, endAt);
        given(reservationTimeRepository.save(any(ReservationTime.class))).willReturn(savedTime);

        // when
        ReservationTimeResult response = reservationTimeService.create(request);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.startAt()).isEqualTo(startAt);
        assertThat(response.endAt()).isEqualTo(endAt);
    }

    @Test
    void 예약시간_삭제() {
        // given
        Long targetTimeId = 1L;
        given(reservationRepository.existsByTimeId(targetTimeId)).willReturn(false);
        given(reservationTimeRepository.delete(targetTimeId)).willReturn(true);

        // when
        reservationTimeService.delete(targetTimeId);

        // then
        verify(reservationRepository).existsByTimeId(targetTimeId);
        verify(reservationTimeRepository).delete(targetTimeId);
    }

    @Test
    void 삭제하려는_시간에_이미_예약이_존재할_경우_예외발생() {
        // given
        Long targetTimeId = 1L;
        given(reservationRepository.existsByTimeId(targetTimeId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(targetTimeId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_HAS_RESERVATION);

        verify(reservationTimeRepository, org.mockito.Mockito.never()).delete(anyLong());
    }

    @Test
    void 삭제하려는_시간이_DB에_존재하지_않을시_예외밯생() {
        // given
        Long targetTimeId = 1L;
        given(reservationRepository.existsByTimeId(targetTimeId)).willReturn(false);
        given(reservationTimeRepository.delete(targetTimeId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(targetTimeId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_NOT_FOUND);
    }
}
