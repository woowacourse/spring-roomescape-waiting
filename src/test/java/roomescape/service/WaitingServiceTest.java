package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
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
    public WaitingServiceTest(ThemeRepository themeRepository, ReservationTimeRepository reservationTimeRepository,
                              WaitingRepository waitingRepository, MemberRepository memberRepository,
                              WaitingService waitingService) {
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.waitingService = waitingService;
    }

    @BeforeEach
    void setUp() {
        themeRepository.save(new Theme(1L, "name1", "description1", "thumbnail1"));
        themeRepository.save(new Theme(2L, "name2", "description2", "thumbnail2"));
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        reservationTimeRepository.save(new ReservationTime(2L, LocalTime.of(12, 0)));
    }

    @DisplayName("사용자가 예약 대기를 추가한다")
    @Test
    void should_add_reservation_times_when_give_member_request() {
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        WaitingRequest request = new WaitingRequest(now().plusDays(2), 1L, 1L);

        waitingService.addWaiting(request, member);

        Iterable<Waiting> allReservations = waitingRepository.findAll();
        assertThat(allReservations).hasSize(1);
    }

    @DisplayName("예약 대기를 삭제한다")
    @Test
    void should_remove_reservation_times() {
        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        waitingRepository.save(new Waiting(1L, now(), reservationTime, theme1, member));
        waitingRepository.save(new Waiting(2L, now(), reservationTime, theme2, member));

        waitingService.deleteWaiting(1L);

        Iterable<Waiting> waiting = waitingRepository.findAll();
        assertThat(waiting).hasSize(1);
    }

    @DisplayName("존재하지 않는 예약 대기를 삭제하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_reservation_time() {
        assertThatThrownBy(() -> waitingService.deleteWaiting(1000000))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 해당 id:[1000000] 값으로 예약된 예약 대기 내역이 존재하지 않습니다.");
    }

    @DisplayName("존재하는 예약 대기를 삭제하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_reservation_time() {
        Theme theme1 = themeRepository.findById(1L).get();
        Theme theme2 = themeRepository.findById(2L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        waitingRepository.save(new Waiting(1L, now(), reservationTime, theme1, member));
        waitingRepository.save(new Waiting(2L, now(), reservationTime, theme2, member));

        assertThatCode(() -> waitingService.deleteWaiting(1))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재 이전으로 예약 대기를 추가하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_previous_date() {
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
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();
        reservationTimeRepository.save(new ReservationTime(LocalTime.now()));

        WaitingRequest request = new WaitingRequest(LocalDate.now(), 3L, 1L);

        assertThatCode(() -> waitingService.addWaiting(request, member))
                .doesNotThrowAnyException();
    }

    @DisplayName("사용자가 예약한 예약 대기를 반환한다.")
    @Test
    void should_return_member_reservations() {
        Theme theme1 = themeRepository.findById(1L).get();
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        memberRepository.save(new Member(1L, "배키", MEMBER, "dmsgml@email.com", "2222"));
        Member member = memberRepository.findById(1L).orElseThrow();

        waitingRepository.save(new Waiting(1L, now(), reservationTime, theme1, member));
        waitingRepository.save(new Waiting(2L, now(), reservationTime, theme1, member));

        List<WaitingWithRank> waiting = waitingService
                .findMemberWaiting(member.getId());

        assertThat(waiting).hasSize(2);
    }
}
