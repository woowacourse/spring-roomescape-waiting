package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.exception.EmailException;
import roomescape.member.exception.MemberNotFound;
import roomescape.member.presentation.dto.MemberRequest;
import roomescape.member.presentation.dto.MemberResponse;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MemberResponse save(final MemberRequest request) {
        boolean emailExist = memberRepository.existsByEmail(new Email(request.email()));
        validateEmailExists(emailExist);

        Member member = memberRepository.save(
            Member.createWithoutId(request.name(), request.email(), request.password()));

        return new MemberResponse(member.getId(), member.getName());
    }

    private static void validateEmailExists(boolean emailExist) {
        if (emailExist) {
            throw new EmailException("중복되는 이메일입니다.");
        }
    }

    public Member findByEmail(final String email) {
        return memberRepository.findByEmail(new Email(email))
            .orElseThrow(() -> new MemberNotFound("멤버를 찾을 수 없습니다."));
    }

    public boolean isExistsByEmail(final String email) {
        return memberRepository.existsByEmail(new Email(email));
    }

    public Member findById(final Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new MemberNotFound("멤버를 찾을 수 없습니다."));
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(member -> new MemberResponse(member.getId(), member.getName()))
                .toList();
    }
}
