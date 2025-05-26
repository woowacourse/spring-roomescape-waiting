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
        // 존재하지 않는 회원 ID 설정
        Long memberId = 1L;
        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 존재하지 않는 시간 ID 설정
        Long timeId = 1L;
        // 존재하지 않는 테마 ID 설정
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
        // 테스트용 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(member);
        Long memberId = member.getId();

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 존재하지 않는 시간 ID 설정
        Long timeId = 1L;
        // 존재하지 않는 테마 ID 설정
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
        // 테스트용 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(member);
        Long memberId = member.getId();

        // 현재 시간보다 5분 전의 시간으로 예약 시간 생성 (과거 시간)
        ReservationTime pastTime = new ReservationTime(LocalTime.now().minusMinutes(5));
        timeRepository.save(pastTime);
        Long timeId = pastTime.getId();

        // 오늘 날짜로 예약 날짜 설정 (과거 시간 테스트를 위해)
        LocalDate date = LocalDate.now();
        // 존재하지 않는 테마 ID 설정
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
        // 테스트용 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(member);
        Long memberId = member.getId();

        // 현재 시간으로 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);
        Long timeId = time.getId();

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 존재하지 않는 테마 ID 설정
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
        // 테스트용 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(member);
        Long memberId = member.getId();

        // 현재 시간으로 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);
        Long timeId = time.getId();

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        // 내일 날짜로 예약 날짜 설정
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
        // 테스트용 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(member);
        Long memberId = member.getId();

        // 현재 시간으로 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);
        Long timeId = time.getId();

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        // 회원으로 예약 생성 및 저장 (이미 예약한 상태)
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
        // 예약한 회원 생성 및 저장
        Member reservationMember = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(reservationMember);

        // 대기할 회원 생성 및 저장
        Member waitingMember = MemberFixture.createMember("김진우", "test2@test.com", "5678");
        memberRepository.save(waitingMember);
        Long waitingMemberId = waitingMember.getId();

        // 현재 시간으로 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);
        Long timeId = time.getId();

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        // 예약 회원으로 예약 생성 및 저장
        Reservation reservation = new Reservation(reservationMember, spec);
        reservationRepository.save(reservation);

        // 첫 번째 대기 요청 생성
        WaitingRequest firstRequest = new WaitingRequest(date, timeId, themeId);
        // 대기 회원으로 첫 번째 대기 생성 (이미 대기한 상태)
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
        // 예약한 회원 생성 및 저장
        Member reservationMember = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(reservationMember);

        // 대기할 회원 생성 및 저장
        Member waitingMember = MemberFixture.createMember("김진우", "test2@test.com", "5678");
        memberRepository.save(waitingMember);
        Long waitingMemberId = waitingMember.getId();

        // 현재 시간으로 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);
        Long timeId = time.getId();

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        // 예약 회원으로 예약 생성 및 저장 (대기 생성을 위한 예약 슬롯 준비)
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
        // 존재하지 않는 대기 ID 설정
        Long waitingId = 1L;
        // 존재하지 않는 회원 ID 설정
        Long memberId = 1L;

        // when & then
        assertThatThrownBy(() -> waitingService.deleteByUser(waitingId, memberId))
                .isInstanceOf(WaitingNotFoundException.class);
    }

    @DisplayName("사용자에 의한 대기 삭제 - 소유자가 아닌 경우 예외 발생")
    @Test
    void deleteByUser_notOwner() {
        // given
        // 대기 소유자 회원 생성 및 저장
        Member owner = MemberFixture.createMember("에드", "test@test.com", "1234");
        memberRepository.save(owner);

        // 다른 회원 생성 및 저장 (대기 소유자가 아님)
        Member otherMember = MemberFixture.createMember("김진우", "test2@test.com", "1234");
        memberRepository.save(otherMember);
        Long otherMemberId = otherMember.getId();

        // 현재 시간으로 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.now());
        timeRepository.save(time);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);

        // 소유자로 대기 생성 및 저장
        Waiting waiting = new Waiting(owner, spec);
        waitingRepository.save(waiting);
        Long waitingId = waiting.getId();

        // when & then
        assertThatThrownBy(() -> waitingService.deleteByUser(waitingId, otherMemberId))
                .isInstanceOf(AccessForbiddenException.class);
    }
}
