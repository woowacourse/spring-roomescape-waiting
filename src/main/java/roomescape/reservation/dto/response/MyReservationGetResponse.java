package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.member.dto.response.MemberGetResponse;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;

import java.time.LocalDate;

public record MyReservationGetResponse(Long id,
                                       MemberGetResponse member,
                                       @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                       ReservationTime time,
                                       Theme theme,
                                       String status) {
}
