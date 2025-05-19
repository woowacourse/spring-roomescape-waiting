package roomescape.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.spy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.PasswordEncoder;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.member.MemberEmailConflictException;
import roomescape.exception.custom.reason.member.MemberNotFoundException;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberReservationResponse;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.reservation.ReservationStatus;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;

@DataJpaTest
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private final MemberService memberService;
    private final MemberRepositoryFacade memberRepositoryFacade;
    private final PasswordEncoder passwordEncoder;

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    @Autowired
    public MemberServiceTest(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final MemberRepository memberRepository
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;

        this.memberRepositoryFacade = spy(new MemberRepositoryFacadeImpl(memberRepository));
        this.passwordEncoder = new PasswordEncoder();
        this.memberService = new MemberService(memberRepositoryFacade, passwordEncoder);
    }

    @Sql(scripts = "classpath:/initialize_auto_increment.sql")
    @BeforeEach
    void setUp() {
        // Sql 파일을 통해 auto_increment 초기화
    }

    @DisplayName("member를 생성하여 저장한다.")
    @Test
    void createMember() {
        // given
        final MemberRequest memberRequest = new MemberRequest("admin@email.com", "password", "부기");
        final Member expected = new Member(memberRequest.email(), memberRequest.password(), memberRequest.name(),
                MemberRole.MEMBER);

        // when
        memberService.createMember(memberRequest);

        // then
        final ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        then(memberRepositoryFacade).should().save(captor.capture());

        assertSoftly(s -> {
            final Member actual = captor.getValue();
            s.assertThat(actual.getEmail()).isEqualTo(expected.getEmail());
            s.assertThat(passwordEncoder.matches(expected.getPassword(), actual.getPassword())).isTrue();
            s.assertThat(actual.getName()).isEqualTo(expected.getName());
            s.assertThat(actual.getRole()).isEqualTo(expected.getRole());
        });
    }

    @DisplayName("이미 존재하는 이메일로 생성하면, 예외가 발생한다.")
    @Test
    void createMember1() {
        // given
        final MemberRequest memberRequest = new MemberRequest("admin@email.com", "password", "부기");
        final Member member = new Member("admin@email.com", "password", "부기", MemberRole.MEMBER);
        memberRepositoryFacade.save(member);

        // when & then
        assertThatThrownBy(() -> {
            memberService.createMember(memberRequest);
        }).isInstanceOf(MemberEmailConflictException.class);
    }

    @DisplayName("존재하는 모든 member를 반환한다.")
    @Test
    void readAll() {
        // given
        final Member member = new Member("email", "pass", "name", MemberRole.MEMBER);
        memberRepositoryFacade.save(member);

        // when
        final List<MemberResponse> actual = memberService.readAllMember();

        // then
        assertThat(actual).hasSize(1);
    }

    @DisplayName("member가 없다면 빈 컬렉션을 반환한다.")
    @Test
    void readAll1() {
        // given & when
        final List<MemberResponse> actual = memberService.readAllMember();

        // then
        assertThat(actual).isEmpty();
    }

    @DisplayName("member의 예약을 모두 조회한다.")
    @Test
    void readAllReservation() {
        // given
        final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);
        final MemberReservationResponse expected = new MemberReservationResponse(
                1L, "테마", LocalDate.of(2026, 12, 31),
                LocalTime.of(12, 40), "예약"
        );

        final Member member = new Member(loginMember.email(), "pass", "boogie", MemberRole.MEMBER);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
        final Theme theme = new Theme("테마", "설명", "썸네일");
        final Reservation reservation = new Reservation(LocalDate.of(2026, 12, 31), member,
                reservationTime, theme, ReservationStatus.PENDING);

        memberRepositoryFacade.save(member);
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        reservationRepository.save(reservation);

        // when
        final List<MemberReservationResponse> actual = memberService.readAllReservationsByMember(loginMember);

        // then
        assertThat(actual)
                .contains(expected)
                .hasSize(1);
    }

    @DisplayName("이메일의 member가 존재하지 않는다면, 예외가 발생한다.")
    @Test
    void readAllReservation1() {
        // given
        final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

        // when & then
        assertThatThrownBy(() -> {
            memberService.readAllReservationsByMember(loginMember);
        }).isInstanceOf(MemberNotFoundException.class);
    }
}
