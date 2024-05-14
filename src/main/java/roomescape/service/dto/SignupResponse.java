package roomescape.service.dto;

import roomescape.domain.Member;
import roomescape.domain.MemberRole;

public class SignupResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final MemberRole role;

    public SignupResponse(Long id, String name, String email, MemberRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public SignupResponse(Member member) {
        this(member.getId(), member.getName(), member.getEmail(), member.getRole());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public MemberRole getRole() {
        return role;
    }
}
