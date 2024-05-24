package roomescape.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import roomescape.repository.MemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/init-data.sql")
public class TransactionTest {

    @Autowired
    private final MemberRepository memberRepository;
    @Autowired
    private final MemberService memberService;

    @Autowired
    public TransactionTest(MemberRepository memberRepository, MemberService memberService) {
        this.memberRepository = memberRepository;
        this.memberService = memberService;
    }

    // ? repository의 메서드마다 entityManger가 새로 생성되는가? 아니면 클래스 단위로 entityManager가 있는 것인가?
    // ? 만약 repository의 메서드마다 entityManger가 존재한다면, 영속성 컨텍스트를 공유하는가?
    // ? 만약 repository의 클래스 단위로 entityManger가 존재하다면, 영속성 컨텍스트를 공유하지 않는가?

    // @Transactional 을 쓰지 않을 때
    // ? repository

    // @Transactional 을 쓸 때
}
