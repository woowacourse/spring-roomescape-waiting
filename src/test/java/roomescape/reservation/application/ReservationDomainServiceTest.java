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
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.domain.service.MemberDomainService;
import roomescape.reservation.domain.service.ReservationDomainService;
import roomescape.reservation.presentation.dto.response.ReservationResponse;
import roomescape.reservation.exception.ReservationAlreadyExistsException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.reservationtime.domain.service.ReservationTimeDomainService;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.domain.service.ThemeDomainService;

@DataJpaTest
@Import(TestConfig.class)
class ReservationDomainServiceTest {

    private static final LocalDate futureDate = TestFixture.makeFutureDate();
    private static final LocalDateTime afterOneHour = TestFixture.makeTimeAfterOneHour();

    private Long timeId;
    private Long themeId;
    private Long memberId;

    private ReservationApplicationService reservationApplicationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        ReservationDomainService reservationDomainService = new ReservationDomainService(reservationRepository);
        reservationApplicationService = new ReservationApplicationService(
                reservationDomainService,
                new ReservationTimeDomainService(reservationTimeRepository, reservationDomainService),
                new ThemeDomainService(themeRepository, reservationRepository),
                new MemberDomainService(memberRepository));

        ReservationTime time2 = ReservationTime.withUnassignedId(LocalTime.of(9, 0));
        timeId = reservationTimeRepository.save(time2).getId();
        themeId = themeRepository.save(TestFixture.makeTheme()).getId();
        memberId = memberRepository.save(TestFixture.makeMember()).getId();
    }

    @Test
    void createReservation_shouldReturnResponseWhenSuccessful() {
        ReservationResponse response = reservationApplicationService.create(futureDate, timeId, themeId, memberId,
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
        reservationApplicationService.create(futureDate, timeId, themeId, memberId, afterOneHour);
        reservationApplicationService.create(futureDate, timeId2, themeId, memberId, afterOneHour);

        List<ReservationResponse> result = reservationApplicationService.findReservations(null, null, null, null);
        assertThat(result).hasSize(2);
    }

    @Test
    void deleteReservation_shouldRemoveSuccessfully() {
        ReservationResponse response = reservationApplicationService.create(futureDate, timeId, themeId, memberId,
                afterOneHour);
        reservationApplicationService.delete(response.id());

        List<ReservationResponse> result = reservationApplicationService.findReservations(themeId, memberId, futureDate,
                futureDate.plusDays(1));
        assertThat(result).isEmpty();
    }

    @Test
    void createReservation_shouldThrowException_WhenDuplicated() {
        reservationTimeRepository.save(ReservationTime.withUnassignedId(LocalTime.of(10, 0)));
        reservationApplicationService.create(futureDate, timeId, themeId, memberId, afterOneHour);

        assertThatThrownBy(
                () -> reservationApplicationService.create(futureDate, timeId, themeId, memberId, afterOneHour))
                .isInstanceOf(ReservationAlreadyExistsException.class)
                .hasMessageContaining("해당 시간에 이미 예약이 존재합니다.");
    }

    @Test
    void createReservation_shouldThrowException_WhenTimeIdNotFound() {
        assertThatThrownBy(
                () -> reservationApplicationService.create(futureDate, 999L, themeId, memberId, afterOneHour))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessageContaining("요청한 id와 일치하는 예약 시간 정보가 없습니다.");
    }
}
