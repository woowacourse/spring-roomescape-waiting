package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.request.ReservationRequest;
import roomescape.controller.request.WaitingRequest;
import roomescape.exception.BadRequestException;
import roomescape.exception.NotFoundException;
import roomescape.model.*;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.*;
import static roomescape.model.Role.MEMBER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/test_data.sql")
class WaitingServiceTest {

    @Autowired
    private final ThemeRepository themeRepository;
    @Autowired
    private final ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private final WaitingRepository waitingRepository;
    @Autowired
    private final MemberRepository memberRepository;
    @Autowired
    private final WaitingService waitingService;
    @Autowired
    private ReservationService reservationService;

    @Autowired
    public WaitingServiceTest(ThemeRepository themeRepository, ReservationTimeRepository reservationTimeRepository,
                              WaitingRepository waitingRepository, MemberRepository memberRepository,
                              WaitingService waitingService) {
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.waitingService = waitingService;
    }

    @DisplayName("사용자가 예약 대기를 추가한다")
    @Test
    void should_add_waiting_when_give_member_request() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        WaitingRequest request = new WaitingRequest(now().plusDays(2), 1L, 1L);

        waitingService.addWaiting(request, member);

        Iterable<Waiting> allReservations = waitingRepository.findAll();
        assertThat(allReservations).hasSize(1);
    }

    @DisplayName("예약 대기를 삭제한다")
    @Test
    void should_remove_waiting() {
        themeRepository.save(new Theme("name1", "description1", "thumbnail1"));
        themeRepository.save(new Theme("name2", "description2", "thumbnail2"));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member("배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        waitingRepository.save(new Waiting(now().plusDays(1), reservationTime, theme1, member));
        waitingRepository.save(new Waiting(now().plusDays(1), reservationTime, theme2, member));

        waitingService.findAllWaiting().forEach(waiting -> {
            System.out.println(waiting.getId());
        });

        waitingService.deleteWaiting(1L);

        Iterable<Waiting> waiting = waitingService.findAllWaiting();
        assertThat(waiting).hasSize(1);
    }

    @DisplayName("존재하지 않는 예약 대기를 삭제하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_waiting() {
        assertThatThrownBy(() -> waitingService.deleteWaiting(1000000))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 해당 id:[1000000] 값으로 예약된 예약 대기 내역이 존재하지 않습니다.");
    }

    @DisplayName("존재하는 예약 대기를 삭제하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_waiting() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        themeRepository.save(new Theme(2L, "name2", "description2", "thumbnail2"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        waitingRepository.save(new Waiting(now().plusDays(1), reservationTime, theme1, member));
        waitingRepository.save(new Waiting(now().plusDays(1), reservationTime, theme2, member));

        assertThatCode(() -> waitingService.deleteWaiting(1))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재 이전으로 예약 대기를 추가하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_previous_date() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));

        WaitingRequest request =
                new WaitingRequest(LocalDate.now().minusDays(1), 1L, 1L);
        Member member = new Member(1L, "썬", MEMBER, "sun@email.com", "1111");

        assertThatThrownBy(() -> waitingService.addWaiting(request, member))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("[ERROR] 현재(", ") 이전 시간으로 예약 대기를 추가할 수 없습니다.");
    }

    @DisplayName("현재 이후로 예약 대기를 추가하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_current_date() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        reservationTimeRepository.save(new ReservationTime(2L, LocalTime.of(12, 0)));
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();
        reservationTimeRepository.save(new ReservationTime(LocalTime.now()));

        WaitingRequest request = new WaitingRequest(LocalDate.now().plusDays(1), 3L, 1L);

        assertThatCode(() -> waitingService.addWaiting(request, member))
                .doesNotThrowAnyException();
    }

    @DisplayName("사용자가 예약한 예약 대기를 반환한다.")
    @Test
    void should_return_member_waiting() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        Theme theme = themeRepository.findById(1L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        waitingRepository.save(new Waiting(now().plusDays(1), reservationTime, theme, member));
        waitingRepository.save(new Waiting(now().plusDays(1), reservationTime, theme, member));

        List<WaitingWithRank> waiting = waitingService
                .findMemberWaiting(member.getId());

        assertThat(waiting).hasSize(2);
    }

    @DisplayName("이미 사용자가 예약한 날짜, 테마, 시간에 예약 대기를 추가하는 경우 예외를 발생한다.")
    @Test
    void should_throw_exception_when_existing_reservation() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        ReservationRequest reservationRequest = new ReservationRequest(now().plusDays(2), 1L, 1L);
        reservationService.addReservation(reservationRequest, member);

        WaitingRequest request = new WaitingRequest(now().plusDays(2), 1L, 1L);

        assertThatThrownBy(() -> waitingService.addWaiting(request, member))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 현재 이름(배키)으로 예약 내역이 이미 존재합니다.");
    }

    @DisplayName("이미 사용자가 예약 대기한 날짜, 테마, 시간에 예약 대기를 추가하는 경우 예외를 발생한다.")
    @Test
    void should_throw_exception_when_existing_waiting() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        WaitingRequest request = new WaitingRequest(now().plusDays(2), 1L, 1L);
        waitingService.addWaiting(request, member);

        assertThatThrownBy(() -> waitingService.addWaiting(request, member))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 현재 이름(배키)으로 예약된 예약 대기 내역이 이미 존재합니다.");
    }

    @DisplayName("모든 예약 대기를 반환한다")
    @Test
    void should_return_all_waiting() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        themeRepository.save(new Theme(2L, "name2", "description2", "thumbnail2"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        waitingRepository.save(new Waiting(1L, now().plusDays(1), reservationTime, theme1, member));
        waitingRepository.save(new Waiting(2L, now().plusDays(1), reservationTime, theme2, member));

        List<Waiting> reservations = waitingRepository.findAll();

        assertThat(reservations).hasSize(2);
    }

    @DisplayName("조건에 맞는 예약 대기를 반환한다.")
    @Test
    void should_return_waiting_when_condition_given() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        Theme theme = themeRepository.findById(1L).get();
        ReservationTime time = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        LocalDate date = LocalDate.now().plusDays(1);

        waitingRepository.save(new Waiting(1L, date, time, theme, member));
        waitingRepository.save(new Waiting(2L, date, time, theme, member));

        Waiting waiting = waitingService.findFirstWaitingByCondition(theme, date, time);

        assertThat(waiting).isNotNull();
    }

    @DisplayName("조건에 맞는 예약 대기가 없는 경우 예외를 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_condition_given() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        Theme theme = themeRepository.findById(1L).get();
        ReservationTime time = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        LocalDate date = LocalDate.now().plusDays(1);

        waitingRepository.save(new Waiting(1L, date, time, theme, member));

        assertThatThrownBy(() -> waitingService.findFirstWaitingByCondition(theme, date.plusDays(1), time))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 해당 테마:[name1], 날짜:[" + date.plusDays(1) + "], 시간:[10:00] 값으로 예약된 예약 대기 내역이 존재하지 않습니다.");
    }

    @DisplayName("조건에 맞는 예약 대기가 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_condition_given_exist() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        Theme theme = themeRepository.findById(1L).get();
        ReservationTime time = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        LocalDate date = LocalDate.now().plusDays(1);

        waitingRepository.save(new Waiting(1L, date, time, theme, member));
        waitingRepository.save(new Waiting(2L, date, time, theme, member));

        boolean exist = waitingService.existsWaiting(theme, date, time);

        assertThat(exist).isTrue();
    }
}
