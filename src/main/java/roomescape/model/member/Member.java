package roomescape.model.member;

import java.util.Objects;

public class Member {

    private final long id;
    private final Name name;
    private final Email email;
    private final Password password;
    private final Role role;

    public Member(long id, String name, String email, String password, Role role) {
        validateRange(id);
        this.id = id;
        this.name = new Name(name);
        this.email = new Email(email);
        this.password = new Password(password);
        this.role = role;
    }

    private void validateRange(long id) {
        if (id <= 0) {
            throw new IllegalStateException("id는 0 이하일 수 없습니다.");
        }
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name.getValue();
    }

    public String getEmail() {
        return email.getValue();
    }

    public String getPassword() {
        return password.getValue();
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return id == member.id
                && Objects.equals(name.getValue(), member.name.getValue())
                && Objects.equals(email.getValue(), member.email.getValue())
                && Objects.equals(password.getValue(), member.password.getValue())
                && role == member.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, password, role);
    }
}
