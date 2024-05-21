package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRanks;
import roomescape.domain.reservation.slot.ReservationSlot;

public record UserReservationResponse(
        long id,
        String theme,
        LocalDate date,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
        LocalTime time,
        String status
) {

        public static Stream<UserReservationResponse> reservationsToResponseStream(List<Reservation> reservation) {
                return reservation.stream()
                        .map(UserReservationResponse::createByReservation);
        }

        private static UserReservationResponse createByReservation(Reservation reservation) {
                ReservationSlot slot = reservation.getSlot();
                return new UserReservationResponse(
                        reservation.getId(),
                        slot.getTheme().getName(),
                        slot.getDate(),
                        slot.getTime().getStartAt(),
                        ReservationStatus.BOOKED.getValue()
                );
        }

        public static Stream<UserReservationResponse> waitingsToResponseStream(WaitingRanks waitingRanks) {
                Map<Waiting, Integer> waitingRankMap = waitingRanks.getWaitingRanks();

                return waitingRankMap.entrySet().stream()
                        .map(UserReservationResponse::createByWaiting);
        }

        private static UserReservationResponse createByWaiting(Entry<Waiting, Integer> waitingRank) {
                Waiting waiting = waitingRank.getKey();
                ReservationSlot slot = waiting.getReservation().getSlot();

                return new UserReservationResponse(
                        waiting.getId(),
                        slot.getTheme().getName(),
                        slot.getDate(),
                        slot.getTime().getStartAt(),
                        waitingRank.getValue() + "번째 " +ReservationStatus.WAIT.getValue()
                );
        }

        public LocalDateTime dateTime() {
                return LocalDateTime.of(date, time);
        }
}
