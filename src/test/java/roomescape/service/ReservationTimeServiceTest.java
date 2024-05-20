package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.member.Member;
import roomescape.model.member.Role;
import roomescape.model.theme.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.ReservationTimeDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Sql("/init.sql")
@SpringBootTest
class ReservationTimeServiceTest {

    private static final int INITIAL_TIME_COUNT = 3;

    @Autowired
    private ReservationTimeService reservationTimeService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository.saveAll(List.of(
                new ReservationTime(LocalTime.of(3, 0))));

        reservationRepository.saveAll(List.of(
                new Reservation(
                        LocalDate.of(2000, 1, 1),
                        new ReservationTime(1L, LocalTime.of(1, 0)),
                        new Theme(1L, "n1", "d1", "t1"),
                        new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER)),
                new Reservation(
                        LocalDate.of(2000, 1, 2),
                        new ReservationTime(2L, LocalTime.of(2, 0)),
                        new Theme(2L, "n2", "d2", "t2"),
                        new Member(2L, "우테코", "wtc@gmail.com", "wtc123!", Role.ADMIN))));
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
        ReservationTime expected = new ReservationTime(1L, LocalTime.of(1, 0));
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
