package roomescape.unit.repository.waiting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.repository.member.JpaMemberRepository;
import roomescape.repository.reservationtime.JpaReservationTimeRepository;
import roomescape.repository.theme.JpaThemeRepository;
import roomescape.repository.waiting.JpaWaitingRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
class JpaWaitingRepositoryTest {

    @Autowired
    private JpaWaitingRepository jpaWaitingRepository;
    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;
    @Autowired
    private JpaThemeRepository jpaThemeRepository;
    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Test
    void 대기_순번과_함께_예약_엔티티를_불러올_수_있다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(1)));
        Theme theme1 = jpaThemeRepository.save(new Theme(null, "theme", "description", "thumbnail"));
        Theme theme2 = jpaThemeRepository.save(new Theme(null, "theme", "description", "thumbnail"));
        Member member1 = jpaMemberRepository.save(new Member(null, "username1", "password", "name1", Role.USER));
        Member member2 = jpaMemberRepository.save(new Member(null, "username2", "password", "name2", Role.USER));
        Member member3 = jpaMemberRepository.save(new Member(null, "username3", "password", "name3", Role.USER));
        Member finder = jpaMemberRepository.save(new Member(null, "username4", "password", "name4", Role.USER));
        jpaWaitingRepository.save(new Waiting(null, date, time, theme1, member1));
        jpaWaitingRepository.save(new Waiting(null, date, time, theme1, member2));
        Waiting saved1 = jpaWaitingRepository.save(new Waiting(null, date, time, theme1, finder));
        jpaWaitingRepository.save(new Waiting(null, date, time, theme1, member3));
        jpaWaitingRepository.save(new Waiting(null, date, time, theme2, member1));
        Waiting saved2 = jpaWaitingRepository.save(new Waiting(null, date, time, theme2, finder));
        jpaWaitingRepository.save(new Waiting(null, date, time, theme2, member2));
        jpaWaitingRepository.save(new Waiting(null, date, time, theme2, member3));

        // When
        List<WaitingWithRank> actual = jpaWaitingRepository.findWaitingsWithRankByMemberId(finder.getId());

        // Then
        assertThat(actual).containsExactlyInAnyOrder(new WaitingWithRank(saved1, 3), new WaitingWithRank(saved2, 2));
    }

    @Test
    void 날짜와_시간_테마에_해당하는_예약대기가_존재하는지_확인할_수_있다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(1)));
        Theme theme = jpaThemeRepository.save(new Theme(null, "theme", "description", "thumbnail"));
        Member member = jpaMemberRepository.save(new Member(null, "username", "password", "name", Role.USER));
        jpaWaitingRepository.save(new Waiting(null, date, time, theme, member));

        // When & Then
        assertThat(jpaWaitingRepository.existsByDateAndTimeAndThemeAndMember(date, time, theme, member)).isTrue();
    }

    @Test
    void 날짜와_시간_테마에_해당하는_예약대기가_없다면_false를_반환해야_한다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(1)));
        Theme theme = jpaThemeRepository.save(new Theme(null, "theme", "description", "thumbnail"));
        Member member = jpaMemberRepository.save(new Member(null, "username", "password", "name", Role.USER));
        jpaWaitingRepository.save(new Waiting(null, date, time, theme, member));

        // When & Then
        assertAll(() -> {
            assertThat(jpaWaitingRepository.existsByDateAndTimeAndThemeAndMember(null, time, theme, member)).isFalse();
            assertThat(jpaWaitingRepository.existsByDateAndTimeAndThemeAndMember(date, null, theme, member)).isFalse();
            assertThat(jpaWaitingRepository.existsByDateAndTimeAndThemeAndMember(date, time, null, member)).isFalse();
            assertThat(jpaWaitingRepository.existsByDateAndTimeAndThemeAndMember(date, time, theme, null)).isFalse();
        });
    }
}
