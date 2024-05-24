package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.reservation.ReservationTime;
import roomescape.exception.AlreadyExistsException;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.WaitingInput;
import roomescape.util.DatabaseCleaner;

@SpringBootTest
class WaitingServiceTest {

    @Autowired
    WaitingService waitingService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    MemberService memberService;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.initialize();
    }

    @Test
    @DisplayName("예약이 존재하는 시간에 대기를 요청하면 예외를 발생하지 않는다")
    void create_waiting() {
        final long timeId = reservationTimeRepository.save(ReservationTime.from(null, "10:00"))
                .getId();
        final long themeId = themeRepository.save(ThemeFixture.getDomain())
                .getId();
        final long memberId1 = memberService.createMember(MemberFixture.getUserCreateInput())
                .id();
        final long memberId2 = memberService.createMember(MemberFixture.getUserCreateInput("new123@gmail.com"))
                .id();

        reservationService.createReservation(new ReservationInput("2023-03-13", timeId, themeId, memberId1));
        final WaitingInput input = new WaitingInput("2023-03-13", timeId, themeId, memberId2);

        assertThatCode(() -> waitingService.createWaiting(input))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약이 존재하지 않는 시간에 대기를 요청하면 예외를 발생한다.")
    void create_waiting_without_reservation() {
        final long timeId = reservationTimeRepository.save(ReservationTime.from(null, "10:00"))
                .getId();
        final long themeId = themeRepository.save(ThemeFixture.getDomain())
                .getId();
        final long memberId = memberService.createMember(MemberFixture.getUserCreateInput())
                .id();

        final WaitingInput input = new WaitingInput("2023-03-13", timeId, themeId, memberId);

        assertThatThrownBy(() -> waitingService.createWaiting(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("해당 사용자의 예약이 존재하는 시간에 대기를 요청하면 예외를 발생한다.")
    void create_waiting_with_same_user() {
        final long timeId = reservationTimeRepository.save(ReservationTime.from(null, "10:00"))
                .getId();
        final long themeId = themeRepository.save(ThemeFixture.getDomain())
                .getId();
        final long memberId = memberService.createMember(MemberFixture.getUserCreateInput())
                .id();

        reservationService.createReservation(new ReservationInput("2023-03-13", timeId, themeId, memberId));
        final WaitingInput input = new WaitingInput("2023-03-13", timeId, themeId, memberId);

        assertThatCode(() -> waitingService.createWaiting(input))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    @DisplayName("해당 사용자의 예약 대기가 존재하는 시간에 대기를 요청하면 예외를 발생한다.")
    void create_duplicated_waiting() {
        final long timeId = reservationTimeRepository.save(ReservationTime.from(null, "10:00"))
                .getId();
        final long themeId = themeRepository.save(ThemeFixture.getDomain())
                .getId();
        final long memberId1 = memberService.createMember(MemberFixture.getUserCreateInput())
                .id();
        final long memberId2 = memberService.createMember(MemberFixture.getUserCreateInput("new123@gmail.com"))
                .id();

        reservationService.createReservation(new ReservationInput("2023-03-13", timeId, themeId, memberId1));
        final WaitingInput input = new WaitingInput("2023-03-13", timeId, themeId, memberId2);
        waitingService.createWaiting(input);

        assertThatCode(() -> waitingService.createWaiting(input))
                .isInstanceOf(AlreadyExistsException.class);
    }
}
