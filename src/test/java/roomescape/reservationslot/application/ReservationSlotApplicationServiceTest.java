package roomescape.reservationslot.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.common.config.TestConfig;
import roomescape.fixture.TestFixture;
import roomescape.member.application.MemberDataService;
import roomescape.reservationslot.exception.ReservationSlotAlreadyExistsException;
import roomescape.reservationslot.exception.ReservationSlotNotFoundException;
import roomescape.reservationslot.infrastructure.ReservationSlotRepository;
import roomescape.reservation.presentation.dto.response.TotalReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.application.ReservationTimeDataService;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.theme.application.ThemeDataService;
import roomescape.reservation.application.ReservationDataService;
import roomescape.theme.infrastructure.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
class ReservationSlotApplicationServiceTest {

    private static final LocalDate futureDate = TestFixture.makeFutureDate();
    private static final LocalDateTime afterOneHour = TestFixture.makeTimeAfterOneHour();

    private Long timeId;
    private Long themeId;
    private Long memberId;

    private ReservationSlotApplicationService reservationSlotApplicationService;

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
        ReservationSlotDataService reservationSlotDataService = new ReservationSlotDataService(reservationSlotRepository);
        reservationSlotApplicationService = new ReservationSlotApplicationService(
                reservationSlotDataService,
                new ReservationTimeDataService(reservationTimeRepository, reservationSlotDataService),
                new ThemeDataService(themeRepository, reservationSlotRepository),
                new MemberDataService(memberRepository),
                new ReservationDataService(reservationRepository)
        );

        ReservationTime time2 = ReservationTime.withUnassignedId(LocalTime.of(9, 0));
        timeId = reservationTimeRepository.save(time2).getId();
        themeId = themeRepository.save(TestFixture.makeTheme()).getId();
        memberId = memberRepository.save(TestFixture.makeMember()).getId();
    }

    @Test
    void createReservation_shouldReturnResponseWhenSuccessful() {
        TotalReservationResponse response = reservationSlotApplicationService.create(futureDate, timeId, themeId, memberId,
                afterOneHour);

        Assertions.assertAll(
                () -> assertThat(response.member().name()).isEqualTo("Mint"),
                () -> assertThat(response.date()).isEqualTo(futureDate),
                () -> assertThat(response.time().startAt()).isEqualTo(LocalTime.of(9, 0))
        );
    }

    @Test
    void createReservation_shouldThrowException_WhenDuplicated() {
        reservationTimeRepository.save(ReservationTime.withUnassignedId(LocalTime.of(10, 0)));
        reservationSlotApplicationService.create(futureDate, timeId, themeId, memberId, afterOneHour);

        assertThatThrownBy(
                () -> reservationSlotApplicationService.create(futureDate, timeId, themeId, memberId, afterOneHour))
                .isInstanceOf(ReservationSlotAlreadyExistsException.class)
                .hasMessageContaining("해당 시간에 이미 예약이 존재합니다.");
    }

    @Test
    void createReservation_shouldThrowException_WhenTimeIdNotFound() {
        assertThatThrownBy(
                () -> reservationSlotApplicationService.create(futureDate, 999L, themeId, memberId, afterOneHour))
                .isInstanceOf(ReservationSlotNotFoundException.class)
                .hasMessageContaining("요청한 id와 일치하는 예약 시간 정보가 없습니다.");
    }
}
