package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import roomescape.config.TestConfig;
import roomescape.global.auth.dto.UserInfo;
import roomescape.global.auth.service.MyPasswordEncoder;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.ReservationInfo;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingWithRank;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.exception.WaitingNotFoundException;
import roomescape.reservation.fixture.TestFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.ThemeService;

@DataJpaTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never"
})
class ReservationServiceTest {

    private static final LocalDate futureDate = TestFixture.makeFutureDate();
    private static final LocalDateTime afterOneHour = TestFixture.makeTimeAfterOneHour();

    private ReservationTime time;
    private Theme theme;
    private Member member;

    private ReservationService reservationService;
    private ReservationFacadeService reservationFacadeService;
    private MemberService memberService;
    private ThemeService themeService;
    private ReservationTimeService reservationTimeService;
    private WaitingService waitingService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository);

        reservationFacadeService = new ReservationFacadeService(reservationService,
                new WaitingService(waitingRepository),
                new MemberService(memberRepository, new MyPasswordEncoder()),
                new ThemeService(themeRepository, reservationRepository),
                new ReservationTimeService(reservationTimeRepository, reservationRepository));

        waitingService = new WaitingService(waitingRepository);

        ReservationTime time2 = ReservationTime.withUnassignedId(LocalTime.of(9, 0));
        time = reservationTimeRepository.save(time2);
        theme = themeRepository.save(TestFixture.makeTheme(1L));
        member = memberRepository.save(TestFixture.makeMember());
    }

    @Test
    void createReservation_shouldReturnResponseWhenSuccessful() {
        ReservationResponse response = reservationFacadeService.create(futureDate, time.getId(), theme.getId(),
                member.getId(),
                afterOneHour);

        Assertions.assertAll(
                () -> assertThat(response.member().name()).isEqualTo("Mint"),
                () -> assertThat(response.date()).isEqualTo(futureDate),
                () -> assertThat(response.time().startAt()).isEqualTo(LocalTime.of(9, 0))
        );
    }

    @Test
    void getReservations_shouldReturnAllCreatedReservations() {
        Long timeId2 = reservationTimeRepository.save(ReservationTime.withUnassignedId(LocalTime.of(10, 0))).getId();
        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationFacadeService.create(futureDate, timeId2, theme.getId(), member.getId(), afterOneHour);

        List<ReservationResponse> result = reservationService.findReservations(null, null, null, null);
        assertThat(result).hasSize(2);
    }

    @Test
    void deleteReservation_shouldRemoveSuccessfully() {
        ReservationResponse response = reservationFacadeService.create(futureDate, time.getId(), theme.getId(),
                member.getId(),
                afterOneHour);
        reservationService.delete(response.id());

        List<ReservationResponse> result = reservationService.findReservations(theme.getId(), member.getId(),
                futureDate,
                futureDate.plusDays(1));
        assertThat(result).isEmpty();
    }

    @Test
    void createReservation_shouldThrowException_WhenTimeIdNotFound() {
        assertThatThrownBy(
                () -> reservationFacadeService.create(futureDate, 999L, theme.getId(), member.getId(), afterOneHour))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessageContaining("요청한 id와 일치하는 예약 시간 정보가 없습니다.");
    }

    @Test
    void createWaiting_shouldReturnWaitingResponseWhenReservationExists() {
        ReservationResponse reservation = reservationFacadeService.create(futureDate, time.getId(), theme.getId(),
                member.getId(), afterOneHour);
        ReservationResponse waiting = reservationFacadeService.create(futureDate, time.getId(), theme.getId(),
                member.getId(), afterOneHour);

        Assertions.assertAll(
                () -> assertThat(reservation.reservedStatus()).isEqualTo(ReservationStatus.RESERVED.getName()),
                () -> assertThat(waiting.reservedStatus()).isEqualTo(ReservationStatus.WAITING.getName())
        );
    }

    @Test
    void deleteReservation_shouldPromoteFirstWaiting() {
        ReservationResponse reserved = reservationFacadeService.create(futureDate, time.getId(), theme.getId(),
                member.getId(), afterOneHour);
        ReservationResponse waiting = reservationFacadeService.create(futureDate, time.getId(), theme.getId(),
                member.getId(), afterOneHour);
        assertThat(waiting.reservedStatus()).isEqualTo(ReservationStatus.WAITING.getName());

        reservationFacadeService.deleteReservation(reserved.id());

        List<ReservationResponse> all = reservationService.findReservations(theme.getId(), member.getId(),
                futureDate, futureDate.plusDays(1));
        assertThat(all).hasSize(1)
                .extracting(ReservationResponse::reservedStatus)
                .containsExactly(ReservationStatus.RESERVED.getName());
    }

    @Test
    void findWaitings_shouldReturnAllWaitingAsReservationResponse() {

        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);

        List<ReservationResponse> waitings = waitingService.findWaitings();
        assertThat(waitings).hasSize(2)
                .allSatisfy(response -> assertThat(response.reservedStatus())
                        .isEqualTo(ReservationStatus.WAITING.getName()));
    }

    @Test
    void findMyWaitingsWithRank_shouldReturnCorrectRanks() {

        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);

        List<WaitingWithRank> waiting = waitingService.findMyWaitingsWithRank(
                new UserInfo(member.getId(), MemberRole.USER));
        assertThat(waiting).hasSize(2);
    }

    @Test
    void findMaxOrderByDateAndTimeAndTheme_shouldReflectHighestTurn() {
        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);

        int max1 = waitingService.findMaxOrderByDateAndTimeAndTheme(futureDate, time.getId(), theme.getId());
        assertThat(max1).isEqualTo(1);

        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        int max2 = waitingService.findMaxOrderByDateAndTimeAndTheme(futureDate, time.getId(), theme.getId());
        assertThat(max2).isEqualTo(2);
    }

    @Test
    void isWaitingExists_shouldReturnTrueWhenExists() {
        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);

        boolean exists = waitingService.isWaitingExists(new ReservationInfo(futureDate, time, theme));
        assertThat(exists).isTrue();
    }

    @Test
    void findFirstWaitingOfInfo_shouldReturnEarliestOrThrow() {
        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationFacadeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);

        ReservationInfo info = new ReservationInfo(futureDate, time, theme);

        Waiting first = waitingService.findFirstWaitingOfInfo(info);
        assertThat(first.getTurn()).isEqualTo(1);

        ReservationInfo notExist = new ReservationInfo(futureDate.plusDays(1), time, theme);
        assertThatThrownBy(() -> waitingService.findFirstWaitingOfInfo(notExist))
                .isInstanceOf(WaitingNotFoundException.class)
                .hasMessageContaining("요청한 id와 일치하는 대기 정보가 없습니다.");
    }

}
