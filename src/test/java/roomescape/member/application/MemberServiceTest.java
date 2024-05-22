package roomescape.member.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.ServiceTest;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.ViolationException;
import roomescape.member.domain.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.TEST_PASSWORD;
import static roomescape.TestFixture.TOMMY_NAME;
import static roomescape.TestFixture.USER_MIA;
import static roomescape.member.domain.Role.USER;

class MemberServiceTest extends ServiceTest {
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("사용자가 가입한다.")
    void create() {
        // given
        Member member = USER_MIA();

        // when
        Member createdMember = memberService.create(member);

        // then
        assertThat(createdMember.getId()).isNotNull();
    }

    @Test
    @DisplayName("Id로 조회하려는 사용자가 존재하지 않는 경우 예외가 발생한다.")
    void findById() {
        // given
        Long notExistingId = 10L;

        // when & then
        assertThatThrownBy(() -> memberService.findById(notExistingId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("중복된 이메일로 가입할 수 없다.")
    void createWithDuplicatedEmail() {
        // given
        Member mia = memberService.create(USER_MIA());
        Member duplicatedEmailMember = new Member(TOMMY_NAME, mia.getEmail().getValue(), TEST_PASSWORD, USER);

        // when & then
        assertThatThrownBy(() -> memberService.create(duplicatedEmailMember))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("동시 요청으로 중복된 이메일로 가입할 수 없다.")
    void createWithDuplicatedEmailInMultiThread() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService service = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            Future<?> future = service.submit(() -> {
                try {
                    memberService.create(USER_MIA());
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        // then
        assertThatThrownBy(() -> {
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof ViolationException) {
                        throw new ViolationException("중복 예약 예외가 발생하였습니다.");
                    }
                }
            }
        }).isInstanceOf(ViolationException.class);
        latch.await();
        service.shutdown();
        assertThat(memberService.findAll()).hasSize(1);
    }
}
