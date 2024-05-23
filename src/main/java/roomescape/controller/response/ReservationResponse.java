package roomescape.controller.response;

import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Waiting;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public class ReservationResponse {

    private final long id;
    private final LocalDate date;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final MemberResponse member;

    private ReservationResponse(long id, LocalDate date, ReservationTimeResponse time, ThemeResponse theme, MemberResponse member) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public static ReservationResponse from(Reservation reservation) {
        ReservationTime time = reservation.getTime();
        Theme theme = reservation.getTheme();
        Member member = reservation.getMember();
        return new ReservationResponse(reservation.getId(), reservation.getDate(),
                ReservationTimeResponse.from(time),
                ThemeResponse.from(theme),
                MemberResponse.from(member));
    }

    private static ReservationResponse from(Waiting waiting) {
        ReservationTime time = waiting.getTime();
        Theme theme = waiting.getTheme();
        Member member = waiting.getMember();
        return new ReservationResponse(waiting.getId(), waiting.getDate(),
                ReservationTimeResponse.from(time),
                ThemeResponse.from(theme),
                MemberResponse.from(member));
    }

    public static List<ReservationResponse> from(List<Reservation> reservations, List<Waiting> waiting) {
        List<ReservationResponse> reservationResponses = reservations.stream()
                .map(ReservationResponse::from)
                .toList();
        List<ReservationResponse> waitingResponses = waiting.stream()
                .map(ReservationResponse::from)
                .toList();
        return Stream.concat(reservationResponses.stream(), waitingResponses.stream()).toList();
    }

    public long getId() {
        return id;
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

    public MemberResponse getMember() {
        return member;
    }
}
