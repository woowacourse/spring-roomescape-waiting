package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import roomescape.DatabaseInitializer;
import roomescape.common.exception.RoomEscapeException;
import roomescape.repository.ReservationRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.repository.ReservationTimeRepository;
import roomescape.domain.ReservationTime;
import roomescape.dto.command.CreateReservationTimeCommand;
import roomescape.dto.response.CreateReservationTimeResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.repository.ThemeRepository;
import roomescape.domain.Theme;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ReservationTimeServiceTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 예약_시간을_추가한다() {
        // given
        CreateReservationTimeCommand command = new CreateReservationTimeCommand(LocalTime.of(10, 0));

        // when
        CreateReservationTimeResponse response = reservationTimeService.createReservationTime(command);

        // then
        assertThat(response.startAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 예약_시간_목록을_조회한다() {
        // given
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 5, 5);
        saveTime(10, 0);
        saveTime(11, 0);

        // when
        List<ReservationTimeResponse> responses = reservationTimeService.getReservationTimes(theme.getId(), date);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("isNotReserved").containsExactly(true, true);
    }

    @Test
    void 예약된_시간이면_isNotReserved가_false다() {
        // given
        ReservationTime time1 = saveTime(10, 0);
        ReservationTime time2 = saveTime(11, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        LocalDate date = LocalDate.of(2026, 5, 5);
        saveReservation("브라운", date, time1, theme);

        // when
        List<ReservationTimeResponse> responses = reservationTimeService.getReservationTimes(theme.getId(), date);

        // then
        assertThat(responses).extracting("isNotReserved").containsExactly(false, true);
    }

    @Test
    void 존재하지_않는_테마로_조회하면_404를_반환한다() {
        assertThatThrownBy(() -> reservationTimeService.getReservationTimes(999L, LocalDate.of(2026, 5, 5)))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void 예약_시간을_삭제한다() {
        // given
        ReservationTime saved = saveTime(10, 0);

        // when & then
        assertThatNoException().isThrownBy(() -> reservationTimeService.deleteReservationTime(saved.getId()));
    }

    @Test
    void 존재하지_않는_시간을_삭제하면_404를_반환한다() {
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(999L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void 예약에_존재하는_시간을_삭제하면_409를_반환한다() {
        // given
        ReservationTime time = saveTime(10, 0);
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        saveReservation("브라운", LocalDate.of(2026, 5, 5), time, theme);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(time.getId()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    private ReservationTime saveTime(int hour, int minute) {
        return reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeRepository.save(Theme.createWithoutId(name, description, thumbnail));
    }

    private void saveReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        reservationRepository.save(Reservation.createWithoutId(name, new ReservationSlot(date, time, theme)));
    }
}
