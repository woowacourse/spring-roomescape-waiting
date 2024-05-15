package roomescape.service.dto.reservation;

import java.time.LocalDate;
import roomescape.controller.dto.MemberReservationRequest;
import roomescape.controller.helper.LoginMember;

public class ReservationCreate {

    private final String email;
    private final long themeId;
    private final LocalDate date;
    private final long timeId;

    public ReservationCreate(String email, long themeId, String date, long timeId) {
        this.email = email;
        this.themeId = themeId;
        this.date = LocalDate.parse(date);
        this.timeId = timeId;
    }

    public ReservationCreate(LoginMember loginMember, MemberReservationRequest requestDto) {
        this(loginMember.getEmail(), requestDto.getThemeId(), requestDto.getDate(), requestDto.getTimeId());
    }

    public String getEmail() {
        return email;
    }

    public long getThemeId() {
        return themeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getTimeId() {
        return timeId;
    }
}
