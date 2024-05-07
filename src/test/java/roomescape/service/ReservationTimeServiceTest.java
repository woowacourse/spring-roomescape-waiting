package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.ReservationTime;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.dao.ReservationDao;
import roomescape.repository.dao.ReservationTimeDao;
import roomescape.repository.dto.ReservationRowDto;
import roomescape.service.dto.ReservationTimeDto;
import roomescape.service.fakedao.FakeReservationDao;
import roomescape.service.fakedao.FakeReservationTimeDao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ReservationTimeServiceTest {

    private static final int INITIAL_TIME_COUNT = 3;

    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp() {
        ReservationTimeDao reservationTimeDao = new FakeReservationTimeDao(new ArrayList<>(List.of(
                new ReservationTime(1, LocalTime.of(1, 0)),
                new ReservationTime(2, LocalTime.of(2, 0)),
                new ReservationTime(3, LocalTime.of(3, 0)))));
        ReservationDao reservationDao = new FakeReservationDao(new ArrayList<>(List.of(
                new ReservationRowDto(1, LocalDate.of(2000, 1, 1), 1L, 1L, 1L),
                new ReservationRowDto(2, LocalDate.of(2000, 1, 2), 2L, 2L, 2L))));
        reservationTimeService = new ReservationTimeService(new ReservationTimeRepository(reservationDao, reservationTimeDao));
    }

    @DisplayName("모든 예약 시간을 조회한다.")
    @Test
    void should_find_all_reservation_times() {
        List<ReservationTime> reservationTimes = reservationTimeService.findAllReservationTimes();
        assertThat(reservationTimes).hasSize(INITIAL_TIME_COUNT);
    }

    @DisplayName("특정 id에 해당하는 예약 시간을 조회한다.")
    @Test
    void should_find_reservation_time_by_id() {
        ReservationTime expected = new ReservationTime(1, LocalTime.of(1, 0));
        ReservationTime actual = reservationTimeService.findReservationTime(1);
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("존재하지 않는 시간을 조회하려는 경우 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_time() {
        assertThatThrownBy(() -> reservationTimeService.findReservationTime(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 존재하지 않는 시간입니다.");
    }

    @DisplayName("예약 시간을 추가한다.")
    @Test
    void should_save_reservation_time() {
        ReservationTimeDto timeDto = new ReservationTimeDto(LocalTime.of(4, 0));
        reservationTimeService.saveReservationTime(timeDto);
        assertThat(reservationTimeService.findAllReservationTimes()).hasSize(INITIAL_TIME_COUNT + 1);
    }

    @DisplayName("중복된 시간을 추가하려는 경우 예외가 발생한다.")
    @Test
    void should_throw_exception_when_duplicated_time() {
        ReservationTimeDto timeDto = new ReservationTimeDto(LocalTime.of(1, 0));
        assertThatThrownBy(() -> reservationTimeService.saveReservationTime(timeDto))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 중복된 시간은 추가할 수 없습니다.");
    }

    @DisplayName("예약 시간을 삭제한다.")
    @Test
    void should_delete_reservation_time() {
        reservationTimeService.deleteReservationTime(3L);
        assertThat(reservationTimeService.findAllReservationTimes()).hasSize(INITIAL_TIME_COUNT - 1);
    }

    @DisplayName("예약 시간을 삭제하려 할 때 특정 id를 가진 예약 시간이 존재하는 경우 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_id() {
        assertThatCode(() -> reservationTimeService.deleteReservationTime(3L))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약 시간을 삭제하려 할 때 특정 id를 가진 예약 시간이 존재하지 않는 경우 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_id() {
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 존재하지 않는 시간입니다.");
    }

    @DisplayName("예약 시간을 삭제하려 할 때 해당 시간을 사용하는 예약이 존재하는 경우 예외가 발생한다.")
    @Test
    void should_throw_exception_when_exist_reservation_using_time() {
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 해당 시간을 사용하고 있는 예약이 있습니다.");
    }

    @DisplayName("이미 존재하는 예약 시간을 중복으로 추가하려 할 때 예외가 발생한다.")
    @Test
    void should_throw_exception_when_add_exist_time() {
        ReservationTimeDto timeDto = new ReservationTimeDto(LocalTime.of(1, 0));
        assertThatThrownBy(() -> reservationTimeService.saveReservationTime(timeDto))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 중복된 시간은 추가할 수 없습니다.");
    }
}
