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
import roomescape.member.service.MemberModuleService;
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
import roomescape.reservationtime.service.ReservationTimeModuleService;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.ThemeModuleService;

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

    private ReservationModuleService reservationModuleService;
    private ReservationCompositeService reservationCompositeService;
    private MemberModuleService memberModuleService;
    private ThemeModuleService themeModuleService;
    private ReservationTimeModuleService reservationTimeModuleService;
    private WaitingModuleService waitingModuleService;

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
        reservationModuleService = new ReservationModuleService(reservationRepository);

        reservationCompositeService = new ReservationCompositeService(reservationModuleService,
                new WaitingModuleService(waitingRepository),
                new MemberModuleService(memberRepository, new MyPasswordEncoder()),
                new ThemeModuleService(themeRepository, reservationRepository),
                new ReservationTimeModuleService(reservationTimeRepository, reservationRepository));

        waitingModuleService = new WaitingModuleService(waitingRepository);

        ReservationTime time2 = ReservationTime.withUnassignedId(LocalTime.of(9, 0));
        time = reservationTimeRepository.save(time2);
        theme = themeRepository.save(TestFixture.makeTheme(1L));
        member = memberRepository.save(TestFixture.makeMember());
    }

    @Test
    void createReservation_shouldReturnResponseWhenSuccessful() {
        ReservationResponse response = reservationCompositeService.create(futureDate, time.getId(), theme.getId(),
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
        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationCompositeService.create(futureDate, timeId2, theme.getId(), member.getId(), afterOneHour);

        List<ReservationResponse> result = reservationModuleService.findReservations(null, null, null, null);
        assertThat(result).hasSize(2);
    }

    @Test
    void deleteReservation_shouldRemoveSuccessfully() {
        ReservationResponse response = reservationCompositeService.create(futureDate, time.getId(), theme.getId(),
                member.getId(),
                afterOneHour);
        reservationModuleService.delete(response.id());

        List<ReservationResponse> result = reservationModuleService.findReservations(theme.getId(), member.getId(),
                futureDate,
                futureDate.plusDays(1));
        assertThat(result).isEmpty();
    }

    @Test
    void createReservation_shouldThrowException_WhenTimeIdNotFound() {
        assertThatThrownBy(
                () -> reservationCompositeService.create(futureDate, 999L, theme.getId(), member.getId(), afterOneHour))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessageContaining("요청한 id와 일치하는 예약 시간 정보가 없습니다.");
    }

    @Test
    void createWaiting_shouldReturnWaitingResponseWhenReservationExists() {
        ReservationResponse reservation = reservationCompositeService.create(futureDate, time.getId(), theme.getId(),
                member.getId(), afterOneHour);
        ReservationResponse waiting = reservationCompositeService.create(futureDate, time.getId(), theme.getId(),
                member.getId(), afterOneHour);

        assertThat(waiting.reservedStatus()).isEqualTo(ReservationStatus.WAITING.getName());
        Assertions.assertAll(
                () -> assertThat(reservation.reservedStatus()).isEqualTo(ReservationStatus.RESERVED.getName()),
                () -> assertThat(waiting.reservedStatus()).isEqualTo(ReservationStatus.WAITING.getName())
        );
    }

    @Test
    void deleteReservation_shouldPromoteFirstWaiting() {
        ReservationResponse reserved = reservationCompositeService.create(futureDate, time.getId(), theme.getId(),
                member.getId(), afterOneHour);
        ReservationResponse waiting = reservationCompositeService.create(futureDate, time.getId(), theme.getId(),
                member.getId(), afterOneHour);
        assertThat(waiting.reservedStatus()).isEqualTo(ReservationStatus.WAITING.getName());

        reservationCompositeService.deleteReservation(reserved.id());

        List<ReservationResponse> all = reservationModuleService.findReservations(theme.getId(), member.getId(),
                futureDate, futureDate.plusDays(1));
        assertThat(all).hasSize(1)
                .extracting(ReservationResponse::reservedStatus)
                .containsExactly(ReservationStatus.RESERVED.getName());
    }

    @Test
    void findWaitings_shouldReturnAllWaitingAsReservationResponse() {

        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);

        List<ReservationResponse> waitings = waitingModuleService.findWaitings();
        assertThat(waitings).hasSize(2)
                .allSatisfy(response -> assertThat(response.reservedStatus())
                        .isEqualTo(ReservationStatus.WAITING.getName()));
    }

    @Test
    void findMyWaitingsWithRank_shouldReturnCorrectRanks() {

        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);

        List<WaitingWithRank> waiting = waitingModuleService.findMyWaitingsWithRank(
                new UserInfo(member.getId(), MemberRole.USER)
        );
        assertThat(waiting).hasSize(2)
                .flatExtracting(WaitingWithRank::getRank)
                .containsExactly(1L, 2L);
    }

    @Test
    void findMaxOrderByDateAndTimeAndTheme_shouldReflectHighestTurn() {
        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);

        int max1 = waitingModuleService.findMaxOrderByDateAndTimeAndTheme(futureDate, time.getId(), theme.getId());
        assertThat(max1).isEqualTo(1);

        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        int max2 = waitingModuleService.findMaxOrderByDateAndTimeAndTheme(futureDate, time.getId(), theme.getId());
        assertThat(max2).isEqualTo(2);
    }

    @Test
    void isWaitingExists_shouldReturnTrueWhenExists() {
        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);

        boolean exists = waitingModuleService.isWaitingExists(
                new ReservationInfo(futureDate,
                        time,
                        theme)
        );
        assertThat(exists).isTrue();
    }

    @Test
    void findFirstWaitingOfInfo_shouldReturnEarliestOrThrow() {
        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);
        reservationCompositeService.create(futureDate, time.getId(), theme.getId(), member.getId(), afterOneHour);

        ReservationInfo info = new ReservationInfo(
                futureDate,
                time,
                theme
        );

        Waiting first = waitingModuleService.findFirstWaitingOfInfo(info);
        assertThat(first.getTurn()).isEqualTo(1);

        ReservationInfo notExist =
                new ReservationInfo(futureDate.plusDays(1),
                        time,
                        theme);
        assertThatThrownBy(() -> waitingModuleService.findFirstWaitingOfInfo(notExist))
                .isInstanceOf(WaitingNotFoundException.class)
                .hasMessageContaining("요청한 id와 일치하는 대기 정보가 없습니다.");
    }

}
