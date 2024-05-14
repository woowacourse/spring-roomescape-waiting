package roomescape.member.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.member.controller.dto.request.SignupRequest;
import roomescape.member.controller.dto.response.MemberResponse;
import roomescape.member.controller.dto.response.SignupResponse;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberDao;

@Service
public class MemberService {
    private final MemberDao memberDao;

    public MemberService(final MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Member findByEmail(final String email) {
        return memberDao.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException(
                        "[ERROR] (email : " + email + ") 에 대한 사용자가 존재하지 않습니다.")
                );
    }

    public SignupResponse save(final SignupRequest signupRequest) {
        Member member = memberDao.save(signupRequest.toEntity());
        return new SignupResponse(member.getNameValue(), member.getEmail(), member.getPassword());
    }


    public List<MemberResponse> getAll() {
        return memberDao.getAll()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }
}
