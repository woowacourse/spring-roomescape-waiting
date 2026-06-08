package roomescape.reservation.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.validator.ReservationValidator;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.dto.ReservationDetail;

@ExtendWith(MockitoExtension.class)
class ReservationValidatorTest {

    @Mock
    private ReservationRepository reservationRepository;

    private ReservationValidator reservationValidator;

    @BeforeEach
    void setUp() {
        reservationValidator = new ReservationValidator(reservationRepository);
    }

    @DisplayName("현재보다 이전 일정으로 예약할 수 없습니다.")
    @Test
    void validate_create_date_time() {
        assertThatThrownBy(() -> reservationValidator.validateCreateDateTime(
                LocalDate.of(2026, 5, 6),
                LocalTime.of(10, 0),
                LocalDateTime.of(2026, 5, 6, 11, 0)
        ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("현재 시간보다 이전 시간으로 예약을 할 수 없습니다.");
    }

    @DisplayName("예약자가 아닌 사용자는 예약을 변경할 수 없습니다.")
    @Test
    void validate_update_owner() {
        Reservation reservation = createReservation();
        ReservationDetail reservationDetail = createReservationDetail();
        ReservationUpdateCommand request = new ReservationUpdateCommand(
                reservation.getId(),
                "카야",
                LocalDate.of(2026, 5, 7),
                2L
        );

        assertThatThrownBy(() -> reservationValidator.validateModification(
                request.name(),
                reservation,
                reservationDetail,
                LocalDateTime.of(2026, 5, 1, 0, 0)
        ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("본인의 예약만 변경하거나 취소할 수 있습니다.");
    }

    @DisplayName("이미 예약이 존재하는 일정으로 변경할 수 없습니다.")
    @Test
    void validate_duplicate_update() {
        Reservation reservation = createReservation();
        ReservationDetail reservationDetail = createReservationDetail();
        ReservationUpdateCommand request = new ReservationUpdateCommand(
                reservation.getId(),
                reservation.getName(),
                LocalDate.of(2026, 5, 7),
                2L
        );
        given(reservationRepository.existsByDateAndThemeAndTimeExcludingId(
                request.date(),
                reservation.getThemeId(),
                request.timeId(),
                reservation.getId()
        )).willReturn(true);

        assertThatThrownBy(() -> reservationValidator.validateUpdateSchedule(
                request,
                reservation,
                LocalTime.of(11, 0),
                LocalDateTime.of(2026, 5, 1, 0, 0)
        ))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 해당 날짜와 시간에 예약이 존재합니다.");
    }

    @DisplayName("이미 같은 일정에 예약한 사용자는 대기를 신청할 수 없습니다.")
    @Test
    void validate_duplicate_waiting_request() {
        ReservationCreateCommand request = new ReservationCreateCommand(
                "스타크",
                LocalDate.of(2026, 5, 6),
                1L,
                1L
        );
        given(reservationRepository.existsByNameAndDateAndThemeAndTime(
                request.name(),
                request.date(),
                request.themeId(),
                request.timeId()
        )).willReturn(true);

        assertThatThrownBy(() -> reservationValidator.validateWaitingRequest(request))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 해당 날짜와 시간에 예약이 존재합니다.");
    }

    private Reservation createReservation() {
        return Reservation.builder()
                .id(1L)
                .name("스타크")
                .date(LocalDate.of(2026, 5, 6))
                .themeId(1L)
                .timeId(1L)
                .build();
    }

    private ReservationDetail createReservationDetail() {
        return new ReservationDetail(
                1L,
                "스타크",
                LocalDate.of(2026, 5, 6),
                1L,
                "theme",
                "description",
                "thumbnail",
                1L,
                LocalTime.of(10, 0)
        );
    }
}
