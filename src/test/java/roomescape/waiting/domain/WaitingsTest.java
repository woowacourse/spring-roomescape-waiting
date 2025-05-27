package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationSpecFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@ActiveProfiles("test")
@DataJpaTest
class WaitingsTest {

    @PersistenceContext
    EntityManager entityManager;

    @DisplayName("가장 높은 우선순위의 대기를 반환하고 제거한다")
    @Test
    void pollHighestPriority() {
        // given
        // 첫 번째 회원 생성
        Member member1 = MemberFixture.createMember("member1", "member1@test.com", "1234");
        // 두 번째 회원 생성
        Member member2 = MemberFixture.createMember("member2", "member2@test.com", "1234");

        // 오전 10시 예약 시간 생성
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        // 테마 생성
        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");

        // 예약 스펙 생성 (오늘 날짜, 예약 시간, 테마)
        ReservationSpec spec = ReservationSpecFixture.createSpec(LocalDate.now(), time, theme);

        // 첫 번째 회원의 대기 생성 및 생성 시간 설정
        Waiting waiting1 = new Waiting(member1, spec);
        waiting1.onCreate();

        // 두 번째 회원의 대기 생성 및 생성 시간 설정
        Waiting waiting2 = new Waiting(member2, spec);
        waiting2.onCreate();

        // 대기 목록 생성 (두 번째 대기가 먼저, 첫 번째 대기가 나중에 추가됨)
        Waitings waitings = new Waitings(Arrays.asList(waiting2, waiting1));

        // when
        Waiting polled = waitings.pollHighestPriority();

        // then
        assertThat(polled).isEqualTo(waiting1);
        assertThat(waitings.pollHighestPriority()).isEqualTo(waiting2);
        assertThat(waitings.pollHighestPriority()).isNull();
    }

    @DisplayName("특정 멤버의 대기가 대기열에 있는지 확인한다")
    @Test
    void containsMember() {
        // given
        // 첫 번째 회원 생성 및 저장
        Member member1 = MemberFixture.createMember("member1", "member1@test.com", "1234");
        // 두 번째 회원 생성 및 저장 (대기열에 포함되지 않음)
        Member member2 = MemberFixture.createMember("member2", "member2@test.com", "1234");
        entityManager.persist(member1);
        entityManager.persist(member2);

        // 오전 10시 예약 시간 생성
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        // 테마 생성
        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");

        // 예약 스펙 생성 (오늘 날짜, 예약 시간, 테마)
        ReservationSpec spec = ReservationSpecFixture.createSpec(LocalDate.now(), time, theme);

        // 첫 번째 회원의 대기 생성 및 생성 시간 설정
        Waiting waiting1 = new Waiting(member1, spec);
        waiting1.onCreate();

        // 대기 목록 생성 (첫 번째 회원의 대기만 포함)
        Waitings waitings = new Waitings(new ArrayList<>(List.of(waiting1)));

        // when & then
        assertThat(waitings.containsMember(member1)).isTrue();
        assertThat(waitings.containsMember(member2)).isFalse();
    }

    @DisplayName("getRankOf 메서드는 특정 대기의 순위를 반환한다")
    @Test
    void getRankOf() {
        // given
        // 첫 번째 회원 생성 및 저장
        Member member1 = MemberFixture.createMember("member1", "member1@test.com", "1234");
        // 두 번째 회원 생성 및 저장
        Member member2 = MemberFixture.createMember("member2", "member2@test.com", "1234");
        // 세 번째 회원 생성 및 저장
        Member member3 = MemberFixture.createMember("member3", "member3@test.com", "1234");
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);

        // 오전 10시 예약 시간 생성 및 저장
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);
        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        entityManager.persist(theme);

        // 예약 스펙 생성 (오늘 날짜, 예약 시간, 테마)
        ReservationSpec spec = ReservationSpecFixture.createSpec(LocalDate.now(), time, theme);

        // 첫 번째 회원의 대기 생성, 생성 시간 설정 및 저장
        Waiting waiting1 = new Waiting(member1, spec);
        waiting1.onCreate();
        entityManager.persist(waiting1);
        // 두 번째 회원의 대기 생성, 생성 시간 설정 및 저장
        Waiting waiting2 = new Waiting(member2, spec);
        waiting2.onCreate();
        entityManager.persist(waiting2);
        // 세 번째 회원의 대기 생성, 생성 시간 설정 및 저장
        Waiting waiting3 = new Waiting(member3, spec);
        waiting3.onCreate();
        entityManager.persist(waiting3);

        // 대기 목록 생성 (세 번째, 첫 번째, 두 번째 대기 순서로 추가)
        Waitings waitings = new Waitings(Arrays.asList(waiting3, waiting1, waiting2));

        // when & then
        assertThat(waitings.getRankOf(waiting1)).isEqualTo(1L);
        assertThat(waitings.getRankOf(waiting2)).isEqualTo(2L);
        assertThat(waitings.getRankOf(waiting3)).isEqualTo(3L);
    }
}
