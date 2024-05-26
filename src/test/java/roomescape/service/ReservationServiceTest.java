package roomescape.service;

import static java.time.LocalDate.now;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import static roomescape.model.ReservationStatus.ACCEPT;
import static roomescape.model.ReservationStatus.CANCEL;
import static roomescape.model.ReservationStatus.WAITING;
import static roomescape.service.fixture.TestMemberFactory.createAdmin;
import static roomescape.service.fixture.TestMemberFactory.createMember;
import static roomescape.service.fixture.TestReservationFactory.createAcceptReservation;
import static roomescape.service.fixture.TestReservationFactory.createAcceptReservationAtNow;
import static roomescape.service.fixture.TestReservationFactory.createWaiting;
import static roomescape.service.fixture.TestReservationTimeFactory.createReservationTime;
import static roomescape.service.fixture.TestThemeFactory.createTheme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import roomescape.controller.request.AdminReservationRequest;
import roomescape.controller.request.ReservationRequest;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.MemberReservation;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/init-data.sql")
class ReservationServiceTest {

    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationService reservationService;

    @DisplayName("모든 예약을 반환한다")
    @Test
    void should_return_all_reservation_times() {
        Theme theme1 = themeRepository.save(createTheme(1L));
        Theme theme2 = themeRepository.save(createTheme(2L));
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        Member member = memberRepository.save(createMember(1L));
        reservationRepository.save(createAcceptReservationAtNow(1L, time, theme1, member));
        reservationRepository.save(createAcceptReservationAtNow(2L, time, theme2, member));

        List<Reservation> reservations = reservationService.findAllReservations();

        assertThat(reservations).hasSize(2);
    }

    @DisplayName("검색 조건에 맞는 예약을 반환한다.")
    @Test
    void should_return_filtered_reservation() {
        Theme theme = themeRepository.save(createTheme(1L));
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        Member member = memberRepository.save(createMember(1L));
        reservationRepository.save(createAcceptReservationAtNow(1L, time, theme, member));
        reservationRepository.save(createAcceptReservationAtNow(2L, time, theme, member));

        List<Reservation> reservations = reservationService
                .filterReservation(1L, 1L, now().minusDays(1), now().plusDays(3));

        assertThat(reservations).hasSize(2);
    }

    @DisplayName("사용자가 예약을 추가한다")
    @Test
    void should_add_reservation_times_when_give_member_request() {
        themeRepository.save(createTheme(1L));
        reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        Member member = memberRepository.save(createMember(1L));

        ReservationRequest request = new ReservationRequest(now().plusDays(2), 1L, 1L);
        reservationService.addReservation(request, member);

        List<Reservation> allReservations = reservationRepository.findAll();
        assertSoftly(assertions -> {
            assertions.assertThat(allReservations).hasSize(1);
            assertions.assertThat(allReservations).extracting("status").containsExactly(ACCEPT);
        });
    }

    @DisplayName("사용자가 대기상태의 예약을 추가한다.")
    @Test
    void should_add_waiting_reservation_times_when_give_member_request() {
        themeRepository.save(createTheme(1L));
        reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        Member member = memberRepository.save(createMember(1L));
        ReservationRequest request = new ReservationRequest(now().plusDays(2), 1L, 1L);

        reservationService.addWaitingReservation(request, member);

        List<Reservation> allReservations = reservationRepository.findAll();
        assertSoftly(assertions -> {
            assertions.assertThat(allReservations).hasSize(1);
            assertions.assertThat(allReservations).extracting("status").containsExactly(WAITING);
        });
    }

    @DisplayName("이미 예약대기를 걸어 놓은 사용자가 또 다시 예약을 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_user_add_duplicated_waiting_reservation() {
        Theme theme = themeRepository.save(createTheme(1L));
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        Member member = memberRepository.save(createMember(1L));
        LocalDate reservationDate = now().plusDays(2);
        reservationRepository.save(createWaiting(1L, reservationDate, time, theme, member));

        ReservationRequest request = new ReservationRequest(reservationDate, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addWaitingReservation(request, member))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 이미 예약을 했거나 예약 대기를 걸어놓았습니다.");
    }

    @DisplayName("이미 예약을 걸어 놓은 사용자가 또 다시 예약을 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_user_add_duplicated_reservation() {
        Theme theme = themeRepository.save(createTheme(1L));
        Member member = memberRepository.save(createMember(1L));
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        LocalDate reservationDate = now().plusDays(2);
        reservationRepository.save(createAcceptReservation(1L, reservationDate, time, theme, member));

        ReservationRequest request = new ReservationRequest(reservationDate, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addWaitingReservation(request, member))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 이미 예약을 했거나 예약 대기를 걸어놓았습니다.");
    }


    @DisplayName("관리자가 예약을 추가한다")
    @Test
    void should_add_reservation_times_when_give_admin_request() {
        themeRepository.save(createTheme(1L));
        reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        memberRepository.save(createAdmin(1L));
        AdminReservationRequest request =
                new AdminReservationRequest(now().plusDays(2), 1L, 1L, 1L);

        reservationService.addReservation(request);

        List<Reservation> allReservations = reservationRepository.findAll();
        assertThat(allReservations).hasSize(1);
    }

    @DisplayName("관리자가 예약을 추가할 때 사용자가 없으면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_member() {
        themeRepository.save(createTheme(1L));
        reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        AdminReservationRequest request =
                new AdminReservationRequest(now().plusDays(2), 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservation(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 아이디가 1인 사용자가 존재하지 않습니다.");
    }

    @DisplayName("예약을 취소한다.")
    @Test
    void should_remove_reservation_times() {
        Theme theme1 = themeRepository.save(createTheme(1L));
        Theme theme2 = themeRepository.save(createTheme(2L));
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        Member member = memberRepository.save(createMember(1L));
        reservationRepository.save(createAcceptReservation(1L, now(), time, theme1, member));
        reservationRepository.save(createAcceptReservation(2L, now(), time, theme2, member));

        reservationService.deleteReservation(1L);

        List<Reservation> reservations = reservationRepository.findAll();
        SoftAssertions.assertSoftly(assertions -> {
            assertions.assertThat(reservations).hasSize(2);
            assertions.assertThat(reservations).extracting("status").containsExactly(CANCEL, ACCEPT);
        });
    }

    @DisplayName("존재하지 않는 예약을 삭제하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_reservation_time() {
        assertThatThrownBy(() -> reservationService.deleteReservation(1000000))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 해당 id:[1000000] 값으로 예약된 내역이 존재하지 않습니다.");
    }

    @DisplayName("승인된 예약을 취소하면 제일 먼저 예약된 대기가 승인으로 바뀐다.")
    @Test
    void should_confirm_first_waiting_reservation_when_delete_waiting_reservation() {
        Theme theme = themeRepository.save(createTheme(1L));
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        Member member1 = memberRepository.save(createMember(1L));
        Member member2 = memberRepository.save(createMember(2L));
        Member member3 = memberRepository.save(createMember(3L));

        reservationRepository.save(createAcceptReservation(1L, now(), time, theme, member1));
        reservationRepository.save(createWaiting(2L, now(), time, theme, member2));
        reservationRepository.save(createWaiting(3L, now(), time, theme, member3));

        reservationService.deleteReservation(1L);

        List<Reservation> reservations = reservationRepository.findAll();
        SoftAssertions.assertSoftly(assertions -> {
            assertions.assertThat(reservations).hasSize(3);
            assertions.assertThat(reservations).extracting("status").containsExactly(CANCEL, ACCEPT, WAITING);
            assertions.assertThat(reservations).extracting("id").containsExactly(1L, 2L, 3L);
        });
    }

    @DisplayName("승인된 예약을 취소하고 이후 대기가 없으면 단순히 취소만 한다.")
    @Test
    void should_only_cancel_reservation_when_no_waiting_reservations() {
        Theme theme = themeRepository.save(createTheme(1L));
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        Member member = memberRepository.save(createMember(1L));
        reservationRepository.save(createAcceptReservation(1L, now(), time, theme, member));

        reservationService.deleteReservation(1L);

        List<Reservation> reservations = reservationRepository.findAll();
        SoftAssertions.assertSoftly(assertions -> {
            assertions.assertThat(reservations).hasSize(1);
            assertions.assertThat(reservations).extracting("status").containsExactly(CANCEL);
        });
    }


    @DisplayName("예약 대기를 취소할 때는 단순히 예약 대기를 취소 상태로 변경한다.")
    @Test
    void should_cancel_reservation_with_no_change_when_delete_waiting_reservation() {
        Theme theme = themeRepository.save(createTheme(1L));
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        Member member1 = memberRepository.save(createMember(1L));
        Member member2 = memberRepository.save(createMember(2L));
        Member member3 = memberRepository.save(createMember(3L));
        reservationRepository.save(createAcceptReservation(1L, now(), time, theme, member1));
        reservationRepository.save(createWaiting(2L, now(), time, theme, member2));
        reservationRepository.save(createWaiting(3L, now(), time, theme, member3));

        reservationService.deleteReservation(2L);

        List<Reservation> reservations = reservationRepository.findAll();
        SoftAssertions.assertSoftly(assertions -> {
            assertions.assertThat(reservations).hasSize(3);
            assertions.assertThat(reservations).extracting("status").containsExactly(ACCEPT, CANCEL, WAITING);
        });
    }


    @DisplayName("존재하는 예약을 삭제하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_reservation_time() {
        Theme theme = themeRepository.save(createTheme(1L));
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        Member member = memberRepository.save(createMember(1L));
        reservationRepository.save(createAcceptReservation(1L, now(), time, theme, member));

        assertThatCode(() -> reservationService.deleteReservation(1))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재 이전으로 예약하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_previous_date() {
        themeRepository.save(createTheme(1L));
        reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        ReservationRequest request = new ReservationRequest(now().minusDays(1), 1L, 1L);
        Member member = memberRepository.save(createMember(1L));

        assertThatThrownBy(() -> reservationService.addReservation(request, member))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("[ERROR] 현재(", ") 이전 시간으로 예약할 수 없습니다.");
    }

    @DisplayName("현재로 예약하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_current_date() {
        themeRepository.save(createTheme(1L));
        themeRepository.save(createTheme(2L));
        themeRepository.save(createTheme(3L));
        reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        reservationTimeRepository.save(createReservationTime(2L, "11:00"));
        reservationTimeRepository.save(createReservationTime(3L, LocalTime.now()));
        Member member = memberRepository.save(createMember(1L));

        ReservationRequest request = new ReservationRequest(now(), 3L, 1L);

        assertThatCode(() -> reservationService.addReservation(request, member))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재 이후로 예약하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_later_date() {
        themeRepository.save(createTheme(1L));
        Member member = memberRepository.save(createMember(1L));
        reservationTimeRepository.save(createReservationTime(1L, "10:00"));

        ReservationRequest request = new ReservationRequest(now().plusDays(2), 1L, 1L);

        assertThatCode(() -> reservationService.addReservation(request, member))
                .doesNotThrowAnyException();
    }

    @DisplayName("날짜, 시간, 테마가 일치하는 예약을 추가하려 할 때 예외가 발생한다.")
    @Test
    void should_throw_exception_when_add_exist_reservation() {
        Theme theme1 = themeRepository.save(createTheme(1L));
        Theme theme2 = themeRepository.save(createTheme(2L));
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        Member member = memberRepository.save(createMember(1L));

        reservationRepository.save(createAcceptReservation(1L, now().plusDays(2), time, theme1, member));
        reservationRepository.save(createAcceptReservation(2L, now(), time, theme2, member));

        ReservationRequest request = new ReservationRequest(now().plusDays(2), 1L, 1L);
        assertThatThrownBy(() -> reservationService.addReservation(request, member))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 이미 해당 시간에 예약이 존재합니다.");
    }

    @DisplayName("사용자가 예약한 예약을 반환한다.")
    @Test
    void should_return_member_reservations() {
        Theme theme = themeRepository.save(createTheme(1L));
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        Member member = memberRepository.save(createMember(1L));
        reservationRepository.save(createAcceptReservation(1L, now(), time, theme, member));
        reservationRepository.save(createWaiting(2L, now(), time, theme, member));

        List<MemberReservation> reservations = reservationService.findMemberReservations(member);

        assertThat(reservations).hasSize(2);
    }

    @DisplayName("모든 예약 대기 목록을 반환한다.")
    @Test
    void should_find_all_waiting_reservations() {
        Theme theme1 = themeRepository.save(createTheme(1L));
        Theme theme2 = themeRepository.save(createTheme(2L));
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "10:00"));
        Member member = memberRepository.save(createMember(1L));
        reservationRepository.save(createWaiting(1L, now().plusDays(2), time, theme1, member));
        reservationRepository.save(createWaiting(2L, now(), time, theme2, member));
        reservationRepository.save(createAcceptReservation(3L, now().plusDays(2), time, theme2, member));

        List<Reservation> waitingReservations = reservationService.findWaitingReservations();

        assertSoftly(assertions -> {
            assertions.assertThat(waitingReservations).hasSize(2);
            assertions.assertThat(waitingReservations).extracting("status")
                    .containsExactly(WAITING, WAITING);
        });
    }
}
