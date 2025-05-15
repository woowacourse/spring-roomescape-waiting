package roomescape.exception;

public enum SecurityErrorCode {

    AUTHORITY_LACK("권한이 존재하지 않습니다.", "권한이 부족합니다."),
    AUTHORITY_NOT_EXIST("권한이 존재하지 않습니다.", "권한이 존재하지 않습니다."),
    INVALID_EMAIL("로그인에 실패하였습니다.", "이메일이 잘못되었습니다."),
    INVALID_PASSWORD("로그인에 실패하였습니다.", "비밀번호가 잘못되었습니다."),
    TOKEN_EXPIRED("다시 로그인해주세요.", "만료된 인증 토큰입니다."),
    TOKEN_INVALID("로그인해주세요.", "잘못된 인증 토큰입니다."),
    TOKEN_NOT_EXIST("로그인해주세요.", "인증 토큰이 존재하지 않습니다."),
    ;

    private final String clientMessage;
    private final String detailMessage;

    SecurityErrorCode(final String clientMessage, final String detailMessage) {
        this.clientMessage = clientMessage;
        this.detailMessage = detailMessage;
    }

    public String clientMessage() {
        return clientMessage;
    }

    public String detailMessage() {
        return detailMessage;
    }
}
