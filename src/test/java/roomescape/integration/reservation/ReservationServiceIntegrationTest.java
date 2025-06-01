package roomescape.integration.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.exception.RoomescapeException;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.support.util.TestCurrentDateTime;
import roomescape.theme.domain.ThemeRepository;
import roomescape.timeslot.domain.TimeSlotRepository;
import roomescape.waiting.domain.WaitingRepository;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = {"/schema.sql", "/test-data.sql"})
public class ReservationServiceIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationService reservationService;
    private TestCurrentDateTime currentDateTime;

    @BeforeEach
    void init() {
        final LocalDateTime now = LocalDateTime.of(2025, 5, 1, 10, 0);
        currentDateTime = new TestCurrentDateTime(now);
        reservationService = new ReservationService(reservationRepository, waitingRepository,
                timeSlotRepository,
                themeRepository,
                memberRepository,
                currentDateTime);
    }

    @DisplayName("새로운 예약을 추가할 수 있다")
    @Test
    void createReservation() {
        // given
        final LocalDate date = currentDateTime.getDate().plusDays(1);
        final ReservationCreateCommand request = new ReservationCreateCommand(date, 1L, 1L, 1L);
        // when
        final ReservationInfo result = reservationService.createReservation(request);
        // then
        assertAll(
                () -> assertThat(result.id()).isNotNull(),
                () -> assertThat(result.member().name()).isEqualTo("리버"),
                () -> assertThat(result.date()).isEqualTo(date),
                () -> assertThat(result.time().id()).isNotNull(),
                () -> assertThat(result.time().startAt()).isEqualTo(LocalTime.of(10, 0))
        );
    }

    @DisplayName("날짜와 시간과 테마가 같은 예약이 이미 존재하면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDuplicateReservation() {
        // given
        final ReservationCreateCommand request = new ReservationCreateCommand(LocalDate.of(2025, 5, 5), 1L, 11L, 1L);
        reservationService.createReservation(request);
        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("이미 예약한 슬롯에 예약 할 수 없습니다.");
    }

    @DisplayName("날짜와 시간이 같아도 테마가 다르면 중복 예외가 발생하지 않는다")
    @Test
    void shouldNot_ThrowException_WhenDifferentTheme() {
        // given
        final LocalDate date = LocalDate.of(2025, 5, 5);
        final ReservationCreateCommand request = new ReservationCreateCommand(date, 1L, 11L, 1L);
        reservationService.createReservation(request);
        final ReservationCreateCommand request2 = new ReservationCreateCommand(date, 1L, 10L, 1L);
        // when & then
        assertThatCode(() -> reservationService.createReservation(request2))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재 혹은 과거 시간에 새로운 예약을 추가할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenPastReservation() {
        // given
        final LocalDate date = currentDateTime.getDate().minusDays(1);
        final ReservationCreateCommand request = new ReservationCreateCommand(date, 1L, 3L, 1L);
        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("이미 지난 슬롯에 예약 대기를 할 수 없습니다.");
    }

    @DisplayName("모든 예약을 조회할 수 있다")
    @Test
    void findReservations() {
        // when
        final List<ReservationInfo> result = reservationService.findReservations();
        // then
        assertThat(result).hasSize(13);
    }

    @DisplayName("id를 기반으로 예약을 취소할 수 있다")
    @Test
    void cancelReservationById() {
        // when
        final List<ReservationInfo> beforeCancelReservations = reservationService.findReservations();
        reservationService.cancelReservationById(1L);
        // then
        final List<ReservationInfo> afterCancelReservations = reservationService.findReservations();
        assertThat(afterCancelReservations).hasSize(beforeCancelReservations.size() - 1);
    }
}
