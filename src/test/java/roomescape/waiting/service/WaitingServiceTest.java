package roomescape.waiting.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.auth.service.dto.LoginMember;
import roomescape.common.exception.ForbiddenException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.service.dto.request.CreateWaitingRequest;
import roomescape.waiting.service.dto.response.CreateWaitingResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DataJpaTest(properties = "spring.sql.init.mode=never")
@Import(WaitingService.class)
class WaitingServiceTest {

    private final Member admin = new Member("admin", "test@test.com", "12341234", Role.ADMIN);
    private final Member other = new Member("other", "test@test.com", "12341234", Role.MEMBER);
    private final Member me = new Member("me", "test@test.com", "12341234", Role.MEMBER);
    private final ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
    private final Theme theme = new Theme("title", "desc", "thumbnail");
    private final Waiting waiting = new Waiting(LocalDate.now().plusDays(1), time, theme, me, LocalDateTime.now());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private WaitingService waitingService;

    @BeforeEach
    void setup() {
        entityManager.persist(admin);
        entityManager.persist(other);
        entityManager.persist(me);
        entityManager.persist(time);
        entityManager.persist(theme);
        entityManager.persist(waiting);
    }

    @AfterEach
    void reset() {
        entityManager.clear();
    }

    @DisplayName("자신이 생성한 대기 예약을 삭제할 수 있다.")
    @Test
    void deleteOnlyMyWaiting() {
        // when & then
        assertThatCode(() -> {
            waitingService.delete(waiting.getId(), LoginMember.of(me));
        }).doesNotThrowAnyException();
    }

    @DisplayName("자신이 생성하지 않은 대기 예약은 삭제할 수 없다.")
    @Test
    void disableToDeleteOtherWaiting() {
        // when & then
        assertThatThrownBy(() -> {
            waitingService.delete(waiting.getId(), LoginMember.of(other));
        }).isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("어드민은 다른 회원의 대기 예약을 삭제할 수 있다.")
    @Test
    void deleteOtherWaitingByAdmin() {
        // when & then
        assertThatCode(() -> {
            waitingService.delete(waiting.getId(), LoginMember.of(admin));
        }).doesNotThrowAnyException();
    }

    @DisplayName("일반 회원은 대기 예약을 생성할 수 있다.")
    @Test
    void createWaitingByMember() {
        // given
        CreateWaitingRequest request = new CreateWaitingRequest(
                LocalDate.now().plusDays(1),
                theme.getId(),
                time.getId()
        );
        LoginMember loginMember = LoginMember.of(me);

        // when
        CreateWaitingResponse response = waitingService.createWaiting(request, loginMember);


        // then
        assertSoftly(softly -> {
            softly.assertThat(response.id()).isNotNull();
            softly.assertThat(entityManager.find(Waiting.class, response.id())).isNotNull();
        });
    }

    @DisplayName("어드민은 대기 예약을 생성할 수 있다.")
    @Test
    void createWaitingByAdmin() {
        // given
        CreateWaitingRequest request = new CreateWaitingRequest(
                LocalDate.now().plusDays(1),
                theme.getId(),
                time.getId()
        );
        LoginMember loginMember = LoginMember.of(admin);

        // when
        CreateWaitingResponse response = waitingService.createWaiting(request, loginMember);


        // then
        assertSoftly(softly -> {
            softly.assertThat(response.id()).isNotNull();
            softly.assertThat(entityManager.find(Waiting.class, response.id())).isNotNull();
        });
    }
}
