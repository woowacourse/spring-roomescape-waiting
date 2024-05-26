package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.config.DatabaseCleaner;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.WaitingCreateRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class WaitingServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private WaitingService waitingService;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
    }

    @Test
    @DisplayName("같은 테마, 날짜, 시간에 예약이 있을 때 예약 대기를 저장한다.")
    void saveTest() {
        final LocalDate date = LocalDate.now().plusDays(1);
        final LocalTime time = LocalTime.now();
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member member = memberRepository.save(new Member("마크", "mark@woowa.com", "asd"));
        final Member member2 = memberRepository.save(new Member("안돌", "andol@woowa.com", "asd"));
        reservationRepository.save(new Reservation(member2, date, theme, reservationTime));
        final LoginMemberInToken loginMember = new LoginMemberInToken(member.getId(), member.getRole(),
                member.getName(), member.getEmail());

        final Waiting waiting = waitingService.save(
                new WaitingCreateRequest(date, theme.getId(), reservationTime.getId()),
                loginMember
        );

        final Long expectedId = waitingRepository.findById(waiting.getId()).get().getId();
        assertThat(expectedId).isEqualTo(waiting.getId());
    }

    @Test
    @DisplayName("같은 테마, 날짜, 시간에 예약이 없을 때 예약 대기를 저장하면 예외가 발생한다.")
    void saveTest_Fail1() {
        final LocalDate date = LocalDate.now().plusDays(1);
        final LocalTime time = LocalTime.now();
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member member = memberRepository.save(new Member("마크", "mark@woowa.com", "asd"));
        final LoginMemberInToken loginMember = new LoginMemberInToken(member.getId(), member.getRole(), member.getName(), member.getEmail());

        assertThatThrownBy(() -> waitingService.save(new WaitingCreateRequest(date, theme.getId(), reservationTime.getId()), loginMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약이 없어 예약 대기를 할 수 없습니다.");
    }

    @Test
    @DisplayName("같은 테마, 날짜, 시간에 자신이 예약을 했을 때 예약 대기를 저장하면 예외가 발생한다.")
    void saveTest_Fail2() {
        final LocalDate date = LocalDate.now().plusDays(1);
        final LocalTime time = LocalTime.now();
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member member = memberRepository.save(new Member("마크", "mark@woowa.com", "asd"));
        reservationRepository.save(new Reservation(member, date, theme, reservationTime));
        final LoginMemberInToken loginMember = new LoginMemberInToken(member.getId(), member.getRole(), member.getName(), member.getEmail());

        assertThatThrownBy(() -> waitingService.save(new WaitingCreateRequest(date, theme.getId(), reservationTime.getId()), loginMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 예약 중 입니다.");
    }

    @Test
    @DisplayName("같은 테마, 날짜, 시간에 내가 예약 대기 중일 때 예약 대기를 저장하면 예외가 발생한다.")
    void saveTest_Fail3() {
        final LocalDate date = LocalDate.now().plusDays(1);
        final LocalTime time = LocalTime.now();
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member waitingMember = memberRepository.save(new Member("마크", "mark@woowa.com", "asd"));
        final Member reservationMember = memberRepository.save(new Member("안돌", "andol@woowa.com", "asd"));
        reservationRepository.save(new Reservation(reservationMember, date, theme, reservationTime));
        final LoginMemberInToken loginMember = new LoginMemberInToken(waitingMember.getId(), waitingMember.getRole(), waitingMember.getName(), waitingMember.getEmail());
        waitingService.save(new WaitingCreateRequest(date, theme.getId(), reservationTime.getId()), loginMember);

        assertThatThrownBy(() -> waitingService.save(new WaitingCreateRequest(date, theme.getId(), reservationTime.getId()), loginMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 예약 대기 중 입니다.");
    }

    @Test
    @DisplayName("지나간 날짜에 대한 예약 대기를 저장하면 예외가 발생한다.")
    void saveTest_Fail4() {
        final LocalDate date = LocalDate.now().minusDays(1);
        final LocalTime time = LocalTime.now();
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member waitingMember = memberRepository.save(new Member("마크", "mark@woowa.com", "asd"));
        final Member reservationMember = memberRepository.save(new Member("안돌", "andol@woowa.com", "asd"));
        reservationRepository.save(new Reservation(reservationMember, date, theme, reservationTime));
        final LoginMemberInToken loginMember = new LoginMemberInToken(waitingMember.getId(), waitingMember.getRole(), waitingMember.getName(), waitingMember.getEmail());

        assertThatThrownBy(() -> waitingService.save(new WaitingCreateRequest(date, theme.getId(), reservationTime.getId()), loginMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지나간 날짜에 대한 예약 대기는 할 수 없습니다.");
    }

    @Test
    @DisplayName("같은 날짜, 지나간 시간에 대한 예약 대기를 저장하면 예외가 발생한다.")
    void saveTest_Fail5() {
        final LocalDate date = LocalDate.now();
        final LocalTime time = LocalTime.now().minusHours(1);
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member member = memberRepository.save(new Member("마크", "mark@woowa.com", "asd"));
        final Member member2 = memberRepository.save(new Member("안돌", "andol@woowa.com", "asd"));
        reservationRepository.save(new Reservation(member2, date, theme, reservationTime));
        final LoginMemberInToken loginMember = new LoginMemberInToken(member.getId(), member.getRole(), member.getName(), member.getEmail());

        assertThatThrownBy(() -> waitingService.save(new WaitingCreateRequest(date, theme.getId(), reservationTime.getId()), loginMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지나간 시간에 대한 예약 대기는 할 수 없습니다.");
    }

    @Test
    @DisplayName("관리자가 모든 예약 대기를 조회한다.")
    void findAllTest() {
        final LocalTime time = LocalTime.now();
        final LocalDate date = LocalDate.now();
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member admin = memberRepository.save(new Member(null, Role.ADMIN,"마크", "mark@woowa.com", "asd"));
        final Member member2 = memberRepository.save(new Member("안돌", "andol@woowa.com", "asd"));
        final Waiting waiting1 = waitingRepository.save(new Waiting(admin, date, theme, reservationTime));
        final Waiting waiting2 = waitingRepository.save(new Waiting(member2, date, theme, reservationTime));
        final LoginMemberInToken loginMember = new LoginMemberInToken(1L, admin.getRole(), admin.getName(), admin.getEmail());

        final List<WaitingResponse> actual = waitingService.findAll(loginMember);

        assertThat(actual).hasSize(2);
        assertThat(waiting1.getId()).isEqualTo(actual.get(0).id());
        assertThat(waiting2.getId()).isEqualTo(actual.get(1).id());
    }

    @Test
    @DisplayName("관리자가 아닌 회원이 모든 예약 대기를 조회하면 예외가 발생한다.")
    void findAllTest_Fail() {
        final LocalTime time = LocalTime.now();
        final LocalDate date = LocalDate.now();
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member member = memberRepository.save(new Member(null, Role.MEMBER, "마크", "mark@woowa.com", "asd"));
        memberRepository.save(new Member("안돌", "andol@woowa.com", "asd"));
        waitingRepository.save(new Waiting(member, date, theme, reservationTime));

        assertThatThrownBy(() -> waitingService.findAll(new LoginMemberInToken(member.getId(), member.getRole(), member.getName(), member.getEmail())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("관리자만 조회할 수 있습니다.");
    }

    @Test
    @DisplayName("멤버id를 통해 그의 예약 대기와 순번을 반환한다.")
    void findWaitingWithRanksByMemberIdTest() {
        final LocalTime time = LocalTime.now();
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member member1 = memberRepository.save(new Member("마크", "mark@woowa.com", "asd"));
        final Member member2 = memberRepository.save(new Member("안돌", "andol@woowa.com", "asd"));
        final LocalDate date = LocalDate.now();
        reservationRepository.save(new Reservation(member1, date, theme, reservationTime));
        waitingRepository.save(new Waiting(member1, date, theme, reservationTime));
        waitingRepository.save(new Waiting(member1, date.plusDays(1), theme, reservationTime));

        final Waiting waiting1 = waitingRepository.save(new Waiting(member2, date, theme, reservationTime));
        final Waiting waiting2 = waitingRepository.save(new Waiting(member2, date.plusDays(1), theme, reservationTime));

        final List<MyReservationResponse> actual = waitingService.findWaitingWithRanksByMemberId(member2.getId());

        assertThat(actual).hasSize(2);
        assertThat(waiting1.getId()).isEqualTo(actual.get(0).getId());
        assertThat(waiting2.getId()).isEqualTo(actual.get(1).getId());
    }

    @Test
    @DisplayName("사용자 본인의 예약 대기를 삭제한다.")
    void deleteTest() {
        final LocalTime time = LocalTime.now();
        final LocalDate date = LocalDate.now();
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member member = memberRepository.save(new Member("마크", "mark@woowa.com", "asd"));
        final Waiting waiting = waitingRepository.save(new Waiting(member, date, theme, reservationTime));
        final LoginMemberInToken loginMember = new LoginMemberInToken(member.getId(), member.getRole(),
                member.getName(), member.getEmail());

        waitingService.delete(waiting.getId(), loginMember);

        assertThat(waitingRepository.findById(waiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("어드민은 본인이 아닌 예약 대기를 삭제할 수 있다.")
    void deleteTest_Admin() {
        final LocalTime time = LocalTime.now();
        final LocalDate date = LocalDate.now();
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member admin = memberRepository.save(new Member(null, Role.ADMIN, "마크", "mark@woowa.com", "asd"));
        memberRepository.save(new Member("안돌", "andol@woowa.com", "asd"));
        final Waiting waiting = waitingRepository.save(new Waiting(admin, date, theme, reservationTime));
        final LoginMemberInToken loginMember = new LoginMemberInToken(admin.getId(), admin.getRole(), admin.getName(), admin.getEmail());

        waitingService.delete(waiting.getId(), loginMember);

        assertThat(waitingRepository.findById(waiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("사용자는 본인의 예약 대기가 아닌 예약 대기를 삭제하면 예외가 발생한다.")
    void deleteTest_Fail() {
        final LocalTime time = LocalTime.now();
        final LocalDate date = LocalDate.now();
        final ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(time));
        final Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        final Member member = memberRepository.save(new Member("마크", "mark@woowa.com", "asd"));
        final Waiting waiting = waitingRepository.save(new Waiting(member, date, theme, reservationTime));
        final LoginMemberInToken loginMember = new LoginMemberInToken(2L, member.getRole(),
                member.getName(), member.getEmail());

        assertThatThrownBy(() -> waitingService.delete(waiting.getId(), loginMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인의 예약 대기만 취소할 수 있습니다.");
    }
}
