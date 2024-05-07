package roomescape.model.member;

public class LoginMember {

    private final long id;

    public LoginMember(long id) {
        validateRange(id);
        this.id = id;
    }

    private void validateRange(long id) {
        if (id <= 0) {
            throw new IllegalStateException("id는 0 이하일 수 없습니다.");
        }
    }

    public long getId() {
        return id;
    }
}
