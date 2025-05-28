package roomescape.reservationtime.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.application.ReservationDataService;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservationslot.application.ReservationSlotApplicationService;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationslot.infrastructure.ReservationSlotRepository;
import roomescape.reservationtime.exception.ReservationTimeAlreadyExistsException;
import roomescape.reservationtime.exception.ReservationTimeInUseException;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.reservationtime.presentation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeResponse;
import roomescape.theme.application.ThemeDataService;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
class ReservationTimeApplicationServiceTest {

    private static final LocalDate futureDate = TestFixture.makeAfterOneWeekDate();
    private static final LocalDateTime afterOneHour = TestFixture.makeTimeAfterOneHour();

    private Theme theme = TestFixture.makeTheme();
    private Member member = TestFixture.makeMember();

    private ReservationSlotApplicationService reservationSlotApplicationService;
    private ReservationTimeApplicationService reservationTimeApplicationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

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
        ReservationTimeDataService reservationTimeDataService = new ReservationTimeDataService(
                reservationTimeRepository, reservationSlotDataService);
        reservationTimeApplicationService = new ReservationTimeApplicationService(reservationTimeDataService);
        ThemeDataService themeDataService = new ThemeDataService(themeRepository);
        MemberDataService memberDataService = new MemberDataService(memberRepository);
        theme = themeRepository.save(theme);
        member = memberRepository.save(member);
        ReservationDataService slotReservationDataService = new ReservationDataService(reservationRepository);
        reservationSlotApplicationService = new ReservationSlotApplicationService(reservationSlotDataService,
                reservationTimeDataService, themeDataService, memberDataService, slotReservationDataService);
    }

    @Test
    void createReservationTime_shouldThrowException_IfDuplicated() {
        LocalTime time = LocalTime.of(1, 1);
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(time);

        reservationTimeApplicationService.create(request);

        assertThatThrownBy(() -> reservationTimeApplicationService.create(request))
                .isInstanceOf(ReservationTimeAlreadyExistsException.class)
                .hasMessageContaining("중복된 예약 시간을 생성할 수 없습니다.");
    }

    @Test
    void createReservationTime_shouldReturnResponse_WhenSuccessful() {
        LocalTime time = LocalTime.of(9, 0);
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(time);

        ReservationTimeResponse response = reservationTimeApplicationService.create(request);

        assertThat(response.startAt()).isEqualTo(time);
    }

    @Test
    void findAll() {
        reservationTimeApplicationService.create(new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
        reservationTimeApplicationService.create(new ReservationTimeCreateRequest(LocalTime.of(11, 0)));

        List<ReservationTimeResponse> result = reservationTimeApplicationService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void removeByIdReservationTime_shouldRemoveSuccessfully() {
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(13, 30));
        ReservationTimeResponse response = reservationTimeApplicationService.create(request);

        reservationTimeApplicationService.removeById(response.id());

        List<ReservationTimeResponse> result = reservationTimeApplicationService.findAll();
        assertThat(result).isEmpty();
    }

    @Test
    void removeByIdReservationTime_shouldThrowException_WhenReservationExists() {
        ReservationTimeResponse reservationTimeResponse = reservationTimeApplicationService.create(
                new ReservationTimeCreateRequest(LocalTime.now()));
        reservationSlotApplicationService.createConfirmedReservation(futureDate, reservationTimeResponse.id(),
                theme.getId(), member.getId(),
                afterOneHour);
        assertThatThrownBy(() -> reservationTimeApplicationService.removeById(reservationTimeResponse.id()))
                .isInstanceOf(ReservationTimeInUseException.class)
                .hasMessageContaining("해당 시간에 대한 예약이 존재하여 삭제할 수 없습니다.");
    }

    @Test
    void findAvailableReservationTimes_shouldReturnAllAvailable() {
        ReservationTimeResponse reservationTimeResponse = reservationTimeApplicationService.create(
                new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
        reservationTimeApplicationService.create(new ReservationTimeCreateRequest(LocalTime.of(11, 0)));
        reservationTimeApplicationService.create(new ReservationTimeCreateRequest(LocalTime.of(12, 0)));

        reservationSlotApplicationService.createConfirmedReservation(futureDate, reservationTimeResponse.id(),
                theme.getId(), member.getId(),
                afterOneHour);
        List<AvailableReservationTimeResponse> availableReservationTimes = reservationTimeApplicationService.findAvailable(
                futureDate, theme.getId());

        assertThat(
                availableReservationTimes.stream()
                        .filter(AvailableReservationTimeResponse::alreadyBooked)
                        .count())
                .isEqualTo(1L);
    }
}
