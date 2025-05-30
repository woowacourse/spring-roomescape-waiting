package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import roomescape.config.JpaConfig;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationitem.ReservationItem;
import roomescape.domain.reservationitem.ReservationItemRepository;
import roomescape.domain.reservationitem.ReservationTheme;
import roomescape.domain.reservationitem.ReservationThemeRepository;
import roomescape.domain.reservationitem.ReservationTime;
import roomescape.domain.reservationitem.ReservationTimeRepository;
import roomescape.repository.impl.MemberRepositoryImpl;
import roomescape.repository.impl.ReservationItemRepositoryImpl;
import roomescape.repository.impl.ReservationRepositoryImpl;
import roomescape.repository.impl.ReservationThemeRepositoryImpl;
import roomescape.repository.impl.ReservationTimeRepositoryImpl;
import roomescape.repository.jpa.MemberJpaRepository;
import roomescape.repository.jpa.ReservationItemJpaRepository;
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
    private ReservationItemRepository reservationItemRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;
    @Autowired
    private MemberJpaRepository memberJpaRepository;
    @Autowired
    private ReservationThemeJpaRepository reservationThemeJpaRepository;
    @Autowired
    private ReservationTimeJpaRepository reservationTimeJpaRepository;
    @Autowired
    private ReservationItemJpaRepository reservationItemJpaRepository;

    private Member member;
    private Member member2;
    private Member member3;
    private ReservationTime time;
    private ReservationTheme theme;
    private ReservationItem reservationItem;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservationRepository = new ReservationRepositoryImpl(reservationJpaRepository);
        memberRepository = new MemberRepositoryImpl(memberJpaRepository);
        reservationThemeRepository = new ReservationThemeRepositoryImpl(reservationThemeJpaRepository);
        reservationTimeRepository = new ReservationTimeRepositoryImpl(reservationTimeJpaRepository);
        reservationItemRepository = new ReservationItemRepositoryImpl(reservationItemJpaRepository);

        member = memberRepository.save(
                new Member("test@example.com", "testPassword", "test", MemberRole.USER)
        );
        member2 = memberRepository.save(
                new Member("test2@example.com", "testPassword2", "test2", MemberRole.USER)
        );
        member3 = memberRepository.save(
                new Member("test3@example.com", "testPassword3", "test3", MemberRole.USER)
        );
        time = reservationTimeRepository.save(
                new ReservationTime(LocalTime.now())
        );
        theme = reservationThemeRepository.save(
                new ReservationTheme("Theme", "Description", "Thumbnail")
        );
        reservationItem = reservationItemRepository.save(new ReservationItem(LocalDate.now().plusDays(1), time, theme));
        reservation = reservationRepository.save(
                new Reservation(member, reservationItem, ReservationStatus.ACCEPTED)
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
                () -> assertThat(reservations.getFirst().getReservationItem().getTime().getId()).isEqualTo(time.getId()),
                () -> assertThat(reservations.getFirst().getReservationItem().getTheme().getId()).isEqualTo(theme.getId())
        );
    }

    @Test
    @DisplayName("예약 대기 중 가장 빠른 대기를 조회한다.")
    void getFirstPendingReservationTest() {
        // given
        reservationRepository.save(new Reservation(member3, reservationItem, ReservationStatus.PENDING));
        reservationRepository.save(new Reservation(member2, reservationItem, ReservationStatus.PENDING));

        // when
        final Optional<Reservation> reservation = reservationRepository.findFirstByReservationItemAndReservationStatusOrderByIdAsc(
                reservationItem, ReservationStatus.PENDING
        );

        // then
        assertThat(reservation).isPresent();
        assertAll(
                () -> assertThat(reservation.get().getReservationStatus()).isEqualTo(ReservationStatus.PENDING),
                () -> assertThat(reservation.get().getMember()).isEqualTo(member3)
        );
    }

    @Test
    @DisplayName("예약 대기가 없다면 Optional이 비어있다.")
    void getNoPendingReservationTest() {
        // when
        final Optional<Reservation> reservation = reservationRepository.findFirstByReservationItemAndReservationStatusOrderByIdAsc(
                reservationItem, ReservationStatus.PENDING
        );

        // then
        assertThat(reservation).isEmpty();
    }
}
