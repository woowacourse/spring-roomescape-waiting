package roomescape.unit.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.dto.request.ReservationCondition;
import roomescape.infrastructure.JdbcMemberRepository;
import roomescape.infrastructure.JdbcReservationRepository;
import roomescape.infrastructure.JdbcReservationTimeRepository;
import roomescape.infrastructure.JdbcThemeRepository;

@JdbcTest
class JdbcReservationRepositoryTest {

    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;

    @Autowired
    public JdbcReservationRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.reservationRepository = new JdbcReservationRepository(jdbcTemplate);
        this.reservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
        this.themeRepository = new JdbcThemeRepository(jdbcTemplate);
        this.memberRepository = new JdbcMemberRepository(jdbcTemplate);
    }

    @Test
    void 예약_시간을_추가할_수_있다() {
        // given
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        Theme theme = new Theme(null, "themeName", "des", "th");
        Theme savedTheme = themeRepository.save(theme);
        Member member = new Member(null, "name1", "email1@domain.com", "password1", Role.MEMBER);
        Member savedMember = memberRepository.save(member);
        Reservation reservation = Reservation.createWithoutId(
                savedMember,
                LocalDate.now().plusDays(1),
                savedReservationTime,
                savedTheme
        );

        // when
        reservationRepository.save(reservation);

        // then
        List<Reservation> reservationDaoAll = reservationRepository.findAll();
        assertThat(reservationDaoAll.size()).isEqualTo(1);
    }

    @Test
    void 예약_시간을_조회할_수_있다() {
        // given
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        Theme theme = new Theme(null, "themeName", "des", "th");
        Theme savedTheme = themeRepository.save(theme);
        Member member = new Member(null, "포라", "email1@domain.com", "password1", Role.MEMBER);
        Member savedMember = memberRepository.save(member);
        Reservation reservation = Reservation.createWithoutId(
                savedMember,
                LocalDate.now().plusDays(1),
                savedReservationTime,
                savedTheme
        );
        reservationRepository.save(reservation);

        // when
        List<Reservation> reservationDaoAll = reservationRepository.findAll();

        // then
        assertThat(reservationDaoAll.getFirst().getMember().getName()).isEqualTo("포라");
        assertThat(reservationDaoAll.getFirst().getDate()).isEqualTo(LocalDate.now().plusDays(1));
    }

    @Test
    void 예약_시간을_삭제할_수_있다() {
        // given
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        Theme theme = new Theme(null, "themeName", "des", "th");
        Theme savedTheme = themeRepository.save(theme);
        Member member = new Member(null, "name1", "email1@domain.com", "password1", Role.MEMBER);
        Member savedMember = memberRepository.save(member);
        Reservation reservation = Reservation.createWithoutId(
                savedMember,
                LocalDate.now().plusDays(1),
                savedReservationTime,
                savedTheme
        );
        reservationRepository.save(reservation);
        int beforeSize = reservationRepository.findAll().size();

        // when
        reservationRepository.deleteById(savedReservationTime.getId());
        int afterSize = reservationRepository.findAll().size();

        // then
        assertThat(beforeSize).isEqualTo(1);
        assertThat(afterSize).isEqualTo(0);
    }

    @Test
    void timeId로_예약을_조회한다() {
        // given
        ReservationTime savedReservationTime = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = new Theme(null, "themeName", "des", "th");
        Theme savedTheme = themeRepository.save(theme);
        Member member = new Member(null, "포라", "email1@domain.com", "password1", Role.MEMBER);
        Member savedMember = memberRepository.save(member);
        Reservation savedReservation = reservationRepository.save(Reservation.createWithoutId(
                savedMember,
                LocalDate.now().plusDays(1),
                savedReservationTime,
                savedTheme
        ));
        // when
        List<Reservation> foundReservation = reservationRepository.findByTimeId(savedReservationTime.getId());
        // then
        assertThat(foundReservation.getFirst().getMember().getName()).isEqualTo("포라");
    }

    @Test
    void id로_예약을_조회한다() {
        // given
        ReservationTime reservationTime = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        Theme theme = new Theme(null, "themeName", "des", "th");
        Theme savedTheme = themeRepository.save(theme);
        Member member = new Member(null, "포라", "email1@domain.com", "password1", Role.MEMBER);
        Member savedMember = memberRepository.save(member);
        Reservation savedReservation = reservationRepository.save(Reservation.createWithoutId(
                savedMember,
                LocalDate.now().plusDays(1),
                savedReservationTime,
                savedTheme
        ));
        // when
        Optional<Reservation> foundReservation = reservationRepository.findById(savedReservation.getId());
        // then
        assertThat(foundReservation.isPresent()).isTrue();
        assertThat(foundReservation.get().getMember().getName()).isEqualTo("포라");
    }

    @Test
    void 날짜와_시간과_테마로_예약을_조회한다() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(9, 0))
        );
        Theme saveTheme = themeRepository.save(
                Theme.createWithoutId("theme1", "desc", "thumb1")
        );
        Member member = memberRepository.save(new Member(null, "name1", "email1@domain.com", "pass1", Role.MEMBER));
        Reservation savedReservation = reservationRepository.save(
                Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), savedTime, saveTheme)
        );
        // when
        Optional<Reservation> foundReservation = reservationRepository.findByDateTimeAndTheme(
                LocalDate.of(2025, 1, 1), savedTime, saveTheme);
        // then
        assertThat(foundReservation).isPresent();
        assertThat(foundReservation.get().getId()).isEqualTo(savedReservation.getId());
    }

    @Test
    void 테마id_회원id_날짜범위로_예약을_조회한다() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(9, 0))
        );
        Theme saveTheme = themeRepository.save(
                Theme.createWithoutId("theme1", "desc", "thumb1")
        );
        Member savedMember = memberRepository.save(
                new Member(null, "name1", "email1@domain.com", "pass1", Role.MEMBER));
        Reservation savedReservation = reservationRepository.save(
                Reservation.createWithoutId(savedMember, LocalDate.of(2025, 1, 1), savedTime, saveTheme)
        );
        ReservationCondition condition = new ReservationCondition(
                Optional.ofNullable(saveTheme.getId()),
                Optional.ofNullable(savedMember.getId()),
                Optional.of(LocalDate.of(2025, 1, 1)),
                Optional.of(LocalDate.of(2025, 1, 1))
        );
        // when
        List<Reservation> foundReservation = reservationRepository.findByCondition(condition);
        // then
        assertThat(foundReservation).hasSize(1);
    }

    @Test
    void 테마id로_예약을_조회한다() {
        // given
        ReservationTime savedReservationTime = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.createWithoutId("themeName", "des", "th"));
        Member member = new Member(null, "포라", "email1@domain.com", "password1", Role.MEMBER);
        Member savedMember = memberRepository.save(member);
        reservationRepository.save(Reservation.createWithoutId(
                savedMember,
                LocalDate.now().plusDays(1),
                savedReservationTime,
                theme
        ));
        // when
        List<Reservation> foundReservation = reservationRepository.findByThemeId(theme.getId());
        // then
        assertThat(foundReservation.getFirst().getMember().getName()).isEqualTo("포라");
    }
}
