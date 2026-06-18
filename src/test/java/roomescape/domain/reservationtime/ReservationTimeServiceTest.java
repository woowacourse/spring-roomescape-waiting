package roomescape.domain.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.reservationslot.JpaReservationSlotRepository;
import roomescape.domain.reservationtime.admin.dto.CreateTimeRequest;
import roomescape.domain.reservationtime.admin.dto.CreateTimeResponse;
import roomescape.domain.reservationtime.admin.dto.ReservationTimeResponse;
import roomescape.support.exception.RoomescapeException;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private JpaReservationTimeRepository reservationTimeRepository;

    @Mock
    private JpaReservationSlotRepository reservationSlotRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("예약 시간을 생성한다.")
    void createReservationTime() {
        // given
        ReservationTime savedReservationTime = ReservationTime.of(1L, LocalTime.of(10, 0));
        given(reservationTimeRepository.save(any(ReservationTime.class)))
            .willReturn(savedReservationTime);

        // when
        CreateTimeResponse response = reservationTimeService.createReservationTime(
            new CreateTimeRequest(LocalTime.of(10, 0))
        );

        // then
        assertSoftly(softly -> {
            assertThat(response.id()).isEqualTo(savedReservationTime.getId());
            assertThat(response.startAt()).isEqualTo(LocalTime.of(10, 0));
        });
    }

    @Test
    @DisplayName("예약 시간 목록을 조회한다.")
    void getReservationTimeList() {
        // given
        given(reservationTimeRepository.findAll()).willReturn(List.of(
            ReservationTime.of(1L, LocalTime.of(10, 0)),
            ReservationTime.of(2L, LocalTime.of(11, 0))
        ));

        // when
        List<ReservationTimeResponse> responses = reservationTimeService.getAllReservationTime();

        // then
        assertSoftly(softly -> {
            assertThat(responses).hasSize(2);
            assertThat(responses)
                .extracting(ReservationTimeResponse::id, ReservationTimeResponse::startAt)
                .containsExactly(
                    tuple(1L, LocalTime.of(10, 0)),
                    tuple(2L, LocalTime.of(11, 0))
                );
        });
    }

    @Test
    @DisplayName("이미 예약이 존재하는 시간은 삭제할 수 없다.")
    void throwExceptionWhenDeletingTimeInUse() {
        // given
        Long reservationTimeId = 1L;
        given(reservationSlotRepository.countByTimeId(reservationTimeId)).willReturn(1);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(reservationTimeId))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이미 예약이 존재하는 시간대는 삭제할 수 없습니다.");
        verify(reservationTimeRepository, never()).deleteById(reservationTimeId);
    }

    @Test
    @DisplayName("예약이 없는 시간은 삭제한다.")
    void deleteTimeWhenNoReservationExists() {
        // given
        Long reservationTimeId = 1L;
        given(reservationSlotRepository.countByTimeId(reservationTimeId)).willReturn(0);

        // when
        reservationTimeService.deleteReservationTime(reservationTimeId);

        // then
        verify(reservationTimeRepository).deleteById(reservationTimeId);
    }
}
