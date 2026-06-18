package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import roomescape.domain.reservation.dto.CreateReservationRequest;
import roomescape.domain.reservation.dto.CreateReservationResponse;
import roomescape.domain.reservation.dto.UpdateReservationRequest;
import roomescape.domain.reservationdate.JpaReservationDateRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationslot.JpaReservationSlotRepository;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.JpaReservationTimeRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.JpaThemeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.JpaUserRepository;
import roomescape.domain.user.User;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.RoomescapeException;

@SpringBootTest
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JpaReservationRepository reservationRepository;

    @Autowired
    private JpaReservationSlotRepository reservationSlotRepository;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private JpaReservationDateRepository reservationDateRepository;

    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JpaThemeRepository themeRepository;

    @TestConfiguration
    static class TestClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            ZoneId zoneId = ZoneId.systemDefault();
            return Clock.fixed(
                LocalDateTime.of(2026, 5, 31, 13, 0)
                    .atZone(zoneId)
                    .toInstant(),
                zoneId
            );
        }
    }

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAllInBatch();
        reservationSlotRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        reservationDateRepository.deleteAllInBatch();
        reservationTimeRepository.deleteAllInBatch();
        themeRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("예약을 생성한다.")
    void createReservation() {
        // given
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");
        Long timeId = saveTime("10:00");
        CreateReservationRequest request = new CreateReservationRequest("보예", dateId, timeId, themeId);

        // when
        CreateReservationResponse response = reservationService.createReservation(request);

        // then
        assertSoftly(softly -> {
            assertThat(response.date()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(response.time()).isEqualTo(LocalTime.of(10, 0));
            assertThat(response.theme().name()).isEqualTo("공포");
            assertThat(findReservationStatus(response.id())).isEqualTo(ReservationStatus.CONFIRMED);
        });
    }

    @Test
    @DisplayName("중복된 예약은 생성할 수 없다.")
    void throwExceptionWhenCreatingDuplicatedReservation() {
        // given
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");
        Long timeId = saveTime("10:00");
        CreateReservationRequest request = new CreateReservationRequest("보예", dateId, timeId, themeId);
        reservationService.createReservation(request);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("중복 예약입니다. 예약 정보를 다시 확인해주세요.");
    }

    @Test
    @DisplayName("오늘보다 이전 날짜는 예약할 수 없다.")
    void throwExceptionWhenCreatingReservationBeforeToday() {
        // given
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-05-30");
        Long timeId = saveTime("10:00");
        CreateReservationRequest request = new CreateReservationRequest("보예", dateId, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("예약 날짜는 오늘 이후여야 합니다. 오늘 날짜:" + LocalDate.of(2026, 5, 31));
    }

    @Test
    @DisplayName("오늘 예약일 경우 현재 시간 이전은 예약할 수 없다.")
    void throwExceptionWhenCreatingReservationBeforeCurrentTimeOnToday() {
        // given
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-05-31");
        Long timeId = saveTime("12:59");
        CreateReservationRequest request = new CreateReservationRequest("보예", dateId, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("예약 시간은 현재 이후여야 합니다. 현재 시각:" + LocalTime.of(13, 0));
    }

    @Test
    @DisplayName("확정 예약자가 예약을 취소하면 첫 번째 대기 예약자가 확정된다.")
    void promoteFirstWaitingReservation() {
        // given
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");
        Long timeId = saveTime("10:00");
        Long slotId = saveReservationSlot(dateId, timeId, themeId);
        Long confirmedReservationId = saveReservation("보예", slotId, ReservationStatus.CONFIRMED, "2026-05-31 13:00:00");
        Long firstWaitingReservationId = saveReservation("수민", slotId, ReservationStatus.WAITING, "2026-05-31 13:01:00");
        Long secondWaitingReservationId = saveReservation("말랑", slotId, ReservationStatus.WAITING, "2026-05-31 13:02:00");

        // when
        reservationService.cancelUserReservation(confirmedReservationId);

        // then
        assertSoftly(softly -> {
            assertThat(findReservationStatus(confirmedReservationId)).isEqualTo(ReservationStatus.CANCELED);
            assertThat(findReservationStatus(firstWaitingReservationId)).isEqualTo(ReservationStatus.CONFIRMED);
            assertThat(findReservationStatus(secondWaitingReservationId)).isEqualTo(ReservationStatus.WAITING);
        });
    }

    @Test
    @DisplayName("예약 날짜와 시간을 수정한다.")
    void updateReservationDateAndTime() {
        // given
        Long themeId = saveTheme("공포");
        Long beforeDateId = saveDate("2026-06-01");
        Long afterDateId = saveDate("2026-06-02");
        Long beforeTimeId = saveTime("10:00");
        Long afterTimeId = saveTime("15:00");
        Long beforeSlotId = saveReservationSlot(beforeDateId, beforeTimeId, themeId);
        Long reservationId = saveReservation("보예", beforeSlotId, ReservationStatus.CONFIRMED, "2026-05-31 13:00:00");
        UpdateReservationRequest request = new UpdateReservationRequest(afterDateId, afterTimeId);

        // when
        reservationService.updateReservation(reservationId, request);

        // then
        Long updatedSlotId = findReservationSlotId(reservationId);
        assertSoftly(softly -> {
            assertThat(findSlotDate(updatedSlotId)).isEqualTo(LocalDate.of(2026, 6, 2));
            assertThat(findSlotTime(updatedSlotId)).isEqualTo(LocalTime.of(15, 0));
            assertThat(findReservationStatus(reservationId)).isEqualTo(ReservationStatus.CONFIRMED);
        });
    }

    @Test
    @DisplayName("존재하지 않는 예약을 수정하면 예외가 발생한다.")
    void throwExceptionWhenUpdatingNonExistentReservation() {
        // given
        UpdateReservationRequest request = new UpdateReservationRequest(1L, 2L);

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(1L, request))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("사용자 예약 신청이 존재하지 않습니다.");
    }

    private Long saveTheme(String name) {
        Theme theme = themeRepository.save(Theme.createWithoutId(name, "무서운 테마", "theme-url"));
        return theme.getId();
    }

    private Long saveDate(String date) {
        ReservationDate reservationDate = reservationDateRepository.save(
            ReservationDate.createWithoutId(LocalDate.parse(date))
        );
        return reservationDate.getId();
    }

    private Long saveTime(String time) {
        ReservationTime reservationTime = reservationTimeRepository.save(
            ReservationTime.createWithoutId(LocalTime.parse(time))
        );
        return reservationTime.getId();
    }

    private Long saveReservationSlot(Long dateId, Long timeId, Long themeId) {
        ReservationDate reservationDate = reservationDateRepository.findById(dateId).orElseThrow();
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).orElseThrow();
        Theme theme = themeRepository.findById(themeId).orElseThrow();
        ReservationSlot reservationSlot = reservationSlotRepository.save(
            ReservationSlot.createWithoutId(reservationDate, reservationTime, theme)
        );
        return reservationSlot.getId();
    }

    private Long saveReservation(
        String username,
        Long reservationSlotId,
        ReservationStatus status,
        String updatedAt
    ) {
        Long userId = saveUser(username);
        User user = userRepository.findById(userId).orElseThrow();
        ReservationSlot reservationSlot = reservationSlotRepository.findById(reservationSlotId).orElseThrow();
        Reservation reservation = reservationRepository.save(
            Reservation.createWithoutId(
                reservationSlot,
                user,
                status,
                fixedClockAt(updatedAt)
            )
        );
        return reservation.getId();
    }

    private Long saveUser(String username) {
        User user = userRepository.save(User.createWithoutId(username));
        return user.getId();
    }

    private ReservationStatus findReservationStatus(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow()
            .getStatus();
    }

    private Long findReservationSlotId(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow()
            .getReservationSlot()
            .getId();
    }

    private LocalDate findSlotDate(Long reservationSlotId) {
        return reservationSlotRepository.findById(reservationSlotId)
            .orElseThrow()
            .getDate()
            .getDate();
    }

    private LocalTime findSlotTime(Long reservationSlotId) {
        return reservationSlotRepository.findById(reservationSlotId)
            .orElseThrow()
            .getTime()
            .getStartAt();
    }

    private Clock fixedClockAt(String dateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        return Clock.fixed(
            LocalDateTime.parse(dateTime.replace(" ", "T"))
                .atZone(zoneId)
                .toInstant(),
            zoneId
        );
    }
}
