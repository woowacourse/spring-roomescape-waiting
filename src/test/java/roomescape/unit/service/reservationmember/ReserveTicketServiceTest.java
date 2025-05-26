package roomescape.unit.service.reservationmember;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reserveticket.ReserveTicket;
import roomescape.dto.member.SignupRequestDto;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.dto.reservationtime.AddReservationTimeDto;
import roomescape.dto.reservationtime.AvailableTimeRequestDto;
import roomescape.dto.theme.AddThemeDto;
import roomescape.dto.waiting.ApplyWaitingRequestDto;
import roomescape.infrastructure.auth.jwt.JwtTokenProvider;
import roomescape.service.member.MemberService;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.reserveticket.ReserveTicketService;
import roomescape.service.theme.ThemeService;
import roomescape.service.waiting.WaitingService;
import roomescape.unit.config.ServiceFixture;
import roomescape.unit.repository.member.FakeMemberRepository;
import roomescape.unit.repository.reservation.FakeReservationRepository;
import roomescape.unit.repository.reservationtime.FakeReservationTimeRepository;
import roomescape.unit.repository.reservation.FakeThemeRepository;
import roomescape.unit.repository.reserveticket.FakeReserveTicketRepository;
import roomescape.unit.repository.waiting.FakeWaitingRepository;

class ReserveTicketServiceTest {

    private ReservationService reservationService;
    private ReserveTicketService reserveTicketService;
    private ReservationTimeService reservationTimeService;
    private ThemeService themeService;
    private MemberService memberService;
    private WaitingService waitingService;

    @BeforeEach
    void setup() {
        FakeReservationRepository fakeReservationRepository = ServiceFixture.fakeReservationRepository();
        FakeReservationTimeRepository fakeReservationTimeRepository = ServiceFixture.fakeReservationTimeRepository();
        FakeThemeRepository fakeThemeRepository = ServiceFixture.fakeThemeRepository();
        reservationService = new ReservationService(fakeReservationRepository,
                fakeReservationTimeRepository, fakeThemeRepository);

        reservationTimeService = new ReservationTimeService(fakeReservationRepository, fakeReservationTimeRepository);
        themeService = new ThemeService(fakeThemeRepository, fakeReservationRepository);

        JwtTokenProvider jwtTokenProvider = ServiceFixture.jwtTokenProvider();
        FakeMemberRepository fakeMemberRepository = ServiceFixture.fakeMemberRepository();
        BCryptPasswordEncoder bCryptPasswordEncoder = ServiceFixture.passwordEncoder();
        memberService = new MemberService(bCryptPasswordEncoder, fakeMemberRepository, jwtTokenProvider);

        FakeWaitingRepository fakeWaitingRepository = ServiceFixture.fakeWaitingRepository();
        waitingService = new WaitingService(fakeWaitingRepository, memberService, reservationTimeService, themeService);

        FakeReserveTicketRepository reservationMemberRepository = ServiceFixture.fakeReserveTicketRepository();
        reserveTicketService = new ReserveTicketService(reservationMemberRepository, memberService, reservationService, waitingService);
    }

    @Test
    void 유저정보를_추가하여_예약을_진행한다() {
        long timeId = reservationTimeService.addReservationTime(
                new AddReservationTimeDto(LocalTime.now().plusMinutes(1L)));
        long themeId = themeService.addTheme(new AddThemeDto("tuda", "asdf", "asdf"));
        AddReservationDto addReservationDto = new AddReservationDto(LocalDate.now(), Long.valueOf(timeId),
                Long.valueOf(themeId));

        long memberId = memberService.signup(new SignupRequestDto("test@naver.com", "testtest", "test"));
        assertThatCode(
                () -> reserveTicketService.addReservationIfWaitingNotExists(addReservationDto, memberId)).doesNotThrowAnyException();
    }

    @Test
    void 예약대기가_있는_시간에는_예약을_추가할_수_없다() {
        long timeId = reservationTimeService.addReservationTime(
                new AddReservationTimeDto(LocalTime.now().plusMinutes(1L)));
        long themeId = themeService.addTheme(new AddThemeDto("tuda", "asdf", "asdf"));
        AddReservationDto addReservationDto = new AddReservationDto(LocalDate.now(), Long.valueOf(timeId), Long.valueOf(themeId));
        long memberId = memberService.signup(new SignupRequestDto("test@naver.com", "testtest", "test"));
        waitingService.addWaiting(addReservationDto, memberId);

        assertThatThrownBy(() -> reserveTicketService.addReservationIfWaitingNotExists(addReservationDto, memberId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 존재하지않는_유저로_예약을_진행하면_예외가_발생한다() {
        long timeId = reservationTimeService.addReservationTime(
                new AddReservationTimeDto(LocalTime.now().plusHours(1L)));
        long themeId = themeService.addTheme(new AddThemeDto("tuda", "asdf", "asdf"));
        AddReservationDto addReservationDto = new AddReservationDto(LocalDate.now(), Long.valueOf(timeId),
                Long.valueOf(themeId));

        long memberId = -1;

        assertThatThrownBy(() -> reserveTicketService.addReservationIfWaitingNotExists(addReservationDto, memberId)).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    void 유저와_예약_정보를_합친_정보를_반환할_수_있다() {
        long timeId = reservationTimeService.addReservationTime(
                new AddReservationTimeDto(LocalTime.now().plusMinutes(1L)));
        long themeId = themeService.addTheme(new AddThemeDto("tuda", "asdf", "asdf"));

        AddReservationDto addReservationDto = new AddReservationDto(LocalDate.now(), Long.valueOf(timeId),
                Long.valueOf(themeId));

        long memberId = memberService.signup(new SignupRequestDto("test@naver.com", "testtest", "test"));
        long reservationId = reserveTicketService.addReservationIfWaitingNotExists(addReservationDto, memberId);

        List<ReserveTicket> reserveTickets = reserveTicketService.allReservations();

        assertAll(
                () -> assertThat(reserveTickets.get(0).getReservationId()).isEqualTo(reservationId),
                () -> assertThat(reserveTickets.get(0).getName()).isEqualTo("test")
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

        AddReservationDto addReservationDto = new AddReservationDto(today.plusDays(1L), timeId, themeId);
        AddReservationDto addReservationDto2 = new AddReservationDto(today.plusDays(2L), timeId2, themeId);

        reserveTicketService.addReservationIfWaitingNotExists(addReservationDto, memberId);
        reserveTicketService.addReservationIfWaitingNotExists(addReservationDto2, memberId);

        List<ReserveTicket> searchedReservations = reserveTicketService.searchReservations(themeId, memberId,
                today, today.plusDays(1L));

        assertThat(searchedReservations).hasSize(1);
    }

    @Test
    void 멤버_별_예약_정보를_확인할_수_있다() {
        long memberId = memberService.signup(new SignupRequestDto("email@email.com", "password", "name"));
        long timeId = reservationTimeService.addReservationTime(new AddReservationTimeDto(LocalTime.of(10, 0)));
        long themeId = themeService.addTheme(new AddThemeDto("name", "description", "thumbnail"));
        long firstId = reserveTicketService.addReservationIfWaitingNotExists(
                new AddReservationDto(LocalDate.now().plusDays(1), timeId, themeId), memberId);
        long secondId = reserveTicketService.addReservationIfWaitingNotExists(
                new AddReservationDto(LocalDate.now().plusDays(2), timeId, themeId), memberId);

        List<ReserveTicket> reserveTickets = reserveTicketService.memberReservations(memberId);
        List<Long> reservationMemberIds = reserveTickets.stream()
                .map(ReserveTicket::getReservationId)
                .toList();

        assertThat(reservationMemberIds).containsAnyElementsOf(List.of(firstId, secondId));
    }

    @Test
    void 선택된_테마와_날짜에_대해서_가능한_시간들을_확인할_수_있다() {
        LocalTime firstTime = LocalTime.now().plusMinutes(1L);
        LocalTime secondTime = LocalTime.now().plusMinutes(2L);

        LocalDate today = LocalDate.now();
        Long firstReservationTimeId = reservationTimeService.addReservationTime(new AddReservationTimeDto(firstTime));
        Long secondReservationTimeId = reservationTimeService.addReservationTime(new AddReservationTimeDto(secondTime));
        Long themeId = themeService.addTheme(new AddThemeDto("테마", "테마2", "unique.png"));
        Long memberId = memberService.signup(new SignupRequestDto("email@email.com", "password", "name"));

        reserveTicketService.addReservationIfWaitingNotExists(
                new AddReservationDto(today, firstReservationTimeId, themeId), memberId);

        AvailableTimeRequestDto availableTimeRequestDto = new AvailableTimeRequestDto(today, themeId);
        List<ReservationSlot> reservationAvailabilities = reserveTicketService.availableReservationTimes(
                        availableTimeRequestDto)
                .getAvailableBookTimes();

        List<ReservationSlot> reservationSlots = List.of(new ReservationSlot(1L, firstTime, true),
                new ReservationSlot(2L, secondTime, false));

        assertThat(reservationAvailabilities).containsExactlyInAnyOrderElementsOf(reservationSlots);
    }

    @Test
    void 예약대기_엔티티를_승인하여_예약_엔티티로_등록시킬_수_있다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = reservationTimeService.addReservationTime(new AddReservationTimeDto(LocalTime.now().plusMinutes(1)));
        Long themeId = themeService.addTheme(new AddThemeDto("theme", "description", "thumbnail"));
        long memberId = memberService.signup(new SignupRequestDto("email", "password", "name"));
        Long savedWaitingId = waitingService.addWaiting(new AddReservationDto(date, timeId, themeId), memberId);

        // When
        Long createdReservationId = reserveTicketService.applyWaiting(new ApplyWaitingRequestDto(savedWaitingId));

        // Then
        assertAll(() -> {
            assertThat(createdReservationId).isEqualTo(1L);
            Assertions.assertThatThrownBy(() -> waitingService.getWaitingById(savedWaitingId)).isInstanceOf(IllegalArgumentException.class);
            assertThat(reservationService.getReservationById(createdReservationId)).isEqualTo(new Reservation(createdReservationId, memberService.getMemberById(memberId).getName(), date, reservationTimeService.getReservationTimeById(timeId), themeService.getThemeById(themeId)));
        });
    }
}
