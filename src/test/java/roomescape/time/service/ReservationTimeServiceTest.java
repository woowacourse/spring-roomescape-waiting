package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.exception.TimeErrorCode;
import roomescape.time.service.dto.ReservationTimeCommand;
import roomescape.time.service.dto.ReservationTimeResult;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("save returns the created reservation time.")
    void save_success() {
        // given
        ReservationTimeCommand command = new ReservationTimeCommand(LocalTime.of(10, 0));
        ReservationTime savedTime = new ReservationTime(1L, LocalTime.of(10, 0));

        given(reservationTimeRepository.save(any(ReservationTime.class))).willReturn(savedTime);

        // when
        ReservationTimeResult result = reservationTimeService.save(command);

        // then
        assertThat(result).isEqualTo(new ReservationTimeResult(1L, LocalTime.of(10, 0)));
        then(reservationTimeRepository).should().save(any(ReservationTime.class));
    }



    @Test
    @DisplayName("save throws ConflictException when the database rejects a duplicate time.")
    void save_databaseDuplicate_throwsConflictException() {
        // given
        ReservationTimeCommand command = new ReservationTimeCommand(LocalTime.of(10, 0));

        given(reservationTimeRepository.save(any(ReservationTime.class)))
                .willThrow(new ConflictException(TimeErrorCode.DUPLICATE_TIME));

        // when & then
        assertThatThrownBy(() -> reservationTimeService.save(command))
                .isInstanceOf(ConflictException.class)
                .hasMessage(TimeErrorCode.DUPLICATE_TIME.getMessage());
    }

    @Test
    @DisplayName("getById returns the reservation time when it exists.")
    void getById_success() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(time));

        // when
        ReservationTime result = reservationTimeService.getById(1L);

        // then
        assertThat(result).isEqualTo(time);
        then(reservationTimeRepository).should().findById(1L);
    }

    @Test
    @DisplayName("getById throws NotFoundException when the time does not exist.")
    void getById_notFound_throwsNotFoundException() {
        // given
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationTimeService.getById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TimeErrorCode.TIME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("deleteById deletes the reservation time when it exists.")
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
    @DisplayName("deleteById throws NotFoundException when the time does not exist.")
    void deleteById_notFound_throwsNotFoundException() {
        // given
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TimeErrorCode.TIME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("deleteById throws ConflictException when the time is in use.")
    void deleteById_timeInUse_throwsConflictException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(time));
        willThrow(new ConflictException(TimeErrorCode.TIME_IN_USE))
                .given(reservationTimeRepository).delete(time);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessage(TimeErrorCode.TIME_IN_USE.getMessage());
    }
}
