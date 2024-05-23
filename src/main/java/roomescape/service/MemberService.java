package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.request.MemberLoginRequest;
import roomescape.controller.request.RegisterRequest;
import roomescape.exception.AuthenticationException;
import roomescape.exception.NotFoundException;
import roomescape.model.Member;
import roomescape.model.Role;
import roomescape.repository.MemberRepository;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public Member findMemberByEmailAndPassword(MemberLoginRequest request) {
        return memberRepository.findByEmailAndPassword(request.email(), request.password())
                .orElseThrow(() -> new AuthenticationException(
                        "사용자(email: %s, password: %s)가 존재하지 않습니다.".formatted(request.email(), request.password())));
    }

    @Transactional(readOnly = true)
    public String findMemberNameById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id가 %s인 사용자가 존재하지 않습니다.".formatted(id)));
        return member.getName();
    }

    @Transactional(readOnly = true)
    public Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id가 %s인 사용자가 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    @Transactional
    public Member register(RegisterRequest request) {
        return memberRepository.save(new Member(request.name(), Role.MEMBER, request.email(), request.password()));
    }
}
