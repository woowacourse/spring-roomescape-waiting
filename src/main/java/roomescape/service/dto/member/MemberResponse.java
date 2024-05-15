package roomescape.service.dto.member;

import roomescape.controller.helper.LoginMember;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;

public class MemberResponse {

    private final String email;
    private final String name;

    public MemberResponse(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public MemberResponse(Member member) {
        this(member.getEmail(), member.getName());
    }

    public MemberResponse(LoginMember member) {
        this(member.getEmail(), member.getName());
    }

    public MemberResponse(Reservation reservation) {
        this(reservation.memberEmail(), reservation.memberName());
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
