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
import roomescape.global.auth.service.MyPasswordEncoder;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.exception.ReservationAlreadyExistsException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.fixture.TestFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.ThemeService;

@DataJpaTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations="
})
class ReservationServiceTest {

    private static final LocalDate futureDate = TestFixture.makeFutureDate();
    private static final LocalDateTime afterOneHour = TestFixture.makeTimeAfterOneHour();

    private Long timeId;
    private Long themeId;
    private Long memberId;

    private ReservationModuleService reservationModuleService;
    private ReservationCompositeService reservationCompositeService;
    private MemberService memberService;
    private ThemeService themeService;
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WaitingReservationRepository waitingReservationRepository;

    @BeforeEach
    void setUp() {
        reservationModuleService = new ReservationModuleService(reservationRepository);

        reservationCompositeService = new ReservationCompositeService(reservationModuleService,
                new WaitingModuleService(waitingReservationRepository),
                new MemberService(memberRepository, new MyPasswordEncoder()),
                new ThemeService(themeRepository, reservationRepository),
                new ReservationTimeService(reservationTimeRepository, reservationRepository));

        ReservationTime time2 = ReservationTime.withUnassignedId(LocalTime.of(9, 0));
        timeId = reservationTimeRepository.save(time2).getId();
        themeId = themeRepository.save(TestFixture.makeTheme(1L)).getId();
        memberId = memberRepository.save(TestFixture.makeMember()).getId();
    }

    @Test
    void createReservation_shouldReturnResponseWhenSuccessful() {
        ReservationResponse response = reservationCompositeService.create(futureDate, timeId, themeId, memberId,
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
        reservationCompositeService.create(futureDate, timeId, themeId, memberId, afterOneHour);
        reservationCompositeService.create(futureDate, timeId2, themeId, memberId, afterOneHour);

        List<ReservationResponse> result = reservationModuleService.findReservations(null, null, null, null);
        assertThat(result).hasSize(2);
    }

    @Test
    void deleteReservation_shouldRemoveSuccessfully() {
        ReservationResponse response = reservationCompositeService.create(futureDate, timeId, themeId, memberId,
                afterOneHour);
        reservationModuleService.delete(response.id());

        List<ReservationResponse> result = reservationModuleService.findReservations(themeId, memberId, futureDate,
                futureDate.plusDays(1));
        assertThat(result).isEmpty();
    }

    @Test
    void createReservation_shouldThrowException_WhenDuplicated() {
        reservationTimeRepository.save(ReservationTime.withUnassignedId(LocalTime.of(10, 0)));
        reservationCompositeService.createReservation(futureDate, timeId, themeId, memberId, afterOneHour);

        assertThatThrownBy(() -> reservationCompositeService.createReservation(futureDate, timeId, themeId, memberId, afterOneHour))
                .isInstanceOf(ReservationAlreadyExistsException.class)
                .hasMessageContaining("해당 시간에 이미 예약이 존재합니다.");
    }

    @Test
    void createReservation_shouldThrowException_WhenTimeIdNotFound() {
        assertThatThrownBy(() -> reservationCompositeService.create(futureDate, 999L, themeId, memberId, afterOneHour))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessageContaining("요청한 id와 일치하는 예약 시간 정보가 없습니다.");
    }

    @Test
    void createWaiting_shouldReturnWaitingResponseWhenReservationExists() {
        ReservationResponse reservation = reservationCompositeService.create(futureDate, timeId, themeId, memberId, afterOneHour);
        ReservationResponse waiting = reservationCompositeService.create(futureDate, timeId, themeId, memberId, afterOneHour);

        assertThat(waiting.id()).isEqualTo(1L);
        assertThat(waiting.reservedStatus()).isEqualTo(ReservationStatus.WAITING.getName());
        Assertions.assertAll(
                () -> assertThat(reservation.reservedStatus()).isEqualTo(ReservationStatus.RESERVED.getName()),
                () -> assertThat(waiting.reservedStatus()).isEqualTo(ReservationStatus.WAITING.getName()),
                () -> assertThat(waiting.id()).isEqualTo(1L)
        );
    }

    @Test
    void deleteReservation_shouldPromoteFirstWaiting() {
        ReservationResponse reserved = reservationCompositeService.create(futureDate, timeId, themeId, memberId, afterOneHour);
        ReservationResponse waiting = reservationCompositeService.create(futureDate, timeId, themeId, memberId, afterOneHour);
        assertThat(waiting.reservedStatus()).isEqualTo(ReservationStatus.WAITING.getName());

        reservationCompositeService.deleteReservation(reserved.id());

        List<ReservationResponse> all = reservationModuleService.findReservations(themeId, memberId, futureDate, futureDate.plusDays(1));
        assertThat(all).hasSize(1)
                .extracting(ReservationResponse::reservedStatus)
                .containsExactly(ReservationStatus.RESERVED.getName());
    }
}
