package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.NotFoundException;
import roomescape.model.ReservationTime;
import roomescape.model.Waiting;
import roomescape.model.member.LoginMember;
import roomescape.model.member.Member;
import roomescape.model.member.Role;
import roomescape.model.theme.Theme;
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

    @BeforeEach
    void setUp() { // TODO: reservation 추가 및 save 전 검증 테스트 추가
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
        ReservationDto reservationDto = new ReservationDto(LocalDate.of(3333, 3, 3), 1L, 1L, 1L);
        waitingService.saveWaiting(reservationDto);
        assertThat(waitingRepository.count()).isEqualTo(INITIAL_WAITING_COUNT + 1);
    }

    @DisplayName("예약을 삭제한다.")
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

    @DisplayName("특정 멤버의 예약을 조회한다.")
    @Test
    void should_find_all_reservation_waiting_of_member() {
        LoginMember member = new LoginMember(1L);
        List<Waiting> waiting = waitingService.findWaitingByMember(member);
        assertAll(
                () -> assertThat(waiting).hasSize(1),
                () -> assertThat(waiting.get(0).getId()).isEqualTo(1L));
    }
}
