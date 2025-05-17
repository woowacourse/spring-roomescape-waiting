package roomescape.fixture.db;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.domain.PasswordEncryptor;
import roomescape.member.repository.MemberRepository;

@Component
@RequiredArgsConstructor
public class MemberDbFixture {

    private final MemberRepository memberRepository;
    private final PasswordEncryptor passwordEncryptor;
    public static final String RAW_PASSWORD = "1234";

    public Member 유저1_생성() {
        Password password = Password.encrypt(RAW_PASSWORD, passwordEncryptor);
        Member member = Member.signUpUser("유저1", "user1@email.com", password);
        return memberRepository.save(member);
    }

    public Member 유저2_생성() {
        Password password = Password.encrypt(RAW_PASSWORD, passwordEncryptor);
        Member member = Member.signUpUser("유저2", "user2@email.com", password);
        return memberRepository.save(member);
    }

    public Member 관리자_생성() {
        Password password = Password.encrypt(RAW_PASSWORD, passwordEncryptor);
        Member member = Member.signUpAdmin("관리자", "admin@email.com", password);
        return memberRepository.save(member);
    }
}

