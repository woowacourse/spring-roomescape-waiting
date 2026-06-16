package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dao.ReservationRepository;
import roomescape.dao.ReservationTimeRepository;
import roomescape.service.exception.ReservationConflictException;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock private ReservationTimeRepository reservationTimeRepository;
    @Mock private ReservationRepository reservationRepository;
    @InjectMocks private ReservationTimeService reservationTimeService;

    @Test
    void save_이미_존재하는_시간이면_예외() {
        LocalTime startAt = LocalTime.of(10, 0);
        given(reservationTimeRepository.existsByStartAt(startAt)).willReturn(true);

        assertThatThrownBy(() -> reservationTimeService.save(startAt))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 존재하는 예약 시간입니다.");
    }

    @Test
    void delete_정상_삭제() {
        given(reservationRepository.existsByTime_Id(1L)).willReturn(false);

        reservationTimeService.delete(1L);

        then(reservationTimeRepository).should().deleteById(1L);
    }

    @Test
    void delete_예약에_사용중인_시간이면_예외() {
        given(reservationRepository.existsByTime_Id(1L)).willReturn(true);

        assertThatThrownBy(() -> reservationTimeService.delete(1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("예약에 사용 중인 시간은 삭제할 수 없습니다.");
    }
}
