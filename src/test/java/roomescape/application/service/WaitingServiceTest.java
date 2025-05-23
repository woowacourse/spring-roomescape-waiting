package roomescape.application.service;

import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.dto.LoginMember;
import roomescape.dto.request.WaitingRegisterDto;
import roomescape.infrastructure.db.MemberJpaRepository;
import roomescape.infrastructure.db.WaitingJpaRepository;
import roomescape.model.Member;
import roomescape.model.Role;

@SpringBootTest
public class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private WaitingJpaRepository waitingJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Test
    @DisplayName("정상적으로 Waiting 이 등록된다")
    void test1() {
        // given
        Member member = memberJpaRepository.save(new Member("이름", "email@gmail.com", "password", Role.ADMIN));
        LoginMember loginMember = new LoginMember(member);

        LocalDate date = LocalDate.now().plusDays(1);
        WaitingRegisterDto waitingRegisterDto = new WaitingRegisterDto(1L, 1L, date);

        // when
        waitingService.registerWaiting(loginMember, waitingRegisterDto);

        // then
        assertAll(
                () -> assertThat(waitingJpaRepository.findAll()).hasSize(1),
                () -> assertThat(waitingJpaRepository.findAll().getFirst().getId()).isEqualTo(1L)
        );
    }

    @Test
    @DisplayName("현재보다 이전의 날짜로 대기를 등록하는 경우 예외를 던진다")
    void test2() {
        // given
        Member member = memberJpaRepository.save(new Member("이름", "email@gmail.com", "password", Role.ADMIN));
        LoginMember loginMember = new LoginMember(member);

        LocalDate date = LocalDate.now().minusDays(1);
        WaitingRegisterDto waitingRegisterDto = new WaitingRegisterDto(1L, 1L, date);

        // when
        assertThatThrownBy(() -> waitingService.registerWaiting(loginMember, waitingRegisterDto))
                .isInstanceOf(IllegalStateException.class);
    }


}
