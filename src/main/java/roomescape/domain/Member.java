package roomescape.domain;

public record Member(String name) {

    public Member {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("회원 이름은 비어 있을 수 없습니다.");
        }
    }
}
