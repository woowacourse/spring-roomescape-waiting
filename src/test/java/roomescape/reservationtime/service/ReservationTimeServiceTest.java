package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.model.RoomEscapeException;
import roomescape.member.domain.Member;
import roomescape.member.role.MemberRole;
import roomescape.vo.Name;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.TimeRequest;
import roomescape.reservationtime.dto.TimeResponse;
import roomescape.reservationtime.exception.TimeExceptionCode;
import roomescape.reservationtime.repository.TimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    private static final LocalTime CURRENT_TIME = LocalTime.now();

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(17, 3));

    @InjectMocks
    private TimeService timeService;
    @Mock
    private TimeRepository timeRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("시간을 추가한다.")
    void addReservationTime() {
        when(timeRepository.save(any()))
                .thenReturn(time);

        TimeRequest timeRequest = new TimeRequest(time.getStartAt());
        TimeResponse timeResponse = timeService.addReservationTime(timeRequest);

        Assertions.assertThat(timeResponse.id())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("시간을 찾는다.")
    void findReservationTimes() {
        when(timeRepository.findAllByOrderByStartAt())
                .thenReturn(List.of(time));

        List<TimeResponse> timeResponses = timeService.findReservationTimes();

        Assertions.assertThat(timeResponses).hasSize(1);
    }

    @Test
    @DisplayName("중복된 예약 시간 생성 요청시 예외를 던진다.")
    void validation_ShouldThrowException_WhenStartAtIsDuplicated() {
        when(timeRepository.findByStartAt(any()))
                .thenReturn(Optional.of(time));

        TimeRequest timeRequest = new TimeRequest(CURRENT_TIME);
        assertThatThrownBy(() -> timeService.addReservationTime(timeRequest))
                .isExactlyInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("이미 존재하는 예약 시간입니다.");
    }

    @Test
    @DisplayName("시간을 지운다.")
    void removeReservationTime() {
        doNothing()
                .when(timeRepository)
                .deleteById(time.getId());

        assertDoesNotThrow(() -> timeService.removeReservationTime(time.getId()));
    }

    @Test
    @DisplayName("예약이 존재하는 예약 시간 삭제 요청시 예외를 던진다.")
    void validateReservationExistence_ShouldThrowException_WhenReservationExistAtTime() {
        List<Reservation> reservations = List.of(new Reservation(
                LocalDate.now().plusDays(1),
                new ReservationTime(1L, LocalTime.now()),
                new Theme(1L, new Name("테스트 테마"), "테마 설명", "썸네일"),
                new Member(1L, new Name("레모네"), "lemone@gmail.com", "lemon12", MemberRole.MEMBER))
        );

        when(reservationRepository.findByTimeId(1L))
                .thenReturn(reservations);

        Throwable reservationExistAtTime = assertThrows(
                RoomEscapeException.class,
                () -> timeService.removeReservationTime(1L));

        assertEquals(TimeExceptionCode.EXIST_RESERVATION_AT_CHOOSE_TIME.getMessage(),
                reservationExistAtTime.getMessage());
    }

}
