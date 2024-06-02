package roomescape.reservation.dto;

import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.dto.ThemeResponse;
import roomescape.time.dto.ReservationTimeResponse;
import java.time.LocalDate;

public class WaitingResponse {

    private final Long id;
    private final MemberResponse member;
    private final LocalDate date;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final String status;
    private final Long sequence;

    public WaitingResponse(Long id, MemberResponse member, LocalDate date, ReservationTimeResponse time, ThemeResponse theme, String status, Long sequence) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.sequence = sequence;
    }

    public WaitingResponse(Reservation reservation, Long sequence) {
        this(reservation.getId(),
                MemberResponse.from(reservation.getMember()),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus().getStatusName(),
                sequence);
    }

    public static WaitingResponse of(Reservation reservation, Long sequence) {
        return new WaitingResponse(
                reservation.getId(),
                MemberResponse.from(reservation.getMember()),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus().getStatusName(),
                sequence);
    }

    public Long getId() {
        return id;
    }

    public MemberResponse getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public ThemeResponse getTheme() {
        return theme;
    }

    public String getStatus() {
        return status;
    }

    public Long getSequence() {
        return sequence;
    }
}
