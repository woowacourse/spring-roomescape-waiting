package roomescape.domain;

import roomescape.exception.ReservationException;

import java.util.Objects;

public class Member {

    private final Long id;
    private final String name;
    private final MemberRole role;
    private final String email;
    private final String password;

    public Member(final Long id, final String name, final MemberRole role, final String email, final String password) {
        if (name.length() < 2 || name.length() > 10) {
            throw new ReservationException("예약자명은 2글자에서 10글자까지만 가능합니다.");
        }
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public MemberRole getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id) && Objects.equals(name, member.name) && Objects.equals(role, member.role) && Objects.equals(email, member.email) && Objects.equals(password, member.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, role, email, password);
    }
}
