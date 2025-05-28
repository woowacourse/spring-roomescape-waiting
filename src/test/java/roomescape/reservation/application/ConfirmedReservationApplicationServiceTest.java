package roomescape.reservation.application;

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
import roomescape.common.config.TestConfig;
import roomescape.fixture.TestFixture;
import roomescape.member.application.MemberDataService;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.application.dto.request.ConfirmedReservationByCriteriaWebRequest;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.presentation.dto.response.ConfirmedReservationWebResponse;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationslot.exception.InvalidReservationSlotException;
import roomescape.reservationslot.exception.ReservationSlotAlreadyExistsException;
import roomescape.reservationslot.exception.ReservationSlotNotFoundException;
import roomescape.reservationslot.infrastructure.ReservationSlotRepository;
import roomescape.reservationtime.application.ReservationTimeDataService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.theme.application.ThemeDataService;
import roomescape.theme.infrastructure.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
class ConfirmedReservationApplicationServiceTest {

    private static final LocalDate futureDate = TestFixture.makeAfterOneWeekDate();
    private static final LocalDateTime afterOneHour = TestFixture.makeTimeAfterOneHour();

    private Long timeId;
    private Long themeId;
    private Long memberId;

    private ConfirmedReservationApplicationService confirmedReservationApplicationService;

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
        confirmedReservationApplicationService = new ConfirmedReservationApplicationService(
                reservationSlotDataService,
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
        confirmedReservationApplicationService.create(futureDate, timeId, themeId, memberId,
                afterOneHour);
        confirmedReservationApplicationService.create(futureDate, timeId2, themeId, memberId,
                afterOneHour);
        // when
        List<ConfirmedReservationWebResponse> result = confirmedReservationApplicationService.findByCriteria(
                new ConfirmedReservationByCriteriaWebRequest(null, null, null, null));

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void findFilteredReservations_shouldReturnFilteredReservations() {
        // given
        Long timeId2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0))).getId();
        confirmedReservationApplicationService.create(futureDate.minusDays(1), timeId, themeId, memberId,
                afterOneHour);
        confirmedReservationApplicationService.create(futureDate, timeId2, themeId, memberId,
                afterOneHour);

        // when
        List<ConfirmedReservationWebResponse> result = confirmedReservationApplicationService.findByCriteria(
                new ConfirmedReservationByCriteriaWebRequest(null, null, futureDate, null));

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void removeByIdReservation_shouldRemoveConfirmReservationSuccessfully() {
        ConfirmedReservationWebResponse response = confirmedReservationApplicationService.create(futureDate,
                timeId, themeId,
                memberId, afterOneHour);
        confirmedReservationApplicationService.cancel(response.id());

        List<ConfirmedReservationWebResponse> result = confirmedReservationApplicationService.findByCriteria(
                new ConfirmedReservationByCriteriaWebRequest(themeId, memberId, futureDate, futureDate.plusDays(1)));
        assertThat(result).isEmpty();
    }


    @Test
    void create_shouldReturnResponseWhenSuccessful() {
        ConfirmedReservationWebResponse response = confirmedReservationApplicationService.create(futureDate,
                timeId, themeId, memberId, afterOneHour);

        Assertions.assertAll(
                () -> assertThat(response.member().name()).isEqualTo("Mint"),
                () -> assertThat(response.date()).isEqualTo(futureDate),
                () -> assertThat(response.time().startAt()).isEqualTo(LocalTime.of(9, 0))
        );
    }

    @Test
    void create_shouldThrowException_WhenDuplicated() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        confirmedReservationApplicationService.create(futureDate, timeId, themeId, memberId,
                afterOneHour);

        assertThatThrownBy(
                () -> confirmedReservationApplicationService.create(futureDate, timeId, themeId,
                        memberId, afterOneHour))
                .isInstanceOf(ReservationSlotAlreadyExistsException.class)
                .hasMessageContaining("해당 시간에 이미 예약 슬롯이 존재합니다.");
    }

    @Test
    void create_shouldThrowException_WhenTimeIdNotFound() {
        assertThatThrownBy(
                () -> confirmedReservationApplicationService.create(futureDate, 999L, themeId, memberId,
                        afterOneHour))
                .isInstanceOf(ReservationSlotNotFoundException.class)
                .hasMessageContaining("요청한 id와 일치하는 예약 시간 정보가 없습니다.");
    }

    @Test
    void create_shouldThrowException_WhenPastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        assertThatThrownBy(
                () -> confirmedReservationApplicationService.create(pastDate, timeId, themeId, memberId,
                        afterOneHour))
                .isInstanceOf(InvalidReservationSlotException.class)
                .hasMessageContaining("예약 시간이 현재 시간보다 이전일 수 없습니다.");
    }
}
