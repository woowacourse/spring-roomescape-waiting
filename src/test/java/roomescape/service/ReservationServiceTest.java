package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
import roomescape.model.member.LoginMember;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.ReservationDto;
import roomescape.service.dto.ReservationTimeInfoDto;

@Sql("/init.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationServiceTest {

    private static final int INITIAL_RESERVATION_COUNT = 3;
    private static final int INITIAL_TIME_COUNT = 3;

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository.saveAll(List.of(
                new ReservationTime(LocalTime.of(1, 0)),
                new ReservationTime(LocalTime.of(2, 0)),
                new ReservationTime(LocalTime.now())));

        reservationRepository.saveAll(List.of(
                new Reservation(LocalDate.of(2000, 1, 1),
                        new ReservationTime(1L, null),
                        new Theme(1L, null, null, null),
                        new Member(1L, null, null, null, null)),
                new Reservation(LocalDate.of(2000, 1, 2),
                        new ReservationTime(2L, null),
                        new Theme(2L, null, null, null),
                        new Member(2L, null, null, null, null)),
                new Reservation(LocalDate.of(9999, 9, 9),
                        new ReservationTime(1L, null),
                        new Theme(1L, null, null, null),
                        new Member(2L, null, null, null, null))));
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

    @DisplayName("특정 멤버, 테마, 날짜 조건에 따라 예약을 조회한다.")
    @Test
    void should_find_reservations_by_memberId_and_themeId_and_date() {
        List<Reservation> reservations = reservationService
                .findReservationsByConditions(1L, 1L, LocalDate.of(999, 1, 1), LocalDate.of(3000, 1, 1));
        assertAll(
                () -> assertThat(reservations).hasSize(1),
                () -> assertThat(reservations.get(0).getId()).isEqualTo(1L));
    }

    @DisplayName("특정 멤버의 예약을 조회한다.")
    @Test
    void should_find_reservations_by_member() {
        LoginMember member = new LoginMember(1L);
        List<Reservation> reservations = reservationService.findReservationsByMember(member);
        assertAll(
                () -> assertThat(reservations).hasSize(1),
                () -> assertThat(reservations.get(0).getId()).isEqualTo(1L));
    }
}
