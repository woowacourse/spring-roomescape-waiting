package roomescape.member.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.domain.PasswordEncoder;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.exception.EmailAlreadyExistsException;

@Service
@AllArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    public MemberResponse add(final MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        Member member = new Member(
                new Name(request.name()),
                new Email(request.email()),
                new Password(request.password(), passwordEncoder)
        );

        return MemberResponse.from(memberRepository.save(member));
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }
}
