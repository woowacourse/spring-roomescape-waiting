package roomescape.web.controller.request;

import jakarta.validation.constraints.NotBlank;

public record ReservationTimeWebRequest(@NotBlank(message = "잘못된 시간 형식을 입력하셨습니다.") String startAt) {

}
