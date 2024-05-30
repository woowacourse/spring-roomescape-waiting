package roomescape.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import roomescape.controller.request.MemberLoginRequest;
import roomescape.exception.AuthenticationException;
import roomescape.exception.NotFoundException;
import roomescape.model.Member;
import roomescape.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final Logger logger = LoggerFactory.getLogger(MemberService.class);

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    public Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("아이디에 해당하는 사용자가 존재하지 않습니다 : 사용자 아이디={}", id);
                    return new NotFoundException("사용자", id);
                });
    }

    public String findMemberNameById(Long id) {
        return findMemberById(id).getName();
    }

    public Member findMemberByEmailAndPassword(MemberLoginRequest request) {
        return memberRepository.findByEmailAndPassword(request.email(), request.password())
                .orElseThrow(() -> {
                    logger.error("아이디 또는 비밀번호가 일치하지 않습니다 : 입력 아이디={}, 입력 비밀번호={}",
                            request.email(), request.password());
                    return new AuthenticationException("아이디 또는 비밀번호가 일치하지 않습니다.");
                });
    }
}
