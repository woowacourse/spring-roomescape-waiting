package roomescape.service.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import roomescape.domain.ReservationTime;
import roomescape.dto.time.ReservationTimeCreateRequestDto;
import roomescape.dto.time.ReservationTimeResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;

public class ReservationTimeCommandServiceTest {

    @Mock
    private JpaReservationTimeRepository reservationTimeRepository;

    @Mock
    private JpaReservationRepository reservationRepository;

    @InjectMocks
    private ReservationTimeCommandService reservationTimeCommandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("예약 시간 생성 성공 테스트")
    @Test
    void createReservationTime() {
        // given
        LocalTime startAt = LocalTime.of(14, 0);
        ReservationTimeCreateRequestDto requestDto = new ReservationTimeCreateRequestDto(startAt);
        ReservationTime reservationTime = requestDto.createWithoutId();
        ReservationTime savedTime = new ReservationTime(1L, startAt);

        when(reservationTimeRepository.save(any(ReservationTime.class))).thenReturn(savedTime);

        // when
        ReservationTimeResponseDto response = reservationTimeCommandService.createReservationTime(requestDto);

        // then
        assertEquals(savedTime.getId(), response.id());
        assertEquals(savedTime.getStartAt(), response.startAt());
    }

    @DisplayName("중복된 예약 시간 생성 시 예외 발생")
    @Test
    void createDuplicateReservationTime() {
        // given
        LocalTime startAt = LocalTime.of(14, 0);
        ReservationTimeCreateRequestDto requestDto = new ReservationTimeCreateRequestDto(startAt);

        when(reservationTimeRepository.save(any(ReservationTime.class))).thenThrow(new IllegalStateException("중복된 시간입니다."));

        // when & then
        assertThrows(DuplicateContentException.class, () -> reservationTimeCommandService.createReservationTime(requestDto));
    }

    @DisplayName("예약 시간 삭제 성공 테스트")
    @Test
    void deleteReservationTimeById() {
        // given
        Long timeId = 1L;

        when(reservationRepository.existsByTimeId(timeId)).thenReturn(false);
        when(reservationTimeRepository.existsById(timeId)).thenReturn(true);

        // when
        reservationTimeCommandService.deleteReservationTimeById(timeId);

        // then
        verify(reservationTimeRepository).deleteById(timeId);
    }

    @DisplayName("예약이 존재하는 시간 삭제 시 예외 발생")
    @Test
    void deleteReservationTimeWithExistingReservations() {
        // given
        Long timeId = 1L;

        when(reservationRepository.existsByTimeId(timeId)).thenReturn(true);

        // when & then
        assertThrows(IllegalStateException.class, () -> reservationTimeCommandService.deleteReservationTimeById(timeId));
        verify(reservationTimeRepository, never()).deleteById(timeId);
    }

    @DisplayName("존재하지 않는 예약 시간 삭제 시 예외 발생")
    @Test
    void deleteNonExistentReservationTime() {
        // given
        Long timeId = 999L;

        when(reservationRepository.existsByTimeId(timeId)).thenReturn(false);
        when(reservationTimeRepository.existsById(timeId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> reservationTimeCommandService.deleteReservationTimeById(timeId));
        verify(reservationTimeRepository, never()).deleteById(timeId);
    }
}
