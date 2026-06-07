package roomescape.exception.custom;

public class WaitIsFullException extends CustomException {

    public WaitIsFullException() {
        super("해당 슬롯에 대기 인원이 마감되었습니다.");
    }
}
