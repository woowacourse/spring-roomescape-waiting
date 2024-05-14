package roomescape.domain;

public class Member {
    private final LoginMember loginMember;

    private final String email;
    private final String password;

    public Member(long id, Member member) {
        this(id, member.loginMember.getName(), member.getRole(), member.email, member.password);
    }

    public Member(Long id, String name, Role role, String email, String password) {
        this(new LoginMember(id, name, role), email, password);
    }

    public Member(LoginMember loginMember, String email, String password) {
        this.loginMember = loginMember;
        this.email = email;
        this.password = password;
    }

    public LoginMember getLoginMember() {
        return loginMember;
    }

    public Role getRole() {
        return loginMember.getRole();
    }

    public long getId() {
        return loginMember.getId();
    }

    public String getName() {
        return loginMember.getName();
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Member{" +
                "loginMember=" + loginMember +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
