package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationRequestDTO;
import roomescape.dto.ReservationResponseDTO;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.exception.ReservationErrorCode;
import roomescape.exception.ReservationSlotErrorCode;
import roomescape.exception.ReservationTimeErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.exception.ThemeErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest
@Import(TestTimeConfig.class)
@Sql(scripts = "/empty.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    void 중복_예약이면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00"))
        );
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png")
        );

        ReservationRequestDTO request = new ReservationRequestDTO(
                "브라운",
                LocalDate.parse("2026-08-05"),
                time.getId(),
                theme.getId()
        );

        reservationService.addReservation(request);

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(request))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationErrorCode.RESERVATION_DUPLICATE);
    }

    @Test
    void 과거_예약이면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00"))
        );
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png")
        );

        ReservationRequestDTO request = new ReservationRequestDTO(
                "브라운",
                LocalDate.parse("2025-08-05"),
                time.getId(),
                theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(request))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationSlotErrorCode.SLOT_PAST_TIME);
    }

    @Test
    void 존재하지_않는_시간이면_예외가_발생한다() {
        // given
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png")
        );

        ReservationRequestDTO request = new ReservationRequestDTO(
                "브라운",
                LocalDate.parse("2026-08-05"),
                999L,
                theme.getId()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(request))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND);
    }

    @Test
    void 존재하지_않는_테마이면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00"))
        );

        ReservationRequestDTO request = new ReservationRequestDTO(
                "브라운",
                LocalDate.parse("2026-08-05"),
                time.getId(),
                999L
        );

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(request))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ThemeErrorCode.THEME_NOT_FOUND);
    }

    @Test
    void 중복_예약으로_예약을_변경하면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00"))
        );
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png")
        );

        ReservationRequestDTO request = new ReservationRequestDTO(
                "브라운",
                LocalDate.parse("2026-08-05"),
                time.getId(),
                theme.getId()
        );

        ReservationResponseDTO saved = reservationService.addReservation(request);
        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
                saved.date(), saved.time().id()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(saved.id(), updateRequest))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationErrorCode.RESERVATION_DUPLICATE);

    }

    @Test
    void 과거_시간으로_예약을_변경하면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00"))
        );
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png")
        );

        ReservationRequestDTO request = new ReservationRequestDTO(
                "브라운",
                LocalDate.parse("2026-08-05"),
                time.getId(),
                theme.getId()
        );

        ReservationResponseDTO saved = reservationService.addReservation(request);
        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest(
                saved.date().minusYears(1), saved.time().id()
        );

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservation(saved.id(), updateRequest))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationSlotErrorCode.SLOT_PAST_TIME);
    }

    @Test
    void 존재하지_않는_예약을_삭제하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> reservationService.deleteReservation(1L))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationErrorCode.RESERVATION_NOT_FOUND);
    }

    @Test
    void 과거_예약을_삭제하면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00"))
        );
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png")
        );

        Reservation pastReservation = Reservation.create(
                "브라운",
                ReservationSlot.of(LocalDate.parse("2025-08-05"), time, theme)
        );

        Reservation saved = reservationRepository.save(pastReservation);

        // when & then
        assertThatThrownBy(() -> reservationService.deleteReservation(saved.getId()))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationSlotErrorCode.SLOT_PAST_TIME);
    }

}
