package roomescape.controller.response;

import roomescape.domain.ReservationStatus;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.WaitingWithRankResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public record MemberReservationResponse(Long id,
                                        String theme,
                                        LocalDate date,
                                        LocalTime time,
                                        String status) {

    public static MemberReservationResponse from(ReservationResult reservationResult) {
        return new MemberReservationResponse(
                reservationResult.id(),
                reservationResult.theme().name(),
                reservationResult.date(),
                reservationResult.time().startAt(),
                ReservationStatus.RESERVED.getName());
    }

    public static MemberReservationResponse from(WaitingWithRankResult waitingWithRankResult) {
        return new MemberReservationResponse(
                waitingWithRankResult.id(),
                waitingWithRankResult.theme().name(),
                waitingWithRankResult.date(),
                waitingWithRankResult.time().startAt(),
                waitingWithRankResult.rank() + "번째 " + ReservationStatus.WAITING.getName());
    }

    public static List<MemberReservationResponse> from(
            List<ReservationResult> reservationResults, List<WaitingWithRankResult> waitingWithRankResult) {
        ArrayList<MemberReservationResponse> results = new ArrayList<>();

        List<MemberReservationResponse> reservations = reservationResults.stream()
                .map(MemberReservationResponse::from)
                .toList();
        List<MemberReservationResponse> waitings = waitingWithRankResult.stream()
                .map(MemberReservationResponse::from)
                .toList();

        results.addAll(reservations);
        results.addAll(waitings);
        results.sort(Comparator.comparing(MemberReservationResponse::date).thenComparing(MemberReservationResponse::time));
        return Collections.unmodifiableList(results);
    }
}
