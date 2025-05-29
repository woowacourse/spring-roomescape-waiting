package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.common.config.TestConfig;
import roomescape.fixture.TestFixture;
import roomescape.member.application.MemberDataService;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.application.dto.request.ConfirmedReservationCreateRequest;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.presentation.dto.response.WaitingWebResponse;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationslot.infrastructure.ReservationSlotRepository;
import roomescape.reservationslot.presentation.dto.response.ReservationResponse;
import roomescape.reservationtime.application.ReservationTimeDataService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.theme.application.ThemeDataService;
import roomescape.theme.infrastructure.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
class WaitingReservationApplicationServiceTest {

    private static final LocalDate futureDate = TestFixture.makeAfterOneWeekDate();
    private static final LocalDateTime afterOneHour = TestFixture.makeTimeAfterOneHour();

    private WaitingReservationApplicationService waitingReservationApplicationService;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Long timeId;
    private Long themeId;
    private Long memberId2;
    private Long reservationId;

    private ReservationSlotDataService reservationSlotDataService;

    @BeforeEach
    void setUp() {
        reservationSlotDataService = new ReservationSlotDataService(reservationSlotRepository);
        MemberDataService memberDataService = new MemberDataService(memberRepository);
        ReservationDataService reservationDataService = new ReservationDataService(reservationRepository);
        ThemeDataService themeDataService = new ThemeDataService(themeRepository);
        ReservationTimeDataService reservationTimeDataService = new ReservationTimeDataService(
                reservationTimeRepository, reservationSlotDataService);
        waitingReservationApplicationService = new WaitingReservationApplicationService(
                reservationSlotDataService, memberDataService, reservationDataService);
        ConfirmedReservationApplicationService confirmedReservationApplicationService = new ConfirmedReservationApplicationService(
                reservationSlotDataService, reservationTimeDataService, themeDataService, memberDataService,
                reservationDataService);

        timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0))).getId();
        themeId = themeRepository.save(TestFixture.makeTheme()).getId();
        Long memberId = memberRepository.save(TestFixture.makeMember()).getId();
        memberId2 = memberRepository.save(new Member("Free", "free@gmail.com", "password", MemberRole.REGULAR))
                .getId();
        reservationId = confirmedReservationApplicationService.create(
                new ConfirmedReservationCreateRequest(futureDate, timeId, themeId, memberId,
                        afterOneHour)).id();
    }

    @Test
    void create_whenValidRequest_returnReservations() {
        // given

        // when
        waitingReservationApplicationService.create(futureDate, timeId, themeId, memberId2);

        // then
        List<WaitingWebResponse> all = waitingReservationApplicationService.findAll();
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(all.size()).isEqualTo(1);
            softAssertions.assertThat(all.getFirst().name()).isEqualTo("Free");
        });
    }

    @Test
    void findAll() {
        // given
        Long memberId3 = memberRepository.save(new Member("Vector", "vector@gmail.com", "password",
                MemberRole.REGULAR)).getId();
        reservationSlotDataService.getReservationSlotByDateAndTimeAndTheme(futureDate, timeId, themeId);
        waitingReservationApplicationService.create(futureDate, timeId, themeId, memberId2);
        waitingReservationApplicationService.create(futureDate, timeId, themeId, memberId3);

        // when
        List<WaitingWebResponse> all = waitingReservationApplicationService.findAll();

        // then
        assertThat(all.stream().distinct().count()).isEqualTo(2);
    }

    @Test
    void cancelByReservationSlotIdAndMemberId_whenValidRequest_returnVoid() {
        // Given
        Long memberId3 = memberRepository.save(new Member("Vector", "vector@gmail.com", "password",
                MemberRole.REGULAR)).getId();
        ReservationResponse reservationResponse = waitingReservationApplicationService.create(futureDate, timeId,
                themeId, memberId2);
        waitingReservationApplicationService.create(futureDate, timeId, themeId, memberId3);

        // When
        waitingReservationApplicationService.cancelByReservationSlotIdAndMemberId(
                reservationResponse.reservationSlotId(), memberId2);

        // Then
        List<WaitingWebResponse> all = waitingReservationApplicationService.findAll();
        assertThat(all.size()).isOne();
    }

    @Test
    void cancel_whenValidRequest_returnVoid() {
        // Given

        // When
        waitingReservationApplicationService.cancel(reservationId);

        // Then
        List<WaitingWebResponse> all = waitingReservationApplicationService.findAll();
        assertThat(all).isEmpty();
    }
}
