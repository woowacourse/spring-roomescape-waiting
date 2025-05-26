package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.infrastructure.MemberRepositoryAdapter;
import roomescape.reservation.application.dto.AdminReservationRequest;
import roomescape.reservation.application.dto.AdminReservationSearchRequest;
import roomescape.reservation.application.dto.MyReservationResponse;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.application.dto.UserReservationRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.exception.ReservationAlreadyExistsException;
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
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.infrastructure.WaitingRepositoryAdapter;

@ActiveProfiles("test")
@DataJpaTest
@Import({
        ReservationService.class,
        ReservationRepositoryAdapter.class,
        MemberRepositoryAdapter.class,
        ReservationTimeRepositoryAdapter.class,
        ThemeRepositoryAdapter.class,
        WaitingRepositoryAdapter.class
})
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @DisplayName("회원 ID로 예약과 대기를 조회한다")
    @Test
    void findAllByMemberId() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);

        // 회원으로 예약 생성 및 저장
        Reservation reservation = new Reservation(member, spec);
        reservationRepository.save(reservation);

        // 동일한 회원으로 대기 생성 및 저장
        Waiting waiting = new Waiting(member, spec);
        Waiting savedWaiting = waitingRepository.save(waiting);

        // when
        List<MyReservationResponse> responses = reservationService.findAllByMemberId(memberId);

        // then
        assertThat(responses).hasSize(2);

        MyReservationResponse reservationResponse = responses.stream()
                .filter(r -> r.status().equals(MyReservationResponse.RESERVED))
                .findFirst()
                .orElseThrow();
        assertThat(reservationResponse.id()).isEqualTo(reservation.getId());
        assertThat(reservationResponse.theme()).isEqualTo(theme.getName());
        assertThat(reservationResponse.date()).isEqualTo(date);

        MyReservationResponse waitingResponse = responses.stream()
                .filter(r -> r.status().endsWith(MyReservationResponse.WAITING))
                .findFirst()
                .orElseThrow();
        assertThat(waitingResponse.id()).isEqualTo(savedWaiting.getId());
        assertThat(waitingResponse.theme()).isEqualTo(theme.getName());
        assertThat(waitingResponse.date()).isEqualTo(date);
        assertThat(waitingResponse.status()).isEqualTo("1" + MyReservationResponse.WAITING);
    }

    @DisplayName("필터링된 예약을 조회한다")
    @Test
    void findFiltered() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        // 회원으로 예약 생성 및 저장
        Reservation reservation = new Reservation(member, spec);
        reservationRepository.save(reservation);

        // 관리자용 예약 검색 요청 객체 생성 (회원 ID, 테마 ID, 시작 날짜, 종료 날짜)
        AdminReservationSearchRequest request = new AdminReservationSearchRequest(memberId, themeId, date, date);

        // when
        List<ReservationResponse> responses = reservationService.findFiltered(request);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(reservation.getId());
        assertThat(responses.getFirst().member().id()).isEqualTo(memberId);
        assertThat(responses.getFirst().theme().id()).isEqualTo(themeId);
        assertThat(responses.getFirst().date()).isEqualTo(date);
    }

    @DisplayName("사용자에 의한 예약 생성 - 회원이 존재하지 않는 경우 예외 발생")
    @Test
    void createByUser_memberNotFound() {
        // given
        // 존재하지 않는 회원 ID 설정
        Long memberId = 999L;
        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 존재하지 않는 시간 ID 설정
        Long timeId = 1L;
        // 존재하지 않는 테마 ID 설정
        Long themeId = 1L;

        // 사용자 예약 요청 객체 생성
        UserReservationRequest request = new UserReservationRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.createByUser(memberId, request))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @DisplayName("사용자에 의한 예약 생성 - 시간이 존재하지 않는 경우 예외 발생")
    @Test
    void createByUser_timeNotFound() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 존재하지 않는 시간 ID 설정
        Long timeId = 999L;
        // 존재하지 않는 테마 ID 설정
        Long themeId = 1L;

        // 사용자 예약 요청 객체 생성
        UserReservationRequest request = new UserReservationRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.createByUser(memberId, request))
                .isInstanceOf(TimeNotFoundException.class);
    }

    @DisplayName("사용자에 의한 예약 생성 - 과거 시간인 경우 예외 발생")
    @Test
    void createByUser_pastTime() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
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

        // 사용자 예약 요청 객체 생성
        UserReservationRequest request = new UserReservationRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.createByUser(memberId, request))
                .isInstanceOf(ReservationInPastException.class);
    }

    @DisplayName("사용자에 의한 예약 생성 - 테마가 존재하지 않는 경우 예외 발생")
    @Test
    void createByUser_themeNotFound() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);
        Long timeId = time.getId();

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 존재하지 않는 테마 ID 설정
        Long themeId = 999L;

        // 사용자 예약 요청 객체 생성
        UserReservationRequest request = new UserReservationRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.createByUser(memberId, request))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("사용자에 의한 예약 생성 - 이미 예약이 존재하는 경우 예외 발생")
    @Test
    void createByUser_alreadyExists() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
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

        // 동일한 날짜, 시간, 테마로 사용자 예약 요청 객체 생성
        UserReservationRequest request = new UserReservationRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.createByUser(memberId, request))
                .isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @DisplayName("사용자에 의한 예약 생성 - 성공")
    @Test
    void createByUser_success() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);
        Long timeId = time.getId();

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 사용자 예약 요청 객체 생성
        UserReservationRequest request = new UserReservationRequest(date, timeId, themeId);

        // when
        ReservationResponse response = reservationService.createByUser(memberId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.member().id()).isEqualTo(memberId);
        assertThat(response.theme().id()).isEqualTo(themeId);
        assertThat(response.time().id()).isEqualTo(timeId);
        assertThat(response.date()).isEqualTo(date);
    }

    @DisplayName("관리자에 의한 예약 생성 - 성공")
    @Test
    void createByAdmin_success() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);
        Long timeId = time.getId();

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 관리자용 예약 요청 객체 생성 (날짜, 시간 ID, 테마 ID, 회원 ID)
        AdminReservationRequest request = new AdminReservationRequest(date, timeId, themeId, memberId);

        // when
        ReservationResponse response = reservationService.createByAdmin(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.member().id()).isEqualTo(memberId);
        assertThat(response.theme().id()).isEqualTo(themeId);
        assertThat(response.time().id()).isEqualTo(timeId);
        assertThat(response.date()).isEqualTo(date);
    }

    @DisplayName("예약을 삭제한다")
    @Test
    void deleteById() {
        // given
        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        // 회원으로 예약 생성 및 저장
        Reservation reservation = new Reservation(member, spec);
        Reservation savedReservation = reservationRepository.save(reservation);
        Long reservationId = savedReservation.getId();

        // when
        reservationService.deleteById(reservationId);

        // then
        assertThat(reservationRepository.findById(reservationId)).isEmpty();
    }
}
