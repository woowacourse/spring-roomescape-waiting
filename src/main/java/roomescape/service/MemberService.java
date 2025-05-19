package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.request.MemberRequest;
import roomescape.dto.response.MemberResponse;
import roomescape.entity.Member;
import roomescape.exception.custom.DuplicatedException;
import roomescape.repository.jpa.JpaMemberRepository;

@Service
@Transactional
public class MemberService {

    private final JpaMemberRepository memberRepository;

    public MemberService(JpaMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAllMembers() {
        return memberRepository.findAll().stream()
            .map(MemberResponse::from)
            .toList();
    }

    public MemberResponse addMember(MemberRequest request) {
        validateDuplicateMember(request);

        return MemberResponse.from(
            memberRepository.save(
                Member.createUser(request.name(), request.email(), request.password())));
    }

    private void validateDuplicateMember(MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicatedException("member");
        }
    }
}
