package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.member.Member;
import roomescape.model.member.Role;
import roomescape.model.theme.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.dao.MemberDao;
import roomescape.repository.dao.ReservationDao;
import roomescape.repository.dao.ReservationTimeDao;
import roomescape.repository.dao.ThemeDao;
import roomescape.repository.dto.ReservationRowDto;
import roomescape.service.dto.ReservationDto;
import roomescape.service.dto.ReservationTimeInfoDto;
import roomescape.service.fakedao.FakeMemberDao;
import roomescape.service.fakedao.FakeReservationDao;
import roomescape.service.fakedao.FakeReservationTimeDao;
import roomescape.service.fakedao.FakeThemeDao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ReservationServiceTest {

    private static final int INITIAL_RESERVATION_COUNT = 3;
    private static final int INITIAL_TIME_COUNT = 3;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        ThemeDao themeDao = new FakeThemeDao(new ArrayList<>(List.of(
                new Theme(1, "n1", "d1", "t1"),
                new Theme(2, "n2", "d2", "t2"),
                new Theme(3, "n3", "d3", "t3"))));
        ReservationTimeDao reservationTimeDao = new FakeReservationTimeDao(new ArrayList<>(List.of(
                new ReservationTime(1, LocalTime.of(1, 0)),
                new ReservationTime(2, LocalTime.of(2, 0)),
                new ReservationTime(3, LocalTime.now()))));
        MemberDao memberDao = new FakeMemberDao(new ArrayList<>(List.of(
                new Member(1, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER),
                new Member(1, "우테코", "wtc@gmail.com", "wtc123!!", Role.ADMIN))));
        ReservationDao reservationDao = new FakeReservationDao(new ArrayList<>(List.of(
                new ReservationRowDto(1L, LocalDate.of(2000, 1, 1), 1L, 1L, 1L),
                new ReservationRowDto(2L, LocalDate.of(2000, 1, 2), 2L, 2L, 2L),
                new ReservationRowDto(3L, LocalDate.of(9999, 9, 9), 1L, 1L, 2L))));
        reservationService = new ReservationService(new ReservationRepository(reservationDao, reservationTimeDao, themeDao, memberDao));
    }

    @DisplayName("모든 예약을 반환한다.")
    @Test
    void should_find_all_reservations() {
        List<Reservation> reservations = reservationService.findAllReservations();
        assertThat(reservations).hasSize(INITIAL_RESERVATION_COUNT);
    }

    @DisplayName("예약을 추가한다.")
    @Test
    void should_save_reservation() {
        ReservationDto reservationDto = new ReservationDto(LocalDate.of(3333, 3, 3), 1L, 1L, 1L);
        reservationService.saveReservation(reservationDto);
        assertThat(reservationService.findAllReservations()).hasSize(INITIAL_RESERVATION_COUNT + 1);
    }

    @DisplayName("현재 이전으로 예약하려 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_previous_date() {
        ReservationDto reservationDto = new ReservationDto(LocalDate.of(999, 9, 9), 1L, 1L, 1L);
        assertThatThrownBy(() -> reservationService.saveReservation(reservationDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 현재 이전 예약은 할 수 없습니다.");
    }

    @DisplayName("현재(날짜+시간)로 예약하려 하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_current_date() {
        ReservationDto reservationDto = new ReservationDto(LocalDate.now(), 3L, 1L, 1L);
        assertThatCode(() -> reservationService.saveReservation(reservationDto))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재 이후로 예약하려 하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_later_date() {
        ReservationDto reservationDto = new ReservationDto(LocalDate.of(3333, 12, 31), 1L, 1L, 1L);
        assertThatCode(() -> reservationService.saveReservation(reservationDto))
                .doesNotThrowAnyException();
    }

    @DisplayName("날짜와 시간이 중복되는 예약을 추가하려 할 때 예외가 발생한다.")
    @Test
    void should_throw_exception_when_add_exist_reservation() {
        ReservationDto reservationDto = new ReservationDto(LocalDate.of(9999, 9, 9), 1L, 1L, 1L);
        assertThatThrownBy(() -> reservationService.saveReservation(reservationDto))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 중복되는 예약은 추가할 수 없습니다.");
    }

    @DisplayName("예약을 삭제한다.")
    @Test
    void should_delete_reservation() {
        reservationService.deleteReservation(1L);
        assertThat(reservationService.findAllReservations()).hasSize(INITIAL_RESERVATION_COUNT - 1);
    }

    @DisplayName("존재하는 예약을 삭제하려 하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_reservation_time() {
        assertThatCode(() -> reservationService.deleteReservation(1L))
                .doesNotThrowAnyException();
    }

    @DisplayName("존재하지 않는 예약을 삭제하려 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_reservation_time() {
        assertThatThrownBy(() -> reservationService.deleteReservation(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 존재하지 않는 예약입니다.");
    }

    @DisplayName("예약 가능 상태를 담은 시간 정보를 반환한다.")
    @Test
    void should_return_times_with_book_state() {
        LocalDate date = LocalDate.of(9999, 9, 9);
        ReservationTimeInfoDto timesInfo = reservationService.findReservationTimesInformation(date, 1L);

        List<ReservationTime> bookedTimes = timesInfo.getBookedTimes();
        List<ReservationTime> notBookedTimes = timesInfo.getNotBookedTimes();
        assertThat(bookedTimes.size() + notBookedTimes.size()).isEqualTo(INITIAL_TIME_COUNT);
    }
}
