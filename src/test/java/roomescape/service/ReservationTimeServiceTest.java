package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.reservation.time.ReservationTime;
import roomescape.service.dto.command.ReservationTimeCommand;
import roomescape.service.dto.result.ReservationTimeResult;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {
    private final Long timeId = 1L;
    private final LocalTime startAt = LocalTime.parse("19:00");
    private final ReservationTime saved = new ReservationTime(timeId, startAt);

    private ReservationTimeService reservationTimeService;

    @Mock
    private ReservationTimeDao reservationTimeDao;
    @Mock
    private ReservationDao reservationDao;

    @BeforeEach
    public void setUp() {
        reservationTimeService = new ReservationTimeService(reservationTimeDao, reservationDao);
    }

    @Test
    public void 예약_시간_생성_정상_테스트() {
        ReservationTimeCommand command = new ReservationTimeCommand(startAt);
        given(reservationTimeDao.save(any())).willReturn(saved);

        ReservationTimeResult result = reservationTimeService.save(command);

        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.startAt()).isEqualTo(saved.getStartAt());
    }

    @Test
    public void 예약_시간_삭제_정상_테스트() {
        given(reservationTimeDao.existsById(timeId)).willReturn(true);
        given(reservationDao.existsByTimeId(timeId)).willReturn(false);

        reservationTimeService.delete(timeId);

        verify(reservationTimeDao).delete(timeId);
    }

    @Test
    public void 존재하지_않는_시간을_삭제하면_예외가_발생한다() {
        given(reservationTimeDao.existsById(timeId)).willReturn(false);

        assertThatThrownBy(() -> reservationTimeService.delete(timeId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 시간입니다.");

        verify(reservationTimeDao, never()).delete(anyLong());
    }

    @Test
    public void 예약이_존재하는_시간을_삭제하면_예외가_발생한다() {
        given(reservationTimeDao.existsById(timeId)).willReturn(true);
        given(reservationDao.existsByTimeId(timeId)).willReturn(true);

        assertThatThrownBy(() -> reservationTimeService.delete(timeId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("예약이 존재하는 시간은 삭제할 수 없습니다.");

        verify(reservationTimeDao, never()).delete(anyLong());
    }
}