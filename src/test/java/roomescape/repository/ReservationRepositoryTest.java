package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import roomescape.config.JpaConfig;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationThemeRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.repository.impl.MemberRepositoryImpl;
import roomescape.repository.impl.ReservationRepositoryImpl;
import roomescape.repository.impl.ReservationThemeRepositoryImpl;
import roomescape.repository.impl.ReservationTimeRepositoryImpl;
import roomescape.repository.jpa.MemberJpaRepository;
import roomescape.repository.jpa.ReservationJpaRepository;
import roomescape.repository.jpa.ReservationThemeJpaRepository;
import roomescape.repository.jpa.ReservationTimeJpaRepository;

@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(JpaConfig.class)
@DataJpaTest
public class ReservationRepositoryTest {

    private ReservationRepository reservationRepository;

    private MemberRepository memberRepository;
    private ReservationThemeRepository reservationThemeRepository;
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;
    @Autowired
    private MemberJpaRepository memberJpaRepository;
    @Autowired
    private ReservationThemeJpaRepository reservationThemeJpaRepository;
    @Autowired
    private ReservationTimeJpaRepository reservationTimeJpaRepository;

    private Member member;
    private ReservationTime time;
    private ReservationTheme theme;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservationRepository = new ReservationRepositoryImpl(reservationJpaRepository);
        memberRepository = new MemberRepositoryImpl(memberJpaRepository);
        reservationThemeRepository = new ReservationThemeRepositoryImpl(reservationThemeJpaRepository);
        reservationTimeRepository = new ReservationTimeRepositoryImpl(reservationTimeJpaRepository);

        member = memberRepository.save(
                new Member("test@example.com", "testPassword", "test", MemberRole.USER)
        );
        time = reservationTimeRepository.save(
                new ReservationTime(LocalTime.now())
        );
        theme = reservationThemeRepository.save(
                new ReservationTheme("Theme", "Description", "Thumbnail")
        );
        reservation = reservationRepository.saveWithMember(
                new Reservation(member, LocalDate.now().plusDays(1), time, theme)
        );
    }

    @Test
    @DisplayName("날짜와 시간 테마가 같은 예약이 있는지 확인한다.")
    void existsByDateAndTimeIdAndThemeIdTest() {
        // given
        Long nonExistTimeId = 999L;
        Long nonExistThemeId = 999L;
        LocalDate nonExistDate = LocalDate.now().plusDays(2);

        // when
        final boolean exist = reservationRepository.existByDateAndTimeIdAndThemeId(
                reservation.getDate(), time.getId(), theme.getId()
        );
        final boolean nonExist1 = reservationRepository.existByDateAndTimeIdAndThemeId(
                reservation.getDate(), nonExistTimeId, theme.getId()
        );
        final boolean nonExist2 = reservationRepository.existByDateAndTimeIdAndThemeId(
                reservation.getDate(), time.getId(), nonExistThemeId
        );
        final boolean nonExist3 = reservationRepository.existByDateAndTimeIdAndThemeId(
                nonExistDate, time.getId(), theme.getId()
        );

        // then
        assertAll(
                () -> assertThat(exist).isTrue(),
                () -> assertThat(nonExist1).isFalse(),
                () -> assertThat(nonExist2).isFalse(),
                () -> assertThat(nonExist3).isFalse()
        );
    }

    @Test
    @DisplayName("사용자 아이디를 통해 해당 사용자의 예약 내역들을 조회한다")
    void findReservationsByMemberIdTest() {
        // given
        Long memberId = member.getId();

        // when
        final List<Reservation> reservations = reservationRepository.findByMemberId(memberId);

        // then
        assertAll(
                () -> assertThat(reservations).hasSize(1),
                () -> assertThat(reservations.getFirst().getTime().getId()).isEqualTo(time.getId()),
                () -> assertThat(reservations.getFirst().getTheme().getId()).isEqualTo(theme.getId())
        );
    }
}
