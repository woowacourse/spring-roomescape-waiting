package roomescape.controller.response;

import roomescape.model.Member;
import roomescape.model.Role;

public class MemberResponse {

    private Long id;
    private String name;
    private Role role;
    private String email;
    private String password;

    private MemberResponse() {
    }

    public MemberResponse(Member member) {
        this.id = member.getId();
        this.name = member.getName();
        this.role = member.getRole();
        this.email = member.getEmail();
        this.password = member.getPassword();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
