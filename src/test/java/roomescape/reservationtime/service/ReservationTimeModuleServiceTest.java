package roomescape.reservationtime.service;

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
import org.springframework.test.context.TestPropertySource;
import roomescape.config.TestConfig;
import roomescape.global.auth.service.MyPasswordEncoder;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberModuleService;
import roomescape.reservation.fixture.TestFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.reservation.service.ReservationCompositeService;
import roomescape.reservation.service.ReservationModuleService;
import roomescape.reservation.service.WaitingModuleService;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.exception.ReservationTimeAlreadyExistsException;
import roomescape.reservationtime.exception.ReservationTimeInUseException;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.ThemeModuleService;

@DataJpaTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never"
})
class ReservationTimeModuleServiceTest {

    private static final LocalDate futureDate = TestFixture.makeFutureDate();
    private static final LocalDateTime afterOneHour = TestFixture.makeTimeAfterOneHour();

    private Theme theme = TestFixture.makeTheme(1L);
    private Member member = TestFixture.makeMember();

    private ReservationTimeModuleService reservationTimeModuleService;
    private ReservationCompositeService reservationCompositeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @BeforeEach
    void setUp() {
        reservationTimeModuleService = new ReservationTimeModuleService(reservationTimeRepository,
                reservationRepository);
        theme = themeRepository.save(theme);
        member = memberRepository.save(member);
        reservationCompositeService = new ReservationCompositeService(
                new ReservationModuleService(reservationRepository),
                new WaitingModuleService(waitingRepository),
                new MemberModuleService(memberRepository, new MyPasswordEncoder()),
                new ThemeModuleService(themeRepository, reservationRepository),
                new ReservationTimeModuleService(reservationTimeRepository, reservationRepository)
        );
    }

    @Test
    void createReservationTime_shouldThrowException_IfDuplicated() {
        LocalTime time = LocalTime.of(1, 1);
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(time);

        reservationTimeModuleService.create(request);

        assertThatThrownBy(() -> reservationTimeModuleService.create(request))
                .isInstanceOf(ReservationTimeAlreadyExistsException.class)
                .hasMessageContaining("중복된 예약 시간을 생성할 수 없습니다.");
    }

    @Test
    void createReservationTime_shouldReturnResponse_WhenSuccessful() {
        LocalTime time = LocalTime.of(9, 0);
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(time);

        ReservationTimeResponse response = reservationTimeModuleService.create(request);

        assertThat(response.startAt()).isEqualTo(time);
    }

    @Test
    void getReservationTimes_shouldReturnAllCreatedTimes() {
        reservationTimeModuleService.create(new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
        reservationTimeModuleService.create(new ReservationTimeCreateRequest(LocalTime.of(11, 0)));

        List<ReservationTimeResponse> result = reservationTimeModuleService.getReservationTimes();

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteReservationTime_shouldRemoveSuccessfully() {
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(13, 30));
        ReservationTimeResponse response = reservationTimeModuleService.create(request);

        reservationTimeModuleService.delete(response.id());

        List<ReservationTimeResponse> result = reservationTimeModuleService.getReservationTimes();
        assertThat(result).isEmpty();
    }

    @Test
    void deleteReservationTime_shouldThrowException_WhenReservationExists() {
        ReservationTimeResponse reservationTimeResponse = reservationTimeModuleService.create(
                new ReservationTimeCreateRequest(LocalTime.now()));
        reservationCompositeService.create(futureDate, reservationTimeResponse.id(), theme.getId(), member.getId(),
                afterOneHour);
        assertThatThrownBy(() -> reservationTimeModuleService.delete(reservationTimeResponse.id()))
                .isInstanceOf(ReservationTimeInUseException.class)
                .hasMessageContaining("해당 시간에 대한 예약이 존재하여 삭제할 수 없습니다.");
    }

    @Test
    void getAvailableReservationTimes_shouldReturnAllAvailableReservationTimes() {
        ReservationTimeResponse reservationTimeResponse = reservationTimeModuleService.create(
                new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
        reservationTimeModuleService.create(new ReservationTimeCreateRequest(LocalTime.of(11, 0)));
        reservationTimeModuleService.create(new ReservationTimeCreateRequest(LocalTime.of(12, 0)));

        reservationCompositeService.create(futureDate, reservationTimeResponse.id(), theme.getId(), member.getId(),
                afterOneHour);
        List<AvailableReservationTimeResponse> availableReservationTimes = reservationTimeModuleService.getAvailableReservationTimes(
                futureDate, theme.getId());

        assertThat(
                availableReservationTimes.stream()
                        .filter(AvailableReservationTimeResponse::alreadyBooked)
                        .count())
                .isEqualTo(1L);
    }
}
