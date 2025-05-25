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
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);

        Reservation reservation = new Reservation(member, spec);
        reservationRepository.save(reservation);

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
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        Reservation reservation = new Reservation(member, spec);
        reservationRepository.save(reservation);

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
        Long memberId = 999L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 1L;
        Long themeId = 1L;

        UserReservationRequest request = new UserReservationRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.createByUser(memberId, request))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @DisplayName("사용자에 의한 예약 생성 - 시간이 존재하지 않는 경우 예외 발생")
    @Test
    void createByUser_timeNotFound() {
        // given
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 999L;
        Long themeId = 1L;

        UserReservationRequest request = new UserReservationRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.createByUser(memberId, request))
                .isInstanceOf(TimeNotFoundException.class);
    }

    @DisplayName("사용자에 의한 예약 생성 - 과거 시간인 경우 예외 발생")
    @Test
    void createByUser_pastTime() {
        // given
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        ReservationTime pastTime = new ReservationTime(LocalTime.now().minusMinutes(5));
        timeRepository.save(pastTime);
        Long timeId = pastTime.getId();

        LocalDate date = LocalDate.now();
        Long themeId = 1L;

        UserReservationRequest request = new UserReservationRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.createByUser(memberId, request))
                .isInstanceOf(ReservationInPastException.class);
    }

    @DisplayName("사용자에 의한 예약 생성 - 테마가 존재하지 않는 경우 예외 발생")
    @Test
    void createByUser_themeNotFound() {
        // given
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);
        Long timeId = time.getId();

        LocalDate date = LocalDate.now().plusDays(1);
        Long themeId = 999L;

        UserReservationRequest request = new UserReservationRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.createByUser(memberId, request))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("사용자에 의한 예약 생성 - 이미 예약이 존재하는 경우 예외 발생")
    @Test
    void createByUser_alreadyExists() {
        // given
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);
        Long timeId = time.getId();

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        Reservation reservation = new Reservation(member, spec);
        reservationRepository.save(reservation);

        UserReservationRequest request = new UserReservationRequest(date, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.createByUser(memberId, request))
                .isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @DisplayName("사용자에 의한 예약 생성 - 성공")
    @Test
    void createByUser_success() {
        // given
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);
        Long timeId = time.getId();

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        LocalDate date = LocalDate.now().plusDays(1);
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
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);
        Long memberId = member.getId();

        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);
        Long timeId = time.getId();

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        LocalDate date = LocalDate.now().plusDays(1);
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
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);

        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        timeRepository.save(time);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        LocalDate date = LocalDate.now().plusDays(1);
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        Reservation reservation = new Reservation(member, spec);
        Reservation savedReservation = reservationRepository.save(reservation);
        Long reservationId = savedReservation.getId();

        // when
        reservationService.deleteById(reservationId);

        // then
        assertThat(reservationRepository.findById(reservationId)).isEmpty();
    }
}
