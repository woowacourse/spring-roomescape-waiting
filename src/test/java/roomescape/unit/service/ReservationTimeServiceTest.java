package roomescape.unit.service;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dto.request.CreateReservationTimeRequest;
import roomescape.entity.ReservationTime;
import roomescape.exception.custom.InvalidReservationTimeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.ReservationTimeService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    void 예약시간을_추가할_수_있다() {
        CreateReservationTimeRequest createReservationTimeRequest = new CreateReservationTimeRequest(
                LocalTime.now().plusMinutes(30L));
        when(reservationTimeRepository.existsByStartAt(any(LocalTime.class))).thenReturn(false);
        when(reservationTimeRepository.save(any(ReservationTime.class))).thenReturn(new ReservationTime(
                1L,
                LocalTime.now()
        ));
        ReservationTime reservationTime = reservationTimeService.addReservationTime(createReservationTimeRequest);

        assertThat(reservationTime.getId()).isNotNull();
    }

    @Test
    void 예약을_삭제_할_수_있다() {
        //given

        reservationTimeService.deleteReservationTime(1L);
        //when & then
        verify(reservationTimeRepository, times(1)).deleteById(1L);
    }

    @Test
    void 예약시간을_조회할_수_있다() {
        // given
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now());
        ReservationTime reservationTime1 = new ReservationTime(2L, LocalTime.now());
        when(reservationTimeRepository.findAll()).thenReturn(List.of(reservationTime1,reservationTime));
        // when
        List<ReservationTime> actual = reservationTimeService.findAll();

        //then
        assertThat(actual).hasSize(2);
    }

    @Test
    void 특정_시간에_대한_예약이_존재할때_시간을_삭제하려고하면_예외가_발생한다() {
        // given
        when(reservationRepository.existsByTimeId(1L)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1L))
                .isInstanceOf(InvalidReservationTimeException.class);
    }

    @Test
    void 중복_시간을_설정할_수_없다() {
        // given
        LocalTime startAt = LocalTime.now();
        when(reservationTimeRepository.existsByStartAt(any(LocalTime.class))).thenReturn(true);
        CreateReservationTimeRequest duplicateAddReservationTime = new CreateReservationTimeRequest(startAt);
        // when & then
        assertThatThrownBy(() -> reservationTimeService.addReservationTime(duplicateAddReservationTime))
                .isInstanceOf(InvalidReservationTimeException.class);
    }
}
