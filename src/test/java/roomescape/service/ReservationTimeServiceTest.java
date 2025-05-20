package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.time.AvailableReservationTimeResponse;
import roomescape.dto.time.ReservationTimeCreateRequest;
import roomescape.dto.time.ReservationTimeResponse;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    private ReservationTime testReservationTime;
    private ReservationTimeCreateRequest createRequest;
    private LocalDate testDate;
    private Theme testTheme;
    private Member testMember;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        LocalTime time = LocalTime.of(14, 0); // 14:00
        testReservationTime = new ReservationTime(1L, time);
        createRequest = new ReservationTimeCreateRequest(time);
        
        testDate = LocalDate.now().plusDays(1);
        testTheme = new Theme(1L, "Test Theme", "Test Description", "test-thumbnail.jpg");
        testMember = new Member(1L, "Test User", "test@example.com", Role.USER, "password");
        testReservation = new Reservation(1L, testMember, testDate, testReservationTime, testTheme);
    }

    @Test
    @DisplayName("새로운 예약 시간을 생성할 수 있다")
    void createReservationTime_WithValidRequest_ReturnsReservationTimeResponse() {
        // given
        when(reservationTimeRepository.save(any(ReservationTime.class))).thenReturn(testReservationTime);

        // when
        ReservationTimeResponse response = reservationTimeService.createReservationTime(createRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(testReservationTime.getId());
        assertThat(response.startAt()).isEqualTo(testReservationTime.getStartAt());
    }

    @Test
    @DisplayName("중복된 시간으로 예약 시간을 생성하면 예외가 발생한다")
    void createReservationTime_WithDuplicateTime_ThrowsDuplicateContentException() {
        // given
        when(reservationTimeRepository.save(any(ReservationTime.class)))
                .thenThrow(new IllegalStateException("중복된 시간입니다"));

        // when & then
        assertThatThrownBy(() -> reservationTimeService.createReservationTime(createRequest))
                .isInstanceOf(DuplicateContentException.class)
                .hasMessageContaining("중복된 시간입니다");
    }

    @Test
    @DisplayName("모든 예약 시간을 조회할 수 있다")
    void findAllReservationTimes_ReturnsAllTimes() {
        // given
        ReservationTime time1 = testReservationTime;
        ReservationTime time2 = new ReservationTime(2L, LocalTime.of(16, 0));
        List<ReservationTime> times = Arrays.asList(time1, time2);

        when(reservationTimeRepository.findAll()).thenReturn(times);

        // when
        List<ReservationTimeResponse> responses = reservationTimeService.findAllReservationTimes();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(0).startAt()).isEqualTo(LocalTime.of(14, 0));
        assertThat(responses.get(1).id()).isEqualTo(2L);
        assertThat(responses.get(1).startAt()).isEqualTo(LocalTime.of(16, 0));
    }

    @Test
    @DisplayName("특정 날짜와 테마에 대한 가능한 예약 시간을 조회할 수 있다")
    void findAvailableReservationTimes_ReturnsAvailableTimes() {
        // given
        ReservationTime time1 = testReservationTime;
        ReservationTime time2 = new ReservationTime(2L, LocalTime.of(16, 0));
        List<ReservationTime> allTimes = Arrays.asList(time1, time2);

        List<Reservation> reservations = Collections.singletonList(testReservation);

        when(reservationTimeRepository.findAll()).thenReturn(allTimes);
        when(reservationRepository.findByDateAndThemeId(testDate, 1L)).thenReturn(reservations);

        // when
        List<AvailableReservationTimeResponse> responses = reservationTimeService.findAvailableReservationTimes(testDate, 1L);

        // then
        assertThat(responses).hasSize(2);
        
        // 첫 번째 시간은 이미 예약됨
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(0).startAt()).isEqualTo(LocalTime.of(14, 0));
        assertThat(responses.get(0).alreadyBooked()).isTrue();
        
        // 두 번째 시간은 예약 가능
        assertThat(responses.get(1).id()).isEqualTo(2L);
        assertThat(responses.get(1).startAt()).isEqualTo(LocalTime.of(16, 0));
        assertThat(responses.get(1).alreadyBooked()).isFalse();
    }

    @Test
    @DisplayName("예약 시간을 삭제할 수 있다")
    void deleteReservationTimeById_WithExistingId_DeletesTime() {
        // given
        Long timeId = 1L;
        when(reservationRepository.existsByTimeId(timeId)).thenReturn(false);
        when(reservationTimeRepository.existsById(timeId)).thenReturn(true);
        doNothing().when(reservationTimeRepository).deleteById(timeId);

        // when
        reservationTimeService.deleteReservationTimeById(timeId);

        // then
        verify(reservationTimeRepository, times(1)).deleteById(timeId);
    }

    @Test
    @DisplayName("이미 예약이 있는 시간을 삭제하면 예외가 발생한다")
    void deleteReservationTimeById_WithExistingReservation_ThrowsIllegalStateException() {
        // given
        Long timeId = 1L;
        when(reservationRepository.existsByTimeId(timeId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(timeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이 시간의 예약이 이미 존재합니다");
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하면 예외가 발생한다")
    void deleteReservationTimeById_WithNonExistingId_ThrowsNotFoundException() {
        // given
        Long timeId = 999L;
        when(reservationRepository.existsByTimeId(timeId)).thenReturn(false);
        when(reservationTimeRepository.existsById(timeId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(timeId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("등록된 시간만 삭제할 수 있습니다");
    }
}
