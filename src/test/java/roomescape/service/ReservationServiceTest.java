package roomescape.service;

import static java.time.LocalDate.now;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import static roomescape.model.ReservationStatus.ACCEPT;
import static roomescape.model.ReservationStatus.PENDING;
import static roomescape.model.Role.ADMIN;
import static roomescape.model.Role.MEMBER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
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
    private final ThemeRepository themeRepository;
    @Autowired
    private final ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private final ReservationRepository reservationRepository;
    @Autowired
    private final MemberRepository memberRepository;
    @Autowired
    private final ReservationService reservationService;

    @Autowired
    public ReservationServiceTest(ThemeRepository themeRepository, ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository, MemberRepository memberRepository,
                                  ReservationService reservationService) {
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.reservationService = reservationService;
    }

    @BeforeEach
    void setUp() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        themeRepository.save(new Theme(2L, "name2", "description2", "thumbnail2"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        reservationTimeRepository.save(new ReservationTime(2L, LocalTime.of(12, 0)));
    }

    @DisplayName("모든 예약 시간을 반환한다")
    @Test
    void should_return_all_reservation_times() {
        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, now(), reservationTime, theme1, member));
        reservationRepository.save(new Reservation(2L, now(), reservationTime, theme2, member));

        List<Reservation> reservations = reservationService.findAllReservations();

        assertThat(reservations).hasSize(2);
    }

    @DisplayName("검색 조건에 맞는 예약을 반환한다.")
    @Test
    void should_return_filtered_reservation() {
        Theme theme1 = themeRepository.findById(1L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, now(), reservationTime, theme1, member));
        reservationRepository.save(new Reservation(2L, now(), reservationTime, theme1, member));

        List<Reservation> reservations = reservationService
                .filterReservation(1L, 1L, now().minusDays(1), now().plusDays(3));

        assertThat(reservations).hasSize(2);
    }

    @DisplayName("사용자가 예약 시간을 추가한다")
    @Test
    void should_add_reservation_times_when_give_member_request() {
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

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
    void should_add_pending_reservation_times_when_give_member_request() {
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        LocalDate reservationDate = now().plusDays(2);
        ReservationRequest request = new ReservationRequest(reservationDate, 1L, 1L);

        reservationService.addPendingReservation(request, member);

        List<Reservation> allReservations = reservationRepository.findAll();
        assertSoftly(assertions -> {
            assertions.assertThat(allReservations).hasSize(1);
            assertions.assertThat(allReservations).extracting("status").containsExactly(PENDING);
        });
    }

    @DisplayName("이미 예약대기를 걸어 놓은 사용자가 또 다시 예약을 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_user_add_duplicated_pending_reservation() {
        Member member = new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222");
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(11, 0));
        Theme theme = new Theme(1L, "name", "description", "thumbnail");
        LocalDate reservationDate = now().plusDays(2);
        Reservation reservation = new Reservation(reservationDate, PENDING, reservationTime, theme, member);
        memberRepository.save(member);
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        reservationRepository.save(reservation);

        ReservationRequest request = new ReservationRequest(reservationDate, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addPendingReservation(request, member))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 이미 예약을 했거나 예약 대기를 걸어놓았습니다.");
    }

    @DisplayName("이미 예약을 걸어 놓은 사용자가 또 다시 예약을 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_user_add_duplicated_reservation() {
        Member member = new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222");
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(11, 0));
        Theme theme = new Theme(1L, "name", "description", "thumbnail");
        LocalDate reservationDate = now().plusDays(2);
        Reservation reservation = new Reservation(reservationDate, ACCEPT, reservationTime, theme, member);
        memberRepository.save(member);
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        reservationRepository.save(reservation);

        ReservationRequest request = new ReservationRequest(reservationDate, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addPendingReservation(request, member))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 이미 예약을 했거나 예약 대기를 걸어놓았습니다.");
    }


    @DisplayName("관리자가 예약 시간을 추가한다")
    @Test
    void should_add_reservation_times_when_give_admin_request() {
        memberRepository.save(new Member(1L, "썬", ADMIN, "sun@email.com", "1111"));
        AdminReservationRequest request =
                new AdminReservationRequest(now().plusDays(2), 1L, 1L, 1L);
        reservationService.addReservation(request);

        List<Reservation> allReservations = reservationRepository.findAll();
        assertThat(allReservations).hasSize(1);
    }

    @DisplayName("관리자가 예약 시간을 추가할 때 사용자가 없으면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_member() {
        AdminReservationRequest request =
                new AdminReservationRequest(now().plusDays(2), 1L, 1L, 1L);

        assertThatThrownBy(() -> reservationService.addReservation(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 아이디가 1인 사용자가 존재하지 않습니다.");
    }

    @DisplayName("예약 시간을 삭제한다")
    @Test
    void should_remove_reservation_times() {
        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, now(), reservationTime, theme1, member));
        reservationRepository.save(new Reservation(2L, now(), reservationTime, theme2, member));

        reservationService.deleteReservation(1L);

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("존재하지 않는 예약을 삭제하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_reservation_time() {
        assertThatThrownBy(() -> reservationService.deleteReservation(1000000))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 해당 id:[1000000] 값으로 예약된 내역이 존재하지 않습니다.");
    }

    @DisplayName("승인된 예약을 삭제하면 제일 먼저 예약된 대기가 승인으로 바뀐다.")
    @Test
    void should_confirm_first_waiting_reservation_when_delete_waiting_reservation() {
        Theme theme = themeRepository.findById(1L).get();
        ReservationTime time = reservationTimeRepository.findById(1L).get();

        Member member1 = new Member(1L, "피케이", MEMBER, "pk@email.com", "1111");
        Member member2 = new Member(2L, "배키", MEMBER, "dmsgml@email.com", "2222");
        Member member3 = new Member(3L, "포비", MEMBER, "pobi@email.com", "3333");
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        Reservation acceptedReservation =
                new Reservation(1L, now(), ACCEPT, LocalDateTime.now().minusMinutes(1), time, theme, member1);
        Reservation waiting1 =
                new Reservation(2L, now(), PENDING, LocalDateTime.now(), time, theme, member2);
        Reservation waiting2 =
                new Reservation(3L, now(), PENDING, LocalDateTime.now().plusMinutes(1), time, theme, member3);
        reservationRepository.save(acceptedReservation);
        reservationRepository.save(waiting1);
        reservationRepository.save(waiting2);

        reservationService.deleteReservation(1L);

        List<Reservation> reservations = reservationRepository.findAll();
        SoftAssertions.assertSoftly(assertions -> {
            assertions.assertThat(reservations).hasSize(2);
            assertions.assertThat(reservations).extracting("status").containsExactly(ACCEPT, PENDING);
            assertions.assertThat(reservations).extracting("id").containsExactly(2L, 3L);
        });
    }

    @DisplayName("승인된 예약을 삭제하고 이후 대기가 없으면 단순히 삭제만 한다.")
    @Test
    void should_only_remove_reservation_when_no_waiting_reservations() {
        Theme theme = themeRepository.findById(1L).get();
        ReservationTime time = reservationTimeRepository.findById(1L).get();
        Member member = new Member(1L, "피케이", MEMBER, "pk@email.com", "1111");
        memberRepository.save(member);
        Reservation reservation = new Reservation(1L, now(), ACCEPT, LocalDateTime.now(), time, theme, member);
        reservationRepository.save(reservation);

        reservationService.deleteReservation(1L);

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).isEmpty();
    }


    @DisplayName("예약 대기를 삭제할 때는 단순히 예약 대기만 삭제한다.")
    @Test
    void should_remove_reservation_with_no_change_when_delete_waiting_reservation() {
        Theme theme = themeRepository.findById(1L).get();
        ReservationTime time = reservationTimeRepository.findById(1L).get();

        Member member1 = new Member(1L, "피케이", MEMBER, "pk@email.com", "1111");
        Member member2 = new Member(2L, "배키", MEMBER, "dmsgml@email.com", "2222");
        Member member3 = new Member(3L, "포비", MEMBER, "pobi@email.com", "3333");
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        Reservation acceptedReservation =
                new Reservation(1L, now(), ACCEPT, LocalDateTime.now().minusMinutes(1), time, theme, member1);
        Reservation waiting1 =
                new Reservation(2L, now(), PENDING, LocalDateTime.now(), time, theme, member2);
        Reservation waiting2 =
                new Reservation(3L, now(), PENDING, LocalDateTime.now().plusMinutes(1), time, theme, member3);
        reservationRepository.save(acceptedReservation);
        reservationRepository.save(waiting1);
        reservationRepository.save(waiting2);

        reservationService.deleteReservation(2L);

        List<Reservation> reservations = reservationRepository.findAll();
        SoftAssertions.assertSoftly(assertions -> {
            assertions.assertThat(reservations).hasSize(2);
            assertions.assertThat(reservations).extracting("status").containsExactly(ACCEPT, PENDING);
            assertions.assertThat(reservations).extracting("id").containsExactly(1L, 3L);
        });
    }


    @DisplayName("존재하는 예약을 삭제하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_reservation_time() {
        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, now(), reservationTime, theme1, member));
        reservationRepository.save(new Reservation(2L, now(), reservationTime, theme2, member));

        assertThatCode(() -> reservationService.deleteReservation(1))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재 이전으로 예약하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_previous_date() {
        ReservationRequest request =
                new ReservationRequest(LocalDate.now().minusDays(1), 1L, 1L);
        Member member = new Member(1L, "썬", MEMBER, "sun@email.com", "1111");

        assertThatThrownBy(() -> reservationService.addReservation(request, member))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("[ERROR] 현재(", ") 이전 시간으로 예약할 수 없습니다.");
    }

    @DisplayName("현재로 예약하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_current_date() {
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();
        reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        themeRepository.save(new Theme("name", "description", "thumbnail"));

        ReservationRequest request = new ReservationRequest(LocalDate.now(), 3L, 1L);

        assertThatCode(() -> reservationService.addReservation(request, member))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재 이후로 예약하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_later_date() {
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();
        reservationTimeRepository.save(new ReservationTime(LocalTime.now()));

        ReservationRequest request =
                new ReservationRequest(LocalDate.now().plusDays(2), 1L, 1L);

        assertThatCode(() -> reservationService.addReservation(request, member))
                .doesNotThrowAnyException();
    }

    @DisplayName("날짜, 시간, 테마가 일치하는 예약을 추가하려 할 때 예외가 발생한다.")
    @Test
    void should_throw_exception_when_add_exist_reservation() {
        LocalDate date = now().plusDays(2);

        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, date, reservationTime, theme1, member));
        reservationRepository.save(new Reservation(2L, now(), reservationTime, theme2, member));

        ReservationRequest request = new ReservationRequest(date, 1L, 1L);
        assertThatThrownBy(() -> reservationService.addReservation(request, member))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 이미 해당 시간에 예약이 존재합니다.");
    }

    @DisplayName("사용자가 예약한 예약을 반환한다.")
    @Test
    void should_return_member_reservations() {
        Theme theme1 = themeRepository.findById(1L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, now(), reservationTime, theme1, member));
        reservationRepository.save(new Reservation(2L, now(), reservationTime, theme1, member));

        List<MemberReservation> reservations = reservationService
                .findMemberReservations(member);

        assertThat(reservations).hasSize(2);
    }

    @DisplayName("모든 예약 대기 목록을 반환한다.")
    @Test
    void should_find_all_waiting_reservations() {
        LocalDate date = now().plusDays(2);

        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        reservationRepository.save(new Reservation(1L, date, PENDING, reservationTime, theme1, member));
        reservationRepository.save(new Reservation(2L, now(), PENDING, reservationTime, theme2, member));
        reservationRepository.save(new Reservation(3L, date, ACCEPT, reservationTime, theme2, member));

        List<Reservation> waitingReservations = reservationService.findWaitingReservations();

        assertSoftly(assertions -> {
            assertions.assertThat(waitingReservations).hasSize(2);
            assertions.assertThat(waitingReservations).extracting("status")
                    .containsExactly(PENDING, PENDING);
        });
    }
}
