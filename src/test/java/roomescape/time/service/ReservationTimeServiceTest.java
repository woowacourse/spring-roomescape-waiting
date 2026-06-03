package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.exception.TimeErrorCode;
import roomescape.time.service.dto.ReservationTimeCommand;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    // --- save() Tests ---

    @Test
    @DisplayName("예약 시간을 성공적으로 저장한다.")
    void save_success() {
        // given
        ReservationTimeCommand command = new ReservationTimeCommand(LocalTime.of(10, 0));
        ReservationTime savedTime = new ReservationTime(1L, LocalTime.of(10, 0));

        given(reservationTimeRepository.existsByStartAt(any(ReservationTime.class))).willReturn(false);
        given(reservationTimeRepository.save(any(ReservationTime.class))).willReturn(savedTime);

        // when
        reservationTimeService.save(command);

        // then
        then(reservationTimeRepository).should().existsByStartAt(any(ReservationTime.class));
        then(reservationTimeRepository).should().save(any(ReservationTime.class));
    }

    @Test
    @DisplayName("예약 시간 생성 시, 기존에 이미 동일한 시간이 있으면 예외가 발생한다.")
    void save_duplicateTime_throwsConflictException() {
        // given
        ReservationTimeCommand command = new ReservationTimeCommand(LocalTime.of(10, 0));

        given(reservationTimeRepository.existsByStartAt(any(ReservationTime.class))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.save(command))
                .isInstanceOf(ConflictException.class)
                .hasMessage(TimeErrorCode.DUPLICATE_TIME.getMessage());
    }

    @Test
    @DisplayName("예약 시간 생성 시, 데이터베이스 제약 조건 위반이 발생하면 예외가 발생한다.")
    void save_databaseDuplicate_throwsConflictException() {
        // given
        ReservationTimeCommand command = new ReservationTimeCommand(LocalTime.of(10, 0));

        given(reservationTimeRepository.existsByStartAt(any(ReservationTime.class))).willReturn(false);
        given(reservationTimeRepository.save(any(ReservationTime.class)))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        // when & then
        assertThatThrownBy(() -> reservationTimeService.save(command))
                .isInstanceOf(ConflictException.class)
                .hasMessage(TimeErrorCode.DUPLICATE_TIME.getMessage());
    }

    // --- getById() Tests ---

    @Test
    @DisplayName("ID에 해당하는 예약 시간을 성공적으로 조회한다.")
    void getById_success() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(time));

        // when
        reservationTimeService.getById(1L);

        // then
        then(reservationTimeRepository).should().findById(1L);
    }

    @Test
    @DisplayName("ID에 해당하는 예약 시간이 존재하지 않으면 예외가 발생한다.")
    void getById_notFound_throwsNotFoundException() {
        // given
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationTimeService.getById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TimeErrorCode.TIME_NOT_FOUND.getMessage());
    }

    // --- deleteById() Tests ---

    @Test
    @DisplayName("예약 시간을 성공적으로 삭제한다.")
    void deleteById_success() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(time));

        // when
        reservationTimeService.deleteById(1L);

        // then
        then(reservationTimeRepository).should().findById(1L);
        then(reservationTimeRepository).should().delete(time);
    }

    @Test
    @DisplayName("삭제하려는 예약 시간이 존재하지 않으면 예외가 발생한다.")
    void deleteById_notFound_throwsNotFoundException() {
        // given
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TimeErrorCode.TIME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("삭제하려는 예약 시간이 사용 중이면 예외가 발생한다.")
    void deleteById_timeInUse_throwsConflictException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(time));
        willThrow(new DataIntegrityViolationException("foreign key"))
                .given(reservationTimeRepository).delete(time);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessage(TimeErrorCode.TIME_IN_USE.getMessage());
    }
}
