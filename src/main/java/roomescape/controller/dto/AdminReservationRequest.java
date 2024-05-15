package roomescape.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import roomescape.controller.helper.validator.DateFormatConstraint;
import roomescape.service.dto.reservation.ReservationCreate;

public class AdminReservationRequest {

    @Email(message = "이메일 형식이 맞지 않습니다.")
    private final String email;

    @NotNull(message = "테마 아이디는 반드시 입력되어야 합니다.")
    @Positive(message = "테마 아이디는 자연수여야 합니다. ${validatedValue}은 사용할 수 없습니다.")
    private final Long themeId;

    @DateFormatConstraint
    private final String date;

    @NotNull(message = "예약 시간 아이디는 반드시 입력되어야 합니다.")
    @Positive(message = "예약 시간 아이디는 자연수여야 합니다. ${validatedValue}은 사용할 수 없습니다.")
    private final Long timeId;

    public AdminReservationRequest(String email, Long themeId, String date, Long timeId) {
        this.email = email;
        this.themeId = themeId;
        this.date = date;
        this.timeId = timeId;
    }

    public ReservationCreate toCreateReservation() {
        return new ReservationCreate(email, themeId, date, timeId);
    }

    public String getEmail() {
        return email;
    }

    public Long getThemeId() {
        return themeId;
    }

    public String getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }
}
