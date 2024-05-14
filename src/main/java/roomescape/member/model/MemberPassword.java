package roomescape.member.model;

public record MemberPassword(String value) {

    public MemberPassword {
        validateValue(value);

    }

    private void validateValue(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("회원 비밀번호로 공백을 입력할 수 없습니다.");
        }
    }
}
