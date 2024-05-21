package roomescape.controller.response;

import static java.util.Comparator.comparing;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.service.dto.MemberReservation;

public class MemberReservationResponse {

    private Long id;
    private String theme;
    private LocalDate date;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;
    private String status;

    public MemberReservationResponse(MemberReservation reservation) {
        this.id = reservation.id();
        this.theme = reservation.theme();
        this.date = reservation.date();
        this.time = reservation.time();
        this.status = mapStatus(reservation.order());
    }

    public static List<MemberReservationResponse> of(List<MemberReservation> memberReservations) {
        return memberReservations.stream()
                .map(MemberReservationResponse::new)
                .sorted(comparing(MemberReservationResponse::getDate))
                .toList();
    }


    public Long getId() {
        return id;
    }

    public String getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public String mapStatus(Long order) {
        if (order == 0L) {
            return "예약";
        }
        return "%s번째 예약대기".formatted(order);
    }
}
