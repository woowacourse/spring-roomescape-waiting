package roomescape.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record WaitingRequest(
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "날짜를 입력해 주세요.")
        LocalDate date,

        @NotNull(message = "시간을 선택해 주세요.")
        Long timeId,

        @NotNull(message = "테마를 선택해 주세요.")
        Long themeId,

        @NotBlank(message = "대기자 이름을 입력해 주세요.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z]{2,30}$",
                message = "닉네임은 2자 이상 30자 이하의 한글 또는 영문만 사용할 수 있습니다."
        )
        String name
) {
}
