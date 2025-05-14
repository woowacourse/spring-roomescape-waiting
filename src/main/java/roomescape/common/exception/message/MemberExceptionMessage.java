package roomescape.common.exception.message;

public enum MemberExceptionMessage {
    INVALID_MEMBER_EMAIL("해당 멤버 이메일은 존재하지 않습니다"),
    DUPLICATE_MEMBER("이미 존재하는 회원입니다");

    private final String message;

    MemberExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
