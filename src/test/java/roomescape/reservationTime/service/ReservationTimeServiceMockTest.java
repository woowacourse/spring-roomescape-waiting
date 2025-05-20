package roomescape.reservationTime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.dto.request.TimeConditionRequest;
import roomescape.reservationTime.dto.response.TimeConditionResponse;
import roomescape.theme.domain.Theme;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceMockTest {
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @DisplayName("이미 존재하는 예약이 있는 경우 예약 시간을 삭제할 수 없다.")
    @Test
    void can_not_delete_when_reservation_exists() {
        // given
        Mockito.when(reservationRepository.existsByTimeId(1L))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 가능 시간 조회 테스트")
    @Test
    void time_condition_test() {
        // given
        LocalDate localDate = LocalDate.of(2024, 10, 6);
        Member member1 = Member.createWithId(1L, "a", "a", "a", Role.USER);
        Long themeId = 1L;
        Mockito.when(reservationRepository.findByDateAndThemeId(localDate, themeId))
                .thenReturn(
                        List.of(
                                Reservation.createWithId(1L, member1, localDate,
                                        ReservationTime.createWithId(1L, LocalTime.of(10, 0)),
                                        Theme.createWithId(1L, "a", "a", "a"))
                        )
                );
        Mockito.when(reservationTimeRepository.findAll())
                .thenReturn(
                        List.of(
                                ReservationTime.createWithId(1L, LocalTime.of(10, 0)),
                                ReservationTime.createWithId(2L, LocalTime.of(11, 0))
                        )
                );
        // when
        List<TimeConditionResponse> responses = reservationTimeService.getTimesWithCondition(
                new TimeConditionRequest(localDate, themeId));
        // then
        assertThat(responses).containsExactlyInAnyOrder(
                new TimeConditionResponse(1L, LocalTime.of(10, 0), true),
                new TimeConditionResponse(2L, LocalTime.of(11, 0), false)
        );
    }
}
