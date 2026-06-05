package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.repository.JdbcReservationSlotRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationSlotRepository;
import roomescape.reservation.service.validator.ReservationValidator;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.JdbcReservationTimeRepository;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.test_config.MutableClock;
import roomescape.test_config.TestClockConfig;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static roomescape.reservation.domain.Status.*;

@JdbcTest
@Import({
        TestClockConfig.class,
        ReservationService.class,
        JdbcReservationRepository.class,
        JdbcReservationSlotRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcThemeRepository.class,
        ReservationValidator.class,
        ReservationCreator.class
})
@Sql(value = "/acceptance-cleanup.sql", executionPhase = BEFORE_TEST_METHOD)
public class ReservationServiceTxTest {
    @Autowired ReservationService reservationService;
    @MockitoSpyBean ReservationRepository reservationRepository;
    @Autowired ReservationSlotRepository reservationSlotRepository;
    @Autowired ReservationTimeRepository reservationTimeRepository;
    @Autowired ThemeRepository themeRepository;
    @Autowired MutableClock clock;


    @Test
    @DisplayName("예약 날짜/시간을 수정할 때 승격이 실패하면 예약 수정도 이루어지지 않는다.")
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // @JdbcTest에 @Transactional이 있기 때문에 서비스 내부에서 롤백이 되지 않는다. 롤백이 되게 하기 위해 추가
    public void editDateTime_fail_when_promotion_fail() {
        // given
        stubtoPromoteFail();
        clock.setFixed(LocalDate.of(2023, 7, 6));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2023, 8, 10);

        Reservation reservation = insertReservation("브라운", date, time, theme, CONFIRMED);
        insertReservation("포비", date, time, theme, WAITING);


        // when // then
        assertThatThrownBy(() -> reservationService.editDateTime(
                reservation.getId(), LocalDate.of(2023, 8, 12), time.getId(), "브라운"));

        Reservation notEdited = reservationRepository.findById(reservation.getId()).get();
        assertThat(notEdited.getReservationSlot().getDate())
                .isEqualTo(date);
    }

    @Test
    @DisplayName("예약을 취소할 때 승격이 실패하면 예약 취소도 이루어지지 않는다.")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void cancel_fail_when_promote_fail() {
        // given
        stubtoPromoteFail();
        clock.setFixed(LocalDate.of(2023, 8, 6));
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2023, 8, 10);

        Reservation cancel = insertReservation("브라운", date, time, theme, CONFIRMED);
        insertReservation("포비", date, time, theme, WAITING);

        // when then
        assertThatThrownBy(() -> reservationService.cancel(cancel.getId()))
                .isInstanceOf(RuntimeException.class);

        Reservation notCanceled = reservationRepository.findById(cancel.getId()).get();
        assertThat(notCanceled.getStatus()).isNotEqualTo(CANCELED);
    }

    @Test
    @DisplayName("본인의 예약을 취소할 때 승격이 실패하면 본인의 예약도 취소되지 않는다.")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void cancelMine_fail_when_promote_fail() {
        // given
        clock.setFixed(LocalDate.of(2023, 7, 6));
        stubtoPromoteFail();
        Theme theme = insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2023, 8, 10);

        Reservation cancel = insertReservation("브라운", date, time, theme, CONFIRMED);
        Reservation waiting1 = insertReservation("포비", date, time, theme, WAITING);

        // when then
        assertThatThrownBy(() -> reservationService.cancelMine(cancel.getId(), cancel.getGuestName()))
                .isInstanceOf(RuntimeException.class);
        Reservation notCanceled = reservationRepository.findById(cancel.getId()).get();
        assertThat(notCanceled.getStatus()).isNotEqualTo(CANCELED);
    }

    private void stubtoPromoteFail() {
        doThrow(new RuntimeException())
                .when(reservationRepository)
                .updateStatus(any(), any());
    }

    private ReservationTime insertReservationTime(LocalTime startAt) {
        return reservationTimeRepository.save(ReservationTime.create(startAt));
    }

    private Theme insertTheme(String name, String description, String thumbnail) {
        return themeRepository.save(Theme.create(name, description, thumbnail));
    }

    private Reservation insertReservation(String name, LocalDate date, ReservationTime time, Theme theme, Status status) {
        ReservationSlot reservationSlot = reservationSlotRepository.upsert(ReservationSlot.create(date, time, theme));
        return reservationRepository.save(Reservation.create(name, reservationSlot, status, LocalDateTime.now(clock)));
    }
}
