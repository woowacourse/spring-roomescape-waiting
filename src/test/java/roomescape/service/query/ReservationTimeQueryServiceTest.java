package roomescape.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import roomescape.domain.reservation.slot.ReservationTime;
import roomescape.domain.reservation.slot.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.dto.time.AvailableReservationTimeResponseDto;
import roomescape.dto.time.ReservationTimeResponseDto;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;

class ReservationTimeQueryServiceTest {

    @Mock
    private JpaReservationTimeRepository reservationTimeRepository;

    @Mock
    private JpaReservationRepository reservationRepository;

    @InjectMocks
    private ReservationTimeQueryService reservationTimeQueryService;

    private ReservationTime time1;
    private ReservationTime time2;
    private Theme theme;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        time1 = new ReservationTime(1L, LocalTime.of(14, 0));
        time2 = new ReservationTime(2L, LocalTime.of(16, 0));
        theme = new Theme(1L, "공포 테마", "무서운 배경 설명", "image-url");
    }

    @DisplayName("모든 예약 시간 조회 테스트")
    @Test
    void findAllReservationTimes() {
        // given
        List<ReservationTime> allTimes = List.of(time1, time2);
        when(reservationTimeRepository.findAll()).thenReturn(allTimes);
        
        // when
        List<ReservationTimeResponseDto> result = reservationTimeQueryService.findAllReservationTimes();
        
        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(time1.getId());
        assertThat(result.get(0).startAt()).isEqualTo(time1.getStartAt());
        assertThat(result.get(1).id()).isEqualTo(time2.getId());
        assertThat(result.get(1).startAt()).isEqualTo(time2.getStartAt());
    }
    
    @DisplayName("날짜와 테마별 예약 가능 시간 조회 테스트")
    @Test
    void findAllReservationTimesWithAvailabilityBy() {
        // given
        LocalDate date = LocalDate.now();
        Long themeId = 1L;
        
        List<ReservationTime> allTimes = List.of(time1, time2);
        
        Member member = new Member(1L, "홍길동", "hong@example.com", Role.USER, "password");
        Reservation bookedReservation = new Reservation(1L, member, date, time1, theme, ReservationStatus.RESERVED);
        
        List<Reservation> bookedReservations = List.of(bookedReservation);
        
        when(reservationTimeRepository.findAll()).thenReturn(allTimes);
        when(reservationRepository.findReservationsByDateAndThemeId(date, themeId))
                .thenReturn(bookedReservations);
        
        // when
        List<AvailableReservationTimeResponseDto> result = 
                reservationTimeQueryService.findAllReservationTimesWithAvailabilityBy(date, themeId);
        
        // then
        assertThat(result).hasSize(2);
        
        // time1 is already booked
        assertThat(result.get(0).id()).isEqualTo(time1.getId());
        assertThat(result.get(0).startAt()).isEqualTo(time1.getStartAt());
        assertThat(result.get(0).alreadyBooked()).isTrue();
        
        // time2 is available
        assertThat(result.get(1).id()).isEqualTo(time2.getId());
        assertThat(result.get(1).startAt()).isEqualTo(time2.getStartAt());
        assertThat(result.get(1).alreadyBooked()).isFalse();
    }
    
    @DisplayName("예약이 없는 날짜와 테마에 대한 예약 가능 시간 조회 테스트")
    @Test
    void findAllReservationTimesWithAvailabilityByNoBookings() {
        // given
        LocalDate date = LocalDate.now();
        Long themeId = 1L;
        
        List<ReservationTime> allTimes = List.of(time1, time2);
        List<Reservation> noReservations = List.of();
        
        when(reservationTimeRepository.findAll()).thenReturn(allTimes);
        when(reservationRepository.findReservationsByDateAndThemeId(date, themeId))
                .thenReturn(noReservations);
        
        // when
        List<AvailableReservationTimeResponseDto> result = 
                reservationTimeQueryService.findAllReservationTimesWithAvailabilityBy(date, themeId);
        
        // then
        assertThat(result).hasSize(2);
        
        // All times are available
        assertThat(result.get(0).id()).isEqualTo(time1.getId());
        assertThat(result.get(0).alreadyBooked()).isFalse();
        
        assertThat(result.get(1).id()).isEqualTo(time2.getId());
        assertThat(result.get(1).alreadyBooked()).isFalse();
    }
}