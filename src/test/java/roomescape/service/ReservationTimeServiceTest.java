package roomescape.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.ReservationTime;
import roomescape.infrastructure.ReservationRepository;
import roomescape.infrastructure.ReservationTimeRepository;
import roomescape.service.exception.ReservationExistsException;
import roomescape.service.request.ReservationTimeSaveDto;
import roomescape.service.response.ReservationTimeDto;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @DisplayName("예약 시간을 저장하고, 해당 시간을 id값과 함께 반환한다.")
    @Test
    void save() {
        long timeId = 1L;
        String startAt = LocalTime.now().toString();
        ReservationTime reservationTime = new ReservationTime(timeId, startAt);

        when(reservationTimeRepository.save(any(ReservationTime.class)))
                .thenReturn(reservationTime);

        ReservationTimeDto actual = reservationTimeService.save(new ReservationTimeSaveDto(startAt));
        ReservationTimeDto expected = ReservationTimeDto.from(reservationTime);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("실패: 잘못된 시간 포맷을 저장하면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"24:00", "-1:00", "10:60"})
    @NullAndEmptySource
    void save_IllegalTimeFormat(String time) {
        assertThatThrownBy(() -> reservationTimeService.save(new ReservationTimeSaveDto(time)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("실패: 이미 존재하는 시간을 추가할 수 없다.")
    @Test
    void save_TimeAlreadyExists() {
        String rawTime = "10:00";

        when(reservationTimeRepository.existsByStartAt(LocalTime.parse(rawTime)))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationTimeService.save(new ReservationTimeSaveDto(rawTime)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("실패: 시간을 사용하는 예약이 존재하는 경우 시간을 삭제할 수 없다.")
    @Test
    void delete_ReservationExists() {
        long timeId = 1L;
        when(reservationRepository.existsByTimeId(timeId))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationTimeService.delete(timeId))
                .isInstanceOf(ReservationExistsException.class);
    }
}
