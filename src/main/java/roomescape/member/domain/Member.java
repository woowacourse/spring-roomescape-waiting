package roomescape.member.domain;

import roomescape.member.role.MemberRole;
import roomescape.name.domain.Name;

public class Member {

    private static final String DEFAULT_NAME = "어드민";

    private long id;
    private final Name name;
    private final Email email;
    private final Password password;
    private final MemberRole memberRole;

    private Member(long id, String name, Email email, Password password, MemberRole memberRole) {
        this.id = id;
        this.name = new Name(name);
        this.email = email;
        this.password = password;
        this.memberRole = memberRole;
    }

    private Member(String name, Email email, Password password, MemberRole role) {
        this(0, name, email, password, role);
    }

    public static Member saveMemberFrom(long id) {
        return new Member(id, DEFAULT_NAME, Email.saveEmailFrom(null), Password.savePasswordFrom(null),
                MemberRole.MEMBER);
    }

    public static Member memberOf(long id, String name, String email, String password, String role) {
        return new Member(id, name, Email.emailFrom(email), Password.passwordFrom(password),
                MemberRole.findMemberRole(role));
    }

    public static Member saveMemberOf(String email, String password, String name) {
        return new Member(DEFAULT_NAME, Email.emailFrom(email), Password.passwordFrom(password), MemberRole.MEMBER);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getEmail() {
        return email.getEmail();
    }

    public String getPassword() {
        return password.getPassword();
    }
}
