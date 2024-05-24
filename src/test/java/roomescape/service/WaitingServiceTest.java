package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Waiting;
import roomescape.model.WaitingWithRank;
import roomescape.model.member.LoginMember;
import roomescape.model.member.Member;
import roomescape.model.member.Role;
import roomescape.model.theme.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.ReservationDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql("/init.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WaitingServiceTest {

    private static final int INITIAL_WAITING_COUNT = 3;

    @Autowired
    private WaitingService waitingService;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() { // TODO: reservation 추가 및 save 전 검증 테스트 추가
        // TODO: waiting 보다 Reservation 이 우선 들어와야한다는 것이 테이블 설계만을 통해서는 드러나지 않는다. -> 외래키의 이점
        memberRepository.save(new Member("후에버", "whoever@gmail.com", "whoever123!", Role.USER)); // TODO
        reservationRepository.saveAll(List.of(
                new Reservation(
                        LocalDate.of(2000, 1, 1),
                        new ReservationTime(1L, LocalTime.of(1, 0)),
                        new Theme(1L, "n1", "d1", "t1"),
                        new Member(2L, "우테코", "wtc@gmail.com", "wtc123!", Role.ADMIN)),
                new Reservation(
                        LocalDate.of(2000, 1, 2),
                        new ReservationTime(2L, LocalTime.of(2, 0)),
                        new Theme(2L, "n2", "d2", "t2"),
                        new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER)),
                new Reservation(
                        LocalDate.of(9999, 9, 9),
                        new ReservationTime(1L, LocalTime.of(1, 0)),
                        new Theme(1L, "n1", "d1", "t1"),
                        new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER))));
        waitingRepository.saveAll(List.of(
                new Waiting(
                        LocalDate.of(2000, 1, 1),
                        new ReservationTime(1L, LocalTime.of(1, 0)),
                        new Theme(1L, "n1", "d1", "t1"),
                        new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER)),
                new Waiting(
                        LocalDate.of(2000, 1, 2),
                        new ReservationTime(2L, LocalTime.of(2, 0)),
                        new Theme(2L, "n2", "d2", "t2"),
                        new Member(2L, "우테코", "wtc@gmail.com", "wtc123!", Role.ADMIN)),
                new Waiting(
                        LocalDate.of(9999, 9, 9),
                        new ReservationTime(1L, LocalTime.of(1, 0)),
                        new Theme(1L, "n1", "d1", "t1"),
                        new Member(2L, "우테코", "wtc@gmail.com", "wtc123!", Role.ADMIN))));
    }

    @DisplayName("예약 대기를 추가한다.")
    @Test
    @Transactional // TODO: study
    void should_save_reservation_waiting() {
        ReservationDto reservationDto = new ReservationDto(LocalDate.of(9999, 9, 9), 1L, 1L, 3L);
        waitingService.saveWaiting(reservationDto);
        assertThat(waitingRepository.count()).isEqualTo(INITIAL_WAITING_COUNT + 1);
    }

    @DisplayName("본인의 예약에 대한 대기를 저장하려 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_reservation_owner() {
        ReservationDto waitingDto = new ReservationDto(
                LocalDate.of(9999, 9, 9), 1L, 1L, 1L);

        assertThatThrownBy(() -> waitingService.saveWaiting(waitingDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 본인의 예약에는 대기할 수 없습니다.");
    }

    @DisplayName("과거의 예약 대기를 저장하려 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_past_waiting() {
        ReservationDto waitingDto = new ReservationDto(
                LocalDate.of(2000, 1, 1), 1L, 1L, 3L);

        assertThatThrownBy(() -> waitingService.saveWaiting(waitingDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 현재 이전 예약은 대기할 수 없습니다.");
    }

    @DisplayName("중복된 예약 대기를 저장하려 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_duplicated_waiting() {
        ReservationDto waitingDto = new ReservationDto(
                LocalDate.of(9999, 9, 9), 1L, 1L, 2L);

        assertThatThrownBy(() -> waitingService.saveWaiting(waitingDto))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 중복된 예약 대기는 추가할 수 없습니다.");
    }

    @DisplayName("예약 대기를 삭제한다.")
    @Test
    void should_delete_reservation() {
        waitingService.deleteWaiting(1L);
        assertThat(waitingRepository.count()).isEqualTo(INITIAL_WAITING_COUNT - 1);
    }

    @DisplayName("존재하는 예약을 삭제하려 하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_reservation_time() {
        assertThatCode(() -> waitingService.deleteWaiting(1L))
                .doesNotThrowAnyException();
    }

    @DisplayName("존재하지 않는 예약을 삭제하려 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_reservation_time() {
        assertThatThrownBy(() -> waitingService.deleteWaiting(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 존재하지 않는 예약 대기입니다.");
    }

    @DisplayName("본인의 소유가 아닌 예약 대기를 삭제하려 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_owned_reservation_waiting() {
        LoginMember otherMember = new LoginMember(2L);
        assertThatThrownBy(() -> waitingService.deleteOwnWaiting(1L, otherMember))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 해당 예약 대기의 소유자가 아닙니다.");
    }

    @DisplayName("특정 멤버의 예약을 조회한다.")
    @Test
    void should_find_all_reservation_waiting_of_member() {
        LoginMember member = new LoginMember(1L);
        List<WaitingWithRank> waiting = waitingService.findWaitingByMember(member);
        assertAll(
                () -> assertThat(waiting).hasSize(1),
                () -> assertThat(waiting.get(0).getWaiting().getId()).isEqualTo(1L));
    }
}
