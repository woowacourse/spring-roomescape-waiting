package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import roomescape.controller.dto.ReservationTimeRequest;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.ReservationTimeDao;
import roomescape.repository.ScheduleDao;
import roomescape.service.dto.AvailableTimeResult;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimeDao reservationTimeDao;

    @Mock
    private ScheduleDao scheduleDao;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @DisplayName("중복 시간이 아니면 예약 시간을 저장한다.")
    @Test
    void saveReservationTime() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));
        given(reservationTimeDao.existsByStartAt(request.startAt())).willReturn(false);
        given(reservationTimeDao.save(request.startAt())).willReturn(1L);

        Long timeId = reservationTimeService.saveReservationTime(request);

        assertThat(timeId).isEqualTo(1L);
    }

    @DisplayName("이미 존재하는 시간은 저장하지 않는다.")
    @Test
    void saveDuplicateReservationTime() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));
        given(reservationTimeDao.existsByStartAt(request.startAt())).willReturn(true);

        assertThatThrownBy(() -> reservationTimeService.saveReservationTime(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.DUPLICATE_RESERVATION_TIME);

        verify(reservationTimeDao, never()).save(any());
    }

    @DisplayName("DB 유니크 제약 예외도 도메인 예외로 변환한다.")
    @Test
    void saveReservationTimeDuplicateKeyException() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));
        given(reservationTimeDao.existsByStartAt(request.startAt())).willReturn(false);
        given(reservationTimeDao.save(request.startAt())).willThrow(new DuplicateKeyException("duplicate"));

        assertThatThrownBy(() -> reservationTimeService.saveReservationTime(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.DUPLICATE_RESERVATION_TIME);
    }

    @DisplayName("스케줄이 참조하는 예약 시간은 삭제할 수 없다.")
    @Test
    void deleteReservationTimeReferencedBySchedule() {
        given(scheduleDao.existsByTimeId(1L)).willReturn(true);

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.REFERENTIAL_INTEGRITY);

        verify(reservationTimeDao, never()).delete(1L);
    }

    @DisplayName("삭제 중 무결성 예외가 발생하면 도메인 예외로 변환한다.")
    @Test
    void deleteReservationTimeDataIntegrityViolationException() {
        given(scheduleDao.existsByTimeId(1L)).willReturn(false);
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("referenced"))
                .when(reservationTimeDao)
                .delete(1L);

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.REFERENTIAL_INTEGRITY);
    }

    @DisplayName("예약 시간 목록을 조회한다.")
    @Test
    void findAll() {
        given(reservationTimeDao.findAll()).willReturn(List.of(new ReservationTime(1L, LocalTime.of(10, 0))));

        List<ReservationTime> times = reservationTimeService.findAll();

        assertThat(times).hasSize(1);
        assertThat(times.get(0).getId()).isEqualTo(1L);
        assertThat(times.get(0).getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @DisplayName("시간별 예약 수를 조회한다.")
    @Test
    void findAvailableTimes() {
        LocalDate date = LocalDate.of(2026, 7, 1);
        given(reservationTimeDao.findAvailableTimes(1L, date, ReservationStatus.CANCELED))
                .willReturn(List.of(
                        new AvailableTimeResult(1L, LocalTime.of(10, 0), 0),
                        new AvailableTimeResult(2L, LocalTime.of(11, 0), 1),
                        new AvailableTimeResult(3L, LocalTime.of(12, 0), 3)
                ));

        List<AvailableTimeResult> results = reservationTimeService.findAvailableTimes(1L, date);

        assertThat(results).extracting(AvailableTimeResult::reservationCount)
                .containsExactly(0, 1, 3);
    }
}
