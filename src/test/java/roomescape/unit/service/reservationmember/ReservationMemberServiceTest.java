package roomescape.unit.service.reservationmember;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import roomescape.domain.reservationmember.ReservationMember;
import roomescape.dto.member.SignupRequestDto;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.dto.reservationtime.AddReservationTimeDto;
import roomescape.dto.theme.AddThemeDto;
import roomescape.infrastructure.auth.jwt.JwtTokenProvider;
import roomescape.service.member.MemberService;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservationmember.ReservationMemberService;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.theme.ThemeService;
import roomescape.unit.config.ServiceFixture;
import roomescape.unit.repository.member.FakeMemberRepository;
import roomescape.unit.repository.reservation.FakeReservationRepository;
import roomescape.unit.repository.reservation.FakeReservationTimeRepository;
import roomescape.unit.repository.reservation.FakeThemeRepository;
import roomescape.unit.repository.reservationmember.FakeReservationMemberRepository;

class ReservationMemberServiceTest {

    private ReservationMemberService reservationMemberService;
    private ReservationTimeService reservationTimeService;
    private ThemeService themeService;
    private MemberService memberService;

    @BeforeEach
    void setup() {
        FakeReservationRepository fakeReservationRepository = ServiceFixture.fakeReservationRepository();
        FakeReservationTimeRepository fakeReservationTimeRepository = ServiceFixture.fakeReservationTimeRepository();
        FakeThemeRepository fakeThemeRepository = ServiceFixture.fakeThemeRepository();
        ReservationService reservationService = new ReservationService(fakeReservationRepository,
                fakeReservationTimeRepository, fakeThemeRepository);

        reservationTimeService = new ReservationTimeService(fakeReservationRepository, fakeReservationTimeRepository);
        themeService = new ThemeService(fakeThemeRepository, fakeReservationRepository);

        JwtTokenProvider jwtTokenProvider = ServiceFixture.jwtTokenProvider();
        FakeMemberRepository fakeMemberRepository = ServiceFixture.fakeMemberRepository();
        BCryptPasswordEncoder bCryptPasswordEncoder = ServiceFixture.passwordEncoder();
        memberService = new MemberService(bCryptPasswordEncoder, fakeMemberRepository, jwtTokenProvider);

        FakeReservationMemberRepository reservationMemberRepository = ServiceFixture.fakeReservationMemberRepository();
        reservationMemberService = new ReservationMemberService(memberService, reservationService,
                reservationMemberRepository);
    }

    @Test
    void 유저정보를_추가하여_예약을_진행한다() {
        long timeId = reservationTimeService.addReservationTime(
                new AddReservationTimeDto(LocalTime.now().plusHours(1L)));
        long themeId = themeService.addTheme(new AddThemeDto("tuda", "asdf", "asdf"));
        AddReservationDto addReservationDto = new AddReservationDto("asdf", LocalDate.now(), Long.valueOf(timeId),
                Long.valueOf(themeId));

        long memberId = memberService.signup(new SignupRequestDto("test@naver.com", "testtest", "test"));
        assertThatCode(
                () -> reservationMemberService.addReservation(addReservationDto, memberId)).doesNotThrowAnyException();
    }

    @Test
    void 존재하지않는_유저로_예약을_진행하면_예외가_발생한다() {
        long timeId = reservationTimeService.addReservationTime(
                new AddReservationTimeDto(LocalTime.now().plusHours(1L)));
        long themeId = themeService.addTheme(new AddThemeDto("tuda", "asdf", "asdf"));
        AddReservationDto addReservationDto = new AddReservationDto("asdf", LocalDate.now(), Long.valueOf(timeId),
                Long.valueOf(themeId));

        long memberId = -1;

        assertThatThrownBy(() -> reservationMemberService.addReservation(addReservationDto, memberId)).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    void 유저와_예약_정보를_합친_정보를_반환할_수_있다() {
        long timeId = reservationTimeService.addReservationTime(
                new AddReservationTimeDto(LocalTime.now().plusHours(1L)));
        long themeId = themeService.addTheme(new AddThemeDto("tuda", "asdf", "asdf"));

        AddReservationDto addReservationDto = new AddReservationDto("asdf", LocalDate.now(), Long.valueOf(timeId),
                Long.valueOf(themeId));

        long memberId = memberService.signup(new SignupRequestDto("test@naver.com", "testtest", "test"));
        long reservationId = reservationMemberService.addReservation(addReservationDto, memberId);

        List<ReservationMember> reservationMembers = reservationMemberService.allReservations();

        assertAll(
                () -> assertThat(reservationMembers.get(0).getReservationId()).isEqualTo(reservationId),
                () -> assertThat(reservationMembers.get(0).getName()).isEqualTo("test")
        );
    }

    @Test
    void 예약정보_검색_테스트() {
        long timeId = reservationTimeService.addReservationTime(
                new AddReservationTimeDto(LocalTime.now().plusHours(1L)));
        long timeId2 = reservationTimeService.addReservationTime(
                new AddReservationTimeDto(LocalTime.now().plusHours(2L)));

        long themeId = themeService.addTheme(new AddThemeDto("tuda", "asdf", "asdf"));
        long memberId = memberService.signup(new SignupRequestDto("test@naver.com", "testtest", "test"));

        LocalDate today = LocalDate.now();

        AddReservationDto addReservationDto = new AddReservationDto("asdf", today.plusDays(1L), timeId, themeId);
        AddReservationDto addReservationDto2 = new AddReservationDto("asdf2", today.plusDays(2L), timeId2, themeId);

        reservationMemberService.addReservation(addReservationDto, memberId);
        reservationMemberService.addReservation(addReservationDto2, memberId);

        List<ReservationMember> searchedReservations = reservationMemberService.searchReservations(themeId, memberId,
                today, today.plusDays(1L));

        assertThat(searchedReservations).hasSize(1);
    }
}
