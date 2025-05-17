package roomescape.member.dto.request;

public record SignupRequest(String email, String name, String password) {

    private static final String EMAIL_SIGN = "@";

    public SignupRequest {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 비어있을 수 없습니다.");
        }
        if (!email.contains(EMAIL_SIGN)) {
            throw new IllegalArgumentException("이메일의 형식에 맞게 작성해주세요.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 비어있을 수 없습니다.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 비어있을 수 없습니다.");
        }
    }
}
