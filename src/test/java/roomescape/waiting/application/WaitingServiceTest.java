package roomescape.waiting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.auth.exception.AccessForbiddenException;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.infrastructure.MemberRepositoryAdapter;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.exception.ReservationInPastException;
import roomescape.reservation.infrastructure.ReservationRepositoryAdapter;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.reservationTime.exception.TimeNotFoundException;
import roomescape.reservationTime.infrastructure.ReservationTimeRepositoryAdapter;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.infrastructure.ThemeRepositoryAdapter;
import roomescape.waiting.application.dto.WaitingRequest;
import roomescape.waiting.application.dto.WaitingResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.exception.DuplicatedWaitingException;
import roomescape.waiting.exception.SlotNotReservedException;
import roomescape.waiting.exception.WaitingNotFoundException;
import roomescape.waiting.infrastructure.WaitingRepositoryAdapter;

@ActiveProfiles("test")
@DataJpaTest
@Import({
        WaitingService.class,
        WaitingRepositoryAdapter.class,
        MemberRepositoryAdapter.class,
        ReservationRepositoryAdapter.class,
        ReservationTimeRepositoryAdapter.class,
        ThemeRepositoryAdapter.class
})
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("대기 생성 - 회원이 존재하지 않는 경우 예외 발생")
    @Test
    void create_memberNotFound() {
        // given
        Long memberId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        Long themeId = 1L;

        WaitingRequest request = new WaitingRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> waitingService.create(memberId, request))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @DisplayName("대기 생성 - 시간이 존재하지 않는 경우 예외 발생")
    @Test
    void create_timeNotFound() {
        // given
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(member);
        Long memberId = member.getId();

        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        Long themeId = 1L;

        WaitingRequest request = new WaitingRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> waitingService.create(memberId, request))
                .isInstanceOf(TimeNotFoundException.class);
    }

    @DisplayName("대기 생성 - 과거 시간인 경우 예외 발생")
    @Test
    void create_pastTime() {
        // given
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(member);
        Long memberId = member.getId();

        ReservationTime pastTime = new ReservationTime(LocalTime.now().minusMinutes(5));
        timeRepository.save(pastTime);
        Long timeId = pastTime.getId();

        LocalDate date = LocalDate.now();
        Long themeId = 1L;

        WaitingRequest request = new WaitingRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> waitingService.create(memberId, request))
                .isInstanceOf(ReservationInPastException.class);
    }

    @DisplayName("대기 생성 - 테마가 존재하지 않는 경우 예외 발생")
    @Test
    void create_themeNotFound() {
        // given
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(member);
        Long memberId = member.getId();

        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);
        Long timeId = time.getId();

        LocalDate date = LocalDate.now().plusDays(1);
        Long themeId = 1L;

        WaitingRequest request = new WaitingRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> waitingService.create(memberId, request))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("대기 생성 - 예약이 존재하지 않는 경우 예외 발생")
    @Test
    void create_slotNotReserved() {
        // given
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(member);
        Long memberId = member.getId();

        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);
        Long timeId = time.getId();

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        LocalDate date = LocalDate.now().plusDays(1);

        WaitingRequest request = new WaitingRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> waitingService.create(memberId, request))
                .isInstanceOf(SlotNotReservedException.class);
    }

    @DisplayName("대기 생성 - 이미 예약한 사용자가 같은 슬롯에 대기 생성 시 예외 발생")
    @Test
    void create_duplicatedReservationMember() {
        // given
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(member);
        Long memberId = member.getId();

        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);
        Long timeId = time.getId();

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        Reservation reservation = new Reservation(member, spec);
        reservationRepository.save(reservation);

        WaitingRequest request = new WaitingRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> waitingService.create(memberId, request))
                .isInstanceOf(DuplicatedWaitingException.class);
    }

    @DisplayName("대기 생성 - 이미 대기한 사용자가 같은 슬롯에 대기 생성 시 예외 발생")
    @Test
    void create_duplicatedWaitingMember() {
        // given
        Member reservationMember = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(reservationMember);

        Member waitingMember = MemberFixture.createMember("김진우", "test2@test.com", "5678");
        memberRepository.save(waitingMember);
        Long waitingMemberId = waitingMember.getId();

        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);
        Long timeId = time.getId();

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        Reservation reservation = new Reservation(reservationMember, spec);
        reservationRepository.save(reservation);

        WaitingRequest firstRequest = new WaitingRequest(date, timeId, themeId);
        waitingService.create(waitingMemberId, firstRequest);

        WaitingRequest secondRequest = new WaitingRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> waitingService.create(waitingMemberId, secondRequest))
                .isInstanceOf(DuplicatedWaitingException.class);
    }

    @DisplayName("대기 생성 - 성공")
    @Test
    void create_success() {
        // given
        Member reservationMember = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(reservationMember);

        Member waitingMember = MemberFixture.createMember("김진우", "test2@test.com", "5678");
        memberRepository.save(waitingMember);
        Long waitingMemberId = waitingMember.getId();

        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);
        Long timeId = time.getId();

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        Reservation reservation = new Reservation(reservationMember, spec);
        reservationRepository.save(reservation);

        WaitingRequest request = new WaitingRequest(date, timeId, themeId);

        // when
        WaitingResponse response = waitingService.create(waitingMemberId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
    }

    @DisplayName("사용자에 의한 대기 삭제 - 대기가 존재하지 않는 경우 예외 발생")
    @Test
    void deleteByUser_waitingNotFound() {
        // given
        Long waitingId = 1L;
        Long memberId = 1L;

        // when & then
        assertThatThrownBy(() -> waitingService.deleteByUser(waitingId, memberId))
                .isInstanceOf(WaitingNotFoundException.class);
    }

    @DisplayName("사용자에 의한 대기 삭제 - 소유자가 아닌 경우 예외 발생")
    @Test
    void deleteByUser_notOwner() {
        // given
        Member owner = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(owner);

        Member otherMember = MemberFixture.createMember("김진우", "test2@test.com", "1234");
        memberRepository.save(otherMember);
        Long otherMemberId = otherMember.getId();

        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);

        Waiting waiting = new Waiting(owner, spec);
        waitingRepository.save(waiting);
        Long waitingId = waiting.getId();

        // when & then
        assertThatThrownBy(() -> waitingService.deleteByUser(waitingId, otherMemberId))
                .isInstanceOf(AccessForbiddenException.class);
    }
}
