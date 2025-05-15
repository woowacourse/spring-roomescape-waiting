package roomescape.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record ReservationCreateRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") @NotNull(message = "������ ��¥�� �Էµ��� �ʾҴ�.") LocalDate date,
        @NotNull(message = "������ �ð��� �Էµ��� �ʾҴ�.") Long timeId,
        @NotNull(message = "������ �׸��� �Էµ��� �ʾҴ�.") Long themeId) {

}
