package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import roomescape.service.dto.ReservationSaveRequest;

public class UserReservationSaveRequest {
        @NotNull
        @FutureOrPresent(message = "지나간 날짜의 예약을 할 수 없습니다.")
        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate date;

        @NotNull
        @Positive
        private Long timeId;

        @NotNull
        @Positive
        private Long themeId;

        @NotNull
        private ReservationStatusRequest status = ReservationStatusRequest.RESERVED;

        protected UserReservationSaveRequest() {
        }

        public UserReservationSaveRequest(
                Long memberId,
                LocalDate date,
                Long timeId,
                Long themeId,
                ReservationStatusRequest status
        ) {
                this.date = date;
                this.timeId = timeId;
                this.themeId = themeId;
                this.status = status;
        }

        public ReservationSaveRequest toReservationSaveRequest(Long memberId) {
                return new ReservationSaveRequest(memberId, date, timeId, themeId, status);
        }

        public LocalDate getDate() {
                return date;
        }

        public Long getTimeId() {
                return timeId;
        }

        public Long getThemeId() {
                return themeId;
        }

        public ReservationStatusRequest getStatus() {
                return status;
        }
}
