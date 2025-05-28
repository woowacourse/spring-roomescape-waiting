package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.common.config.TestConfig;
import roomescape.fixture.TestFixture;
import roomescape.member.application.MemberDataService;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.presentation.dto.response.ConfirmedReservationResponse;
import roomescape.reservationslot.application.ReservationSlotApplicationService;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationslot.infrastructure.ReservationSlotRepository;
import roomescape.reservationtime.application.ReservationTimeDataService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.theme.application.ThemeDataService;
import roomescape.theme.infrastructure.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
class ReservationApplicationServiceTest {

    private static final LocalDate futureDate = TestFixture.makeAfterOneWeekDate();
    private static final LocalDateTime afterOneHour = TestFixture.makeTimeAfterOneHour();

    private Long timeId;
    private Long themeId;
    private Long memberId;

    private ReservationSlotApplicationService reservationSlotApplicationService;

    private ReservationApplicationService reservationApplicationService;

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

    @BeforeEach
    void setUp() {
        ReservationSlotDataService reservationSlotDataService = new ReservationSlotDataService(
                reservationSlotRepository);
        MemberDataService memberDataService = new MemberDataService(memberRepository);
        ReservationDataService reservationDataService = new ReservationDataService(reservationRepository);
        ThemeDataService themeDataService = new ThemeDataService(themeRepository);
        ReservationTimeDataService reservationTimeDataService = new ReservationTimeDataService(
                reservationTimeRepository, reservationSlotDataService);
        reservationApplicationService = new ReservationApplicationService(
                memberDataService,
                reservationSlotDataService,
                reservationDataService);
        reservationSlotApplicationService = new ReservationSlotApplicationService(reservationSlotDataService,
                reservationTimeDataService, themeDataService,
                memberDataService, reservationDataService);

        ReservationTime time2 = new ReservationTime(LocalTime.of(9, 0));
        timeId = reservationTimeRepository.save(time2).getId();
        themeId = themeRepository.save(TestFixture.makeTheme()).getId();
        memberId = memberRepository.save(TestFixture.makeMember()).getId();
    }

    @Test
    void findFilteredReservations_shouldReturnAllReservations() {
        // given
        Long timeId2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0))).getId();
        reservationSlotApplicationService.createConfirmedReservation(futureDate, timeId, themeId, memberId,
                afterOneHour);
        reservationSlotApplicationService.createConfirmedReservation(futureDate, timeId2, themeId, memberId,
                afterOneHour);
        // when
        List<ConfirmedReservationResponse> result = reservationApplicationService.findByCriteria(null, null, null,
                null);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void findFilteredReservations_shouldReturnFilteredReservations() {
        // given
        Long timeId2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0))).getId();
        reservationSlotApplicationService.createConfirmedReservation(futureDate.minusDays(1), timeId, themeId, memberId,
                afterOneHour);
        reservationSlotApplicationService.createConfirmedReservation(futureDate, timeId2, themeId, memberId,
                afterOneHour);

        // when
        List<ConfirmedReservationResponse> result = reservationApplicationService.findByCriteria(null, null,
                futureDate,
                null);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void removeByIdReservation_shouldRemoveSuccessfully() {
        ConfirmedReservationResponse response = reservationSlotApplicationService.createConfirmedReservation(futureDate,
                timeId, themeId,
                memberId, afterOneHour);
        reservationApplicationService.removeById(response.id());

        List<ConfirmedReservationResponse> result = reservationApplicationService.findByCriteria(themeId, memberId,
                futureDate, futureDate.plusDays(1));
        assertThat(result).isEmpty();
    }
}
