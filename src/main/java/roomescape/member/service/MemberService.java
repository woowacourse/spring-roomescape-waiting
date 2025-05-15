package roomescape.member.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.member.controller.request.SignUpRequest;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.role.Role;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member save(SignUpRequest request) {
        Email email = new Email(request.email());
        Name name = new Name(request.name());
        Password password = new Password(request.password());

        return memberRepository.save(new Member(name, email, password, Role.MEMBER));
    }

    public Member findById(Long id) {
        Optional<Member> member = memberRepository.findById(id);
        if (member.isPresent()) {
            return member.get();
        }
        throw new NoSuchElementException("[ERROR] 멤버가 존재하지 않습니다.");
    }

    public Member findByName(String name) {
        Optional<Member> member = memberRepository.findByName(new Name(name));
        if (member.isPresent()) {
            return member.get();
        }
        throw new NoSuchElementException("[ERROR] 멤버가 존재하지 않습니다.");
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }
}
