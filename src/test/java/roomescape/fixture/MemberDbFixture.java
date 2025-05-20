package roomescape.fixture;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.domain.PasswordEncryptor;
import roomescape.member.repository.MemberRepository;

@Component
@RequiredArgsConstructor
public class MemberDbFixture {

    public static final String RAW_PASSWORD = "1234";
    
    private final MemberRepository memberRepository;
    private final PasswordEncryptor passwordEncryptor;

    public Member 유저1_생성() {
        Email email = Email.create("user1@email.com");
        Password password = Password.encrypt(RAW_PASSWORD, passwordEncryptor);
        Member member = Member.signUpUser("유저1", email, password);
        return memberRepository.save(member);
    }

    public Member 유저2_생성() {
        Email email = Email.create("user2@email.com");
        Password password = Password.encrypt(RAW_PASSWORD, passwordEncryptor);
        Member member = Member.signUpUser("유저2", email, password);
        return memberRepository.save(member);
    }

    public Member 관리자_생성() {
        Email email = Email.create("admin@email.com");
        Password password = Password.encrypt(RAW_PASSWORD, passwordEncryptor);
        Member member = Member.signUpAdmin("관리자", email, password);
        return memberRepository.save(member);
    }
}

