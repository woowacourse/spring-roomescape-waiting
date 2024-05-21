package roomescape.controller.response;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

import static roomescape.model.ReservationStatus.ACCEPT;
import static roomescape.model.ReservationStatus.PENDING;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.exception.NotFoundException;
import roomescape.model.Reservation;
import roomescape.model.ReservationStatus;

public class MemberReservationResponse {

    private Long id;
    private String theme;
    private LocalDate date;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;
    private String status;

    public MemberReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        this.theme = reservation.getTheme().getName();
        this.date = reservation.getDate();
        this.time = reservation.getTime().getStartAt();
        this.status = mapStatus(reservation.getStatus(), 0);
    }

    public MemberReservationResponse(Reservation reservation, int count) {
        this.id = reservation.getId();
        this.theme = reservation.getTheme().getName();
        this.date = reservation.getDate();
        this.time = reservation.getTime().getStartAt();
        this.status = mapStatus(reservation.getStatus(), count);
    }

    public static List<MemberReservationResponse> of(List<Reservation> reservations) {
        List<MemberReservationResponse> responses = new ArrayList<>(mapAcceptReservationToResponse(reservations));

        Map<String, List<Reservation>> waitingReservationGroup =
                filterReservationsByStatus(reservations, PENDING)
                        .stream().collect(groupingBy(MemberReservationResponse::reservationKey));

        for (List<Reservation> waitingReservations : waitingReservationGroup.values()) {
            int count = 1;
            waitingReservations.sort(comparing(Reservation::getCreatedAt));
            for (Reservation reservation : waitingReservations) {
                responses.add(new MemberReservationResponse(reservation, count++));
            }
        }
        responses.sort(comparing(MemberReservationResponse::getDate));
        return responses;
    }

    private static List<MemberReservationResponse> mapAcceptReservationToResponse(List<Reservation> reservations) {
        return filterReservationsByStatus(reservations, ACCEPT)
                .stream()
                .map(MemberReservationResponse::new)
                .toList();
    }

    private static List<Reservation> filterReservationsByStatus(List<Reservation> reservations,
                                                                ReservationStatus status) {
        return reservations.stream()
                .filter(reservation -> reservation.getStatus() == status)
                .toList();
    }

    private static String reservationKey(Reservation reservation) {
        return reservation.getTheme() + "-" + reservation.getTime() + "-" + reservation.getDate();
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

    public String mapStatus(ReservationStatus status, int order) {
        switch (status) {
            case ACCEPT -> {
                return "예약";
            }
            case PENDING -> {
                return "%s번째 예약대기".formatted(order);
            }
            default -> throw new NotFoundException("%s 상태를 표현하는 로직이 존재하지 않습니다.".formatted(status.name()));
        }
    }
}
