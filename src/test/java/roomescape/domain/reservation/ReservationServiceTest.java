package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservation.dto.ReservationCreationRequest;
import roomescape.domain.reservation.dto.ReservationCreationResponse;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservation.dto.ReservationUpdateRequest;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.ReservationTimeErrorCode;
import roomescape.support.exception.RoomescapeException;

@SpringBootTest
@Sql("/truncate.sql")
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("예약을 생성한다.")
    void createReservation() {
        ReservationDate date = createDate(LocalDate.now().plusDays(1));
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("테마");

        ReservationCreationResponse response = reservationService.createReservation(
                new ReservationCreationRequest("테스터", date.getId(), time.getId(), theme.getId()));

        assertThat(response.name()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("과거 시간으로 예약 생성 시 예외가 발생한다.")
    void createReservationWithPastTime() {
        ReservationDate date = createDate(LocalDate.now().minusDays(1));
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("테마");

        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationCreationRequest("테스터", date.getId(), time.getId(), theme.getId())))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationTimeErrorCode.PAST_TIME_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("중복된 예약 생성 시 예외가 발생한다.")
    void createDuplicateReservation() {
        ReservationDate date = createDate(LocalDate.now().plusDays(1));
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("테마");
        reservationService.createReservation(
                new ReservationCreationRequest("테스터1", date.getId(), time.getId(), theme.getId()));

        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationCreationRequest("테스터2", date.getId(), time.getId(), theme.getId())))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationErrorCode.RESERVATION_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("이름으로 예약을 조회한다.")
    void getReservationsByName() {
        ReservationDate date = createDate(LocalDate.now().plusDays(1));
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("테마");
        reservationService.createReservation(
                new ReservationCreationRequest("테스터", date.getId(), time.getId(), theme.getId()));

        List<ReservationResponse> responses = reservationService.getReservationsByName("테스터");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("모든 예약을 조회한다.")
    void getAllReservations() {
        ReservationDate date1 = createDate(LocalDate.now().plusDays(1));
        ReservationDate date2 = createDate(LocalDate.now().plusDays(2));
        ReservationTime time1 = createTime(LocalTime.of(10, 0));
        ReservationTime time2 = createTime(LocalTime.of(11, 0));
        Theme theme = createTheme("테마");
        reservationService.createReservation(
                new ReservationCreationRequest("테스터1", date1.getId(), time1.getId(), theme.getId()));
        reservationService.createReservation(
                new ReservationCreationRequest("테스터2", date2.getId(), time2.getId(), theme.getId()));

        List<ReservationResponse> responses = reservationService.getAllReservations();

        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("예약을 취소한다.")
    void cancelReservation() {
        ReservationDate date = createDate(LocalDate.now().plusDays(1));
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("테마");
        ReservationCreationResponse response = reservationService.createReservation(
                new ReservationCreationRequest("테스터", date.getId(), time.getId(), theme.getId()));

        reservationService.cancelReservation(response.id());

        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("관리자가 예약을 삭제한다.")
    void deleteReservation() {
        ReservationDate date = createDate(LocalDate.now().plusDays(1));
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("테마");
        ReservationCreationResponse response = reservationService.createReservation(
                new ReservationCreationRequest("테스터", date.getId(), time.getId(), theme.getId()));

        reservationService.deleteReservation(response.id());

        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("당일 예약 취소 시 예외가 발생한다.")
    void cancelTodayReservation() {
        ReservationDate date = createDate(LocalDate.now());
        ReservationTime time = createTime(LocalTime.of(23, 59));
        Theme theme = createTheme("테마");
        Reservation reservation = reservationRepository.save(
                Reservation.createWithoutId("당일예약테스터", date, time, theme));

        assertThatThrownBy(() -> reservationService.cancelReservation(reservation.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationDateErrorCode.TODAY_NOT_MODIFIED.getMessage());
    }

    @Test
    @DisplayName("예약을 수정한다.")
    void updateReservation() {
        ReservationDate date = createDate(LocalDate.now().plusDays(1));
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("테마");
        ReservationCreationResponse created = reservationService.createReservation(
                new ReservationCreationRequest("테스터", date.getId(), time.getId(), theme.getId()));

        ReservationDate newDate = createDate(LocalDate.now().plusDays(2));
        ReservationTime newTime = createTime(LocalTime.of(14, 0));

        ReservationResponse updated = reservationService.updateReservation(
                created.id(), new ReservationUpdateRequest(newDate.getId(), newTime.getId()));

        assertThat(updated.date()).isEqualTo(newDate.getPlayDay());
        assertThat(updated.time().id()).isEqualTo(newTime.getId());
    }

    @Test
    @DisplayName("과거 시간으로 예약 수정 시 예외가 발생한다.")
    void updateReservationWithPastTime() {
        ReservationDate date = createDate(LocalDate.now().plusDays(1));
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("테마");
        ReservationCreationResponse created = reservationService.createReservation(
                new ReservationCreationRequest("테스터", date.getId(), time.getId(), theme.getId()));

        ReservationDate pastDate = createDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> reservationService.updateReservation(
                created.id(), new ReservationUpdateRequest(pastDate.getId(), time.getId())))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationTimeErrorCode.PAST_TIME_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("이미 존재하는 시간으로 예약 수정 시 예외가 발생한다.")
    void updateReservationToDuplicatedTime() {
        ReservationDate date = createDate(LocalDate.now().plusDays(1));
        ReservationTime time = createTime(LocalTime.of(10, 0));
        ReservationTime anotherTime = createTime(LocalTime.of(14, 0));
        Theme theme = createTheme("테마");
        ReservationCreationResponse myReservation = reservationService.createReservation(
                new ReservationCreationRequest("내예약", date.getId(), time.getId(), theme.getId()));
        reservationService.createReservation(
                new ReservationCreationRequest("다른사람예약", date.getId(), anotherTime.getId(), theme.getId()));

        assertThatThrownBy(() -> reservationService.updateReservation(
                myReservation.id(), new ReservationUpdateRequest(date.getId(), anotherTime.getId())))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationErrorCode.RESERVATION_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("당일 예약 수정 시 예외가 발생한다.")
    void updateTodayReservation() {
        ReservationDate date = createDate(LocalDate.now());
        ReservationTime time = createTime(LocalTime.of(23, 59));
        ReservationTime newTime = createTime(LocalTime.of(14, 0));
        Theme theme = createTheme("테마");
        Reservation reservation = reservationRepository.save(
                Reservation.createWithoutId("당일예약테스터", date, time, theme));

        assertThatThrownBy(() -> reservationService.updateReservation(
                reservation.getId(), new ReservationUpdateRequest(date.getId(), newTime.getId())))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining(ReservationDateErrorCode.TODAY_NOT_MODIFIED.getMessage());
    }

    private ReservationDate createDate(LocalDate playDay) {
        return reservationDateRepository.save(ReservationDate.createWithoutId(playDay));
    }

    private ReservationTime createTime(LocalTime startAt) {
        return reservationTimeRepository.save(ReservationTime.createWithoutId(startAt));
    }

    private Theme createTheme(String name) {
        return themeRepository.save(Theme.createWithoutId(name, "설명", "url"));
    }
}
