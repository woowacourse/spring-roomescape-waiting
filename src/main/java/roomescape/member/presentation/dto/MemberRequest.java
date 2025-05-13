package roomescape.member.presentation.dto;

public record MemberRequest(String email, String password, String name) {
    public MemberRequest {
        if (email == null) {
            throw new IllegalStateException("email은 null 일 수 없습니다.");
        }

        if (password == null) {
            throw new IllegalStateException("password는 null 일 수 없습니다.");
        }

        if (name == null) {
            throw new IllegalStateException("name은 null 일 수 없습니다.");
        }
    }
}
