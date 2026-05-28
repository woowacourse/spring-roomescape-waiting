package roomescape.reservationWaiting.exception;

public enum ReservationWaitingErrorCode {
    ALREADY_RESERVED("이미 본인이 예약 완료한 타임 슬롯입니다. 마이페이지에서 예약 내역을 확인해 주세요."),
    DUPLICATE_WAITING("이미 해당 타임스롯에 대기를 신청하셨습니다. 다른 타임스롯의 대기를 이용해 주세요."),
    WAITING_NOT_FOUND("요청하신 예약 대기 내역을 찾을 수 없습니다. 대기 신청 상태를 다시 확인해 주세요."),
    TARGET_RESERVATION_NOT_FOUND("대기 신청하려는 예약 정보가 올바르지 않거나 이미 취소되었습니다. 예약 현황판을 새로고침해 주세요.");

    private final String message;

    ReservationWaitingErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
