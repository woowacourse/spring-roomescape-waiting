package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.function.Function;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;

public record MemberReservationResponse(
        long id,
        LocalDate date,
        @JsonFormat(pattern = "kk:mm")
        LocalTime startAt,
        String themeName,
        String status
) {

    public static MemberReservationResponse of(final Reservation reservation, final int count) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getThemeNameValue(),
                StatusMapper.generateMessage(reservation.getStatus(), count)
        );
    }

    private enum StatusMapper {

        RESERVED(Status.RESERVATION, count -> "예약"),
        WAITING(Status.WAITING, count -> String.format("%d번째 예약대기", count)),
        ;

        private final Status status;
        private final Function<Integer, String> messageGenerator;

        StatusMapper(Status status, Function<Integer, String> messageGenerator) {
            this.status = status;
            this.messageGenerator = messageGenerator;
        }

        public static String generateMessage(final Status status, final int count) {
            return Arrays.stream(values())
                    .filter(mapper -> mapper.status == status)
                    .findAny()
                    .map(mapper -> mapper.messageGenerator.apply(count))
                    .orElseThrow();
        }
    }
}
