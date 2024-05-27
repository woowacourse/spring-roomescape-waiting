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
import roomescape.model.Waiting;
import roomescape.model.member.LoginMember;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.ReservationDto;

@Sql("/init.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WaitingServiceTest {

    private static final int INITIAL_WAITING_COUNT = 1;

    @Autowired
    private WaitingService waitingService;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private WaitingRepository waitingRepository;

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
                        new Member(2L, null, null, null, null)),
                new Reservation(LocalDate.now(),
                        new ReservationTime(3L, null),
                        new Theme(3L, null, null, null),
                        new Member(1L, null, null, null, null))
        ));

        waitingRepository.saveAll(List.of(
                new Waiting(new ReservationDto(LocalDate.of(9999, 9, 9), 1L, 1L, 3L))));
    }

    @DisplayName("모든 대기를 반환한다.")
    @Test
    void should_find_all_waitings() {
        List<Waiting> waitings = waitingService.findAllWaitings();
        assertThat(waitings).hasSize(INITIAL_WAITING_COUNT);
    }

    @DisplayName("대기를 추가한다.")
    @Test
    void should_save_waiting() {
        ReservationDto reservationDto = new ReservationDto(LocalDate.of(9999, 9, 9), 1L, 1L, 1L);
        waitingService.saveWaiting(reservationDto);
        assertThat(waitingService.findAllWaitings()).hasSize(INITIAL_WAITING_COUNT + 1);
    }

    @DisplayName("현재 이전으로 대기하려 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_previous_date() {
        ReservationDto reservationDto = new ReservationDto(LocalDate.of(2000, 1, 1), 1L, 1L, 2L);
        assertThatThrownBy(() -> waitingService.saveWaiting(reservationDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 현재 이전 대기는 할 수 없습니다.");
    }

    @DisplayName("현재(날짜+시간)로 대기하려 하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_current_date() {
        ReservationDto reservationDto = new ReservationDto(LocalDate.now(), 3L, 3L, 2L);
        assertThatCode(() -> waitingService.saveWaiting(reservationDto))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재 이후로 대기하려 하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_later_date() {
        ReservationDto reservationDto = new ReservationDto(LocalDate.of(9999, 9, 9), 1L, 1L, 1L);
        assertThatCode(() -> waitingService.saveWaiting(reservationDto))
                .doesNotThrowAnyException();
    }

    @DisplayName("동일한 사용자가 날짜와 시간이 중복되는 대기를 추가하려 할 때 예외가 발생한다.")
    @Test
    void should_throw_exception_when_add_exist_waiting() {
        ReservationDto reservationDto = new ReservationDto(LocalDate.of(9999, 9, 9), 1L, 1L, 3L);
        assertThatThrownBy(() -> waitingService.saveWaiting(reservationDto))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 이미 예약 대기 이력이 존재합니다.");
    }

    @DisplayName("본인의 예약에 대기를 추가하려 할 때 예외가 발생한다.")
    @Test
    void should_throw_exception_when_add_exist_reservation() {
        ReservationDto reservationDto = new ReservationDto(LocalDate.of(9999, 9, 9), 1L, 1L, 2L);
        assertThatThrownBy(() -> waitingService.saveWaiting(reservationDto))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 본인의 예약에는 대기를 할 수 없습니다.");
    }

    @DisplayName("존재하지 않는 예약에 대기를 추가하려 할 때 예외가 발생한다.")
    @Test
    void should_throw_exception_when_add_not_exist_reservation() {
        ReservationDto reservationDto = new ReservationDto(LocalDate.of(9999, 9, 9), 2L, 2L, 2L);
        assertThatThrownBy(() -> waitingService.saveWaiting(reservationDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 예약이 존재하지 않아 예약 대기가 불가합니다.");
    }

    @DisplayName("대기를 삭제한다.")
    @Test
    void should_delete_waiting() {
        waitingService.deleteWaiting(1L);
        assertThat(waitingService.findAllWaitings()).hasSize(INITIAL_WAITING_COUNT - 1);
    }

    @DisplayName("존재하는 대기를 삭제하려 하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_waiting() {
        assertThatCode(() -> waitingService.deleteWaiting(1L))
                .doesNotThrowAnyException();
    }

    @DisplayName("존재하지 않는 대기를 삭제하려 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_waiting() {
        assertThatThrownBy(() -> waitingService.deleteWaiting(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 존재하지 않는 대기입니다.");
    }

    @DisplayName("특정 멤버의 예약을 조회한다.")
    @Test
    void should_find_waitings_by_member() {
        LoginMember member = new LoginMember(3L);
        List<Waiting> waitings = waitingService.findWaitingsByMember(member);
        assertAll(
                () -> assertThat(waitings).hasSize(1),
                () -> assertThat(waitings.get(0).getId()).isEqualTo(1L));
    }
}
