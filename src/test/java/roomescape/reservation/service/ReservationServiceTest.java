package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservation.exception.ReservationErrorCode.CANNOT_EDIT_ALREADY_STARTED_RESERVATION;
import static roomescape.reservation.exception.ReservationErrorCode.CANNOT_EDIT_OTHER_GUEST_RESERVATION;
import static roomescape.reservation.exception.ReservationErrorCode.PAST_RESERVATION_NOT_ALLOWED;
import static roomescape.reservation.exception.ReservationErrorCode.RESERVATION_ALREADY_CANCELED;
import static roomescape.reservation.exception.ReservationErrorCode.RESERVATION_NOT_FOUND;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.common.exception.DomainException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationWaitingResult;
import roomescape.reservation.service.policy.ReservationPolicy;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.JdbcReservationTimeRepository;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.test_config.MutableTimeManager;
import roomescape.test_config.TestTimeManagerConfig;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.theme.repository.ThemeRepository;

@JdbcTest
@Import({
        TestTimeManagerConfig.class,
        ReservationService.class,
        JdbcReservationRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcThemeRepository.class,
        ReservationPolicy.class
})
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    MutableTimeManager timeManager;


    @Test
    @DisplayName("해당 날짜, 시간, 테마에 처음으로 예약을 추가하면 예약이 확정된다.")
    public void create_success1() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2025, 5, 11);

        timeManager.setFixed(LocalDate.of(2025, 5, 10));

        // when
        ReservationWaitingResult reservationWaitingResult =
                reservationService.create("포비", date, time.getId(), theme.getId());

        // then
        assertThat(reservationWaitingResult.status()).isEqualTo(Status.CONFIRMED);
    }

    @Test
    @DisplayName("예약이 존재하는 날짜, 시간, 테마로 새로운 예약을 추가하면 예약이 대기 상태로 들어간다.")
    public void create_success2() {
        // given
        timeManager.setFixed(LocalDate.of(2025, 5, 10));

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2025, 5, 11);
        insertConfirmedReservation(date, time, theme, "포비");

        // when
        ReservationWaitingResult reservationWaitingResult =
                reservationService.create("브라운", date, time.getId(), theme.getId());

        // then
        assertThat(reservationWaitingResult.status()).isEqualTo(Status.WAITING);
    }

    @Test
    @DisplayName("이미 지난 날짜 및 시간으로 예약하려는 경우 예외가 발생한다.")
    public void create_fail2() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate pastDate = LocalDate.of(2023, 8, 5);
        LocalDate currentDate = LocalDate.of(2025, 5, 11);

        timeManager.setFixed(currentDate);

        // when, then
        assertThatThrownBy(() -> reservationService.create("포비", pastDate, time.getId(), theme.getId()))
                .isInstanceOf(DomainException.class)
                .hasMessage(PAST_RESERVATION_NOT_ALLOWED.message());
    }


    @Test
    @DisplayName("확정 예약을 취소하면 같은 슬롯의 첫 번째 대기 예약이 확정된다.")
    void cancel_promoteWaitingReservation() {
        timeManager.setFixed(LocalDate.of(2023, 7, 6));

        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2023, 8, 10);

        Reservation confirmed = insertReservation("포비", date, time, theme, Status.CONFIRMED);
        Reservation waiting = insertReservation("브라운", date, time, theme, Status.WAITING);

        reservationService.cancel(confirmed.getId());

        assertThat(reservationRepository.findById(waiting.getId()).orElseThrow().getStatus())
                .isEqualTo(Status.CONFIRMED);
    }

    @Test
    @DisplayName("확정 예약의 날짜/시간을 변경하면 기존 슬롯의 첫 번째 대기 예약이 확정된다.")
    void editDateTime_promoteWaitingReservation() {
        timeManager.setFixed(LocalDate.of(2023, 7, 6));

        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationTime beforeTime = insertReservationTime(LocalTime.of(10, 0));
        ReservationTime afterTime = insertReservationTime(LocalTime.of(11, 0));

        LocalDate beforeDate = LocalDate.of(2023, 8, 10);
        LocalDate afterDate = LocalDate.of(2023, 8, 11);

        Reservation confirmed = insertReservation("포비", beforeDate, beforeTime, theme, Status.CONFIRMED);
        Reservation waiting = insertReservation("브라운", beforeDate, beforeTime, theme, Status.WAITING);

        reservationService.editDateTime(confirmed.getId(), afterDate, afterTime.getId(), confirmed.getGuestName());

        assertThat(reservationRepository.findById(waiting.getId()).orElseThrow().getStatus())
                .isEqualTo(Status.CONFIRMED);
    }

    @Test
    @DisplayName("해당 예약이 존재하지 않으면 취소할 수 없기 때문에 예외가 발생한다.")
    public void cancel_fail() {
        // given
        Long id = 1L;

        // when, then
        assertThatThrownBy(() -> reservationService.cancel(id))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_NOT_FOUND.message());
    }

    @Test
    @DisplayName("본인의 예약을 삭제한다.")
    public void cancelMine_success() {
        // given
        timeManager.setFixed(LocalDate.of(2023, 7, 6));

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Reservation reservation = insertWaitingReservation(LocalDate.of(2023, 8, 10), time, "브라운");

        // when
        reservationService.deleteMine(reservation.getId(), reservation.getGuestName());

        // then
        assertThat(reservationRepository.findById(reservation.getId()).get().getStatus()).isEqualTo(Status.CANCELED);
    }

    @Test
    @DisplayName("해당 예약이 존재하지 않으면 본인의 예약을 삭제할 수 없기 때문에 예외가 발생한다.")
    public void cancelMine_fail1() {
        // given
        Long id = 1L;

        // when, then
        assertThatThrownBy(() -> reservationService.deleteMine(id, "브라운"))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_NOT_FOUND.message());
    }

    @Test
    @DisplayName("이미 시작된 예약은 삭제할 수 없다.")
    public void cancelMine_fail2() {
        // given
        timeManager.setFixed(LocalDate.of(2023, 8, 11));

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Reservation reservation = insertWaitingReservation(LocalDate.of(2023, 8, 10), time, "브라운");

        // when, then
        assertThatThrownBy(() -> reservationService.deleteMine(reservation.getId(), reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_ALREADY_STARTED_RESERVATION.message());
    }

    @Test
    @DisplayName("본인의 예약이 아니면 삭제할 수 없기 때문에 예외가 발생한다.")
    public void cancelMine_fail3() {
        // given
        timeManager.setFixed(LocalDate.of(2023, 7, 6));

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Reservation reservation = insertWaitingReservation(LocalDate.of(2023, 8, 10), time, "브라운");

        // when, then
        assertThatThrownBy(() -> reservationService.deleteMine(reservation.getId(), "포비"))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_OTHER_GUEST_RESERVATION.message());
    }

    @Test
    @DisplayName("이미 취소된 예약은 삭제할 수 없다.")
    public void cancelMine_fail4() {
        // given
        timeManager.setFixed(LocalDate.of(2023, 7, 6));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Reservation reservation = insertReservation("브라운", LocalDate.of(2023, 8, 10), time, theme, Status.CANCELED);

        // when, then
        assertThatThrownBy(() -> reservationService.deleteMine(reservation.getId(), reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.message());
    }

    @Test
    @DisplayName("예약의 날짜 및 시간을 수정한다.")
    public void editDateTime_success() {
        // given
        ReservationTime existTime = insertReservationTime(LocalTime.of(10, 0));
        LocalDate existDate = LocalDate.of(2023, 8, 5);
        Reservation reservation = insertWaitingReservation(existDate, existTime, "브라운");

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(12, 0));

        timeManager.setFixed(LocalDate.of(2023, 7, 20));

        // when
        reservationService.editDateTime(reservation.getId(), editedDate, editedTime.getId(),
                reservation.getGuestName());

        // then
        ReservationWaitingResult reservationWaitingResult = reservationRepository.findWaitingById(reservation.getId())
                .get();

        assertThat(reservationWaitingResult)
                .extracting(ReservationWaitingResult::date, r -> r.time().getId())
                .containsExactly(editedDate, editedTime.getId());
    }

    @Test
    @DisplayName("수정하려는 예약이 존재하지 않으면 예외가 발생한다.")
    public void editDateTime_fail1() {
        // given
        Long reservationId = 1L;
        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(12, 0));

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservationId, editedDate, editedTime.getId(), "브라운"))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_NOT_FOUND.message());
    }

    @Test
    @DisplayName("수정하려는 예약 시간이 존재하지 않으면 예외가 발생한다.")
    public void editDateTime_fail2() {
        // given
        timeManager.setFixed(LocalDate.of(2023, 7, 20));

        ReservationTime existTime = insertReservationTime(LocalTime.of(10, 0));
        LocalDate existDate = LocalDate.of(2023, 8, 5);
        Reservation reservation = insertWaitingReservation(existDate, existTime, "브라운");

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        Long editedTimeId = 999L;

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), editedDate, editedTimeId,
                reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_TIME_NOT_FOUND.message());
    }

    @Test
    @DisplayName("이미 시작된 예약은 수정할 수 없다.")
    public void editDateTime_fail3() {
        // given
        timeManager.setFixed(LocalDate.of(2023, 8, 6));

        ReservationTime existTime = insertReservationTime(LocalTime.of(10, 0));
        LocalDate existDate = LocalDate.of(2023, 8, 5);
        Reservation reservation = insertWaitingReservation(existDate, existTime, "브라운");

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(12, 0));

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), editedDate, editedTime.getId(),
                reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_ALREADY_STARTED_RESERVATION.message());
    }

    @Test
    @DisplayName("수정하려는 날짜 및 시간에 예약이 존재하면 대기 상태가 된다.")
    public void editDateTime_change_waiting_status() {
        // given
        timeManager.setFixed(LocalDate.of(2023, 7, 6));

        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        LocalDate editedDate = LocalDate.of(2023, 8, 10);
        ReservationTime editedTime = insertReservationTime(LocalTime.of(10, 0));

        insertReservation("브라운", editedDate, editedTime, theme, Status.CONFIRMED);

        LocalDate existDate = LocalDate.of(2023, 8, 6);
        ReservationTime existTime = insertReservationTime(LocalTime.of(12, 0));
        Reservation reservation = insertReservation("포비", existDate, existTime, theme, Status.CONFIRMED);

        reservationService.editDateTime(reservation.getId(), editedDate, editedTime.getId(), "포비");

        // then
        assertThat(reservationRepository.findById(reservation.getId()).get().getStatus())
                .isEqualTo(Status.WAITING);
    }

    @Test
    @DisplayName("이미 취소된 예약은 수정할 수 없다.")
    public void editDateTime_fail4_2() {
        // given
        timeManager.setFixed(LocalDate.of(2023, 7, 6));

        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        LocalDate date = LocalDate.of(2023, 8, 10);
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));

        Reservation reservation = insertReservation("브라운", date, time, theme, Status.CANCELED);

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), date, time.getId(),
                reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(RESERVATION_ALREADY_CANCELED.message());
    }

    @ParameterizedTest
    @CsvSource({
            "2023-07-05, 10:00", // 날짜가 지난 경우
            "2023-07-06, 09:59", // 시간이 지난 경우
    })
    @DisplayName("이미 지난 날짜 및 시간으로 예약을 수정하려는 경우 예외가 발생한다.")
    public void editDateTime_fail5(LocalDate ed, LocalTime et) {
        // given
        timeManager.setFixed(LocalDateTime.of(2023, 7, 6, 10, 0));
        LocalDate existDate = LocalDate.of(2023, 8, 6);
        ReservationTime existTime = insertReservationTime(LocalTime.of(12, 0));
        Reservation reservation = insertWaitingReservation(existDate, existTime, "브라운");

        ReservationTime editedTime = insertReservationTime(et);

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(reservation.getId(), ed, editedTime.getId(),
                reservation.getGuestName()))
                .isInstanceOf(DomainException.class)
                .hasMessage(PAST_RESERVATION_NOT_ALLOWED.message());
    }

    @Test
    @DisplayName("본인의 예약이 아니면 예외가 발생한다.")
    public void editDateTime_fail6() {
        // given
        timeManager.setFixed(LocalDate.of(2023, 7, 6));

        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));

        Reservation reservation = insertReservation("브라운", LocalDate.of(2023, 8, 10), time, theme, Status.CONFIRMED);

        // when then
        assertThatThrownBy(() -> reservationService.editDateTime(
                reservation.getId(), reservation.getDate(), time.getId(), "other_guest"))
                .isInstanceOf(DomainException.class)
                .hasMessage(CANNOT_EDIT_OTHER_GUEST_RESERVATION.message());
    }

    // 1️⃣ findAllReservations() 테스트
    @Test
    @DisplayName("모든 예약을 페이지 단위로 조회한다.")
    void findAllReservations_success() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme, Status.WAITING);
        insertReservation("포비", LocalDate.of(2023, 8, 6), time, theme, Status.CONFIRMED);
        insertReservation("조이", LocalDate.of(2023, 8, 7), time, theme, Status.WAITING);

        // when
        List<Reservation> reservations = reservationService.findAllReservations(2, 1);

        // then
        assertThat(reservations).hasSize(1);
    }

    // 2️⃣ findByGuestName() 테스트
    @Test
    @DisplayName("예약자 이름으로 모든 예약을 조회한다.")
    void findByGuestName_success() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme, Status.CONFIRMED);
        insertReservation("브라운", LocalDate.of(2023, 8, 6), time, theme, Status.WAITING);
        insertReservation("포비", LocalDate.of(2023, 8, 7), time, theme, Status.CONFIRMED);

        // when
        List<ReservationWaitingResult> reservations = reservationService.findByGuestName("브라운");

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).allMatch(r -> r.guestName().equals("브라운"));
    }

    @Test
    @DisplayName("예약자 이름으로 취소되지 않은 예약만 조회한다.")
    void findByGuestNameExceptCanceled_success() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        insertReservation("브라운", LocalDate.of(2023, 8, 5), time, theme, Status.CONFIRMED);
        insertReservation("브라운", LocalDate.of(2023, 8, 6), time, theme, Status.CANCELED);
        insertReservation("브라운", LocalDate.of(2023, 8, 7), time, theme, Status.WAITING);

        // when
        List<ReservationWaitingResult> reservations = reservationService.findByGuestNameExceptCanceled("브라운");

        // then
        assertThat(reservations).hasSize(2)
                .extracting(ReservationWaitingResult::status)
                .doesNotContain(Status.CANCELED);
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약하려는 경우 예외가 발생한다.")
    void create_fail_theme_not_found() {
        // given
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 5, 11);
        Long invalidThemeId = 999L;

        timeManager.setFixed(LocalDate.of(2025, 5, 10));

        // when, then
        assertThatThrownBy(() -> reservationService.create("포비", date, time.getId(), invalidThemeId))
                .isInstanceOf(DomainException.class)
                .hasMessage(ThemeErrorCode.THEME_NOT_FOUND.message());
    }

    private void insertConfirmedReservation(LocalDate date, ReservationTime time, Theme theme,
                                            String guestName) {
        insertReservation(guestName, date, time, theme, Status.CONFIRMED);
    }

    private Reservation insertWaitingReservation(LocalDate existDate, ReservationTime existTime, String guestName) {
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        return insertReservation(guestName, existDate, existTime, theme, Status.WAITING);
    }

    private ReservationTime insertReservationTime(LocalTime startAt) {
        return reservationTimeRepository.save(ReservationTime.create(startAt));
    }

    private Theme insertTheme(String name, String description, String thumbnail) {
        return themeRepository.save(Theme.create(name, description, thumbnail));
    }

    private Reservation insertReservation(String name, LocalDate date, ReservationTime time, Theme theme,
                                          Status status) {
        return reservationRepository.save(Reservation.create(name, ReservationSlot.of(date, time, theme), status));
    }
}
