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
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservationslot.domain.service.ReservationSlotDomainService;
import roomescape.common.config.TestConfig;
import roomescape.fixture.TestFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.service.MemberDomainService;
import roomescape.reservationslot.application.ReservationSlotApplicationService;
import roomescape.reservationslot.infrastructure.ReservationSlotRepository;
import roomescape.reservationtime.domain.service.ReservationTimeDomainService;
import roomescape.reservationtime.exception.ReservationTimeAlreadyExistsException;
import roomescape.reservationtime.exception.ReservationTimeInUseException;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.reservationtime.presentation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.service.ThemeDomainService;
import roomescape.reservation.domain.service.ReservationDomainService;
import roomescape.theme.infrastructure.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
class ReservationTimeDomainServiceTest {

    private static final LocalDate futureDate = TestFixture.makeFutureDate();
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
        ReservationSlotDomainService reservationSlotDomainService = new ReservationSlotDomainService(reservationSlotRepository);
        ReservationTimeDomainService reservationTimeDomainService = new ReservationTimeDomainService(
                reservationTimeRepository, reservationSlotDomainService);
        reservationTimeApplicationService = new ReservationTimeApplicationService(reservationTimeDomainService,
                reservationSlotDomainService);
        ThemeDomainService themeDomainService = new ThemeDomainService(themeRepository, reservationSlotRepository);
        MemberDomainService memberDomainService = new MemberDomainService(memberRepository);
        theme = themeRepository.save(theme);
        member = memberRepository.save(member);
        ReservationDomainService slotReservationDomainService = new ReservationDomainService(reservationRepository);
        reservationSlotApplicationService = new ReservationSlotApplicationService(reservationSlotDomainService,
                reservationTimeDomainService, themeDomainService, memberDomainService, slotReservationDomainService);
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

        List<ReservationTimeResponse> result = reservationTimeApplicationService.getReservationTimes();

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteReservationTime_shouldRemoveSuccessfully() {
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(13, 30));
        ReservationTimeResponse response = reservationTimeApplicationService.create(request);

        reservationTimeApplicationService.delete(response.id());

        List<ReservationTimeResponse> result = reservationTimeApplicationService.getReservationTimes();
        assertThat(result).isEmpty();
    }

    @Test
    void deleteReservationTime_shouldThrowException_WhenReservationExists() {
        ReservationTimeResponse reservationTimeResponse = reservationTimeApplicationService.create(
                new ReservationTimeCreateRequest(LocalTime.now()));
        reservationSlotApplicationService.create(futureDate, reservationTimeResponse.id(), theme.getId(), member.getId(),
                afterOneHour);
        assertThatThrownBy(() -> reservationTimeApplicationService.delete(reservationTimeResponse.id()))
                .isInstanceOf(ReservationTimeInUseException.class)
                .hasMessageContaining("해당 시간에 대한 예약이 존재하여 삭제할 수 없습니다.");
    }

    @Test
    void getAvailableReservationTimes_shouldReturnAllAvailableReservationTimes() {
        ReservationTimeResponse reservationTimeResponse = reservationTimeApplicationService.create(
                new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
        reservationTimeApplicationService.create(new ReservationTimeCreateRequest(LocalTime.of(11, 0)));
        reservationTimeApplicationService.create(new ReservationTimeCreateRequest(LocalTime.of(12, 0)));

        reservationSlotApplicationService.create(futureDate, reservationTimeResponse.id(), theme.getId(), member.getId(),
                afterOneHour);
        List<AvailableReservationTimeResponse> availableReservationTimes = reservationTimeApplicationService.getAvailableReservationTimes(
                futureDate, theme.getId());

        assertThat(
                availableReservationTimes.stream()
                        .filter(AvailableReservationTimeResponse::alreadyBooked)
                        .count())
                .isEqualTo(1L);
    }
}
