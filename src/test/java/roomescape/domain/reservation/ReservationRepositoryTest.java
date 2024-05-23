package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.dto.WaitingWithRankDto;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("예약 시간 아이디로 예약이 존재하는지 확인한다.")
    void existsByTimeId() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));
        Reservation reservation = reservationRepository.save(
                new Reservation(LocalDate.of(2024, 5, 5), member, reservationTime, theme, ReservationStatus.RESERVED));

        boolean exists = reservationRepository.existsByTimeId(reservation.getTime().getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("테마 아이디로 예약이 존재하는지 확인한다.")
    void existsByThemeId() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));
        Reservation reservation = reservationRepository.save(
                new Reservation(LocalDate.of(2024, 5, 5), member, reservationTime, theme, ReservationStatus.RESERVED));

        boolean exists = reservationRepository.existsByThemeId(reservation.getTheme().getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("날짜, 시간 아이디, 테마 아이디, 예약 상태로 예약이 존재하는지 확인한다.")
    void existsByDateAndTimeIdAndThemeIdAndStatus() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));
        Reservation reservation = reservationRepository.save(
                new Reservation(LocalDate.of(2024, 5, 5), member, reservationTime, theme, ReservationStatus.RESERVED));

        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeIdAndStatus(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getStatus()
        );

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("날짜, 시간 아이디, 테마 아이디, 회원 아이디, 예약 상태로 예약이 존재하는지 확인한다.")
    void existsByDateAndTimeIdAndThemeIdAndMemberIdAndStatus() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));
        Reservation reservation = reservationRepository.save(
                new Reservation(LocalDate.of(2024, 5, 5), member, reservationTime, theme, ReservationStatus.RESERVED));

        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberIdAndStatus(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getMember().getId(),
                reservation.getStatus()
        );

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("아이디와 예약 상태로 예약을 찾는다.")
    void findByIdAndStatus() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));
        Reservation reservation = reservationRepository.save(
                new Reservation(LocalDate.of(2024, 5, 5), member, reservationTime, theme, ReservationStatus.RESERVED));

        Optional<Reservation> foundReservation = reservationRepository.findByIdAndStatus(
                reservation.getId(),
                reservation.getStatus()
        );

        assertThat(foundReservation).isPresent();
    }

    @Test
    @DisplayName("회원 아이디, 테마 아이디, 시작 날짜, 종료 날짜로 예약들을 조회한다.")
    void findAllByConditions() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));
        reservationRepository.save(
                new Reservation(LocalDate.of(2024, 5, 5), member, reservationTime, theme, ReservationStatus.RESERVED));

        List<Reservation> reservations = reservationRepository.findAllByConditions(
                member.getId(),
                theme.getId(),
                LocalDate.of(2024, 5, 5),
                LocalDate.of(2024, 5, 5)
        );

        assertThat(reservations).hasSize(1);
    }

    @Test
    @Sql("/waitings.sql")
    @DisplayName("회원 아이디로 예약 대기 순번을 포함한 예약 대기들을 조회한다.")
    void findReservationWithRanksByMemberId() {
        List<WaitingWithRankDto> reservationWithRanks = reservationRepository
                .findWaitingsWithRankByMemberId(4L);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservationWithRanks).hasSize(2);

            softly.assertThat(reservationWithRanks.get(0).reservation().getId()).isEqualTo(4L);
            softly.assertThat(reservationWithRanks.get(0).rank()).isEqualTo(3);

            softly.assertThat(reservationWithRanks.get(1).reservation().getId()).isEqualTo(9L);
            softly.assertThat(reservationWithRanks.get(1).rank()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("예약 상태에 맞는 예약들을 조회한다.")
    void findAllByStatus() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));
        reservationRepository.save(
                new Reservation(LocalDate.of(2024, 5, 5), member, reservationTime, theme, ReservationStatus.RESERVED));

        List<Reservation> reservations = reservationRepository.findAllByStatus(ReservationStatus.RESERVED);

        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("아아디로 예약을 조회한다.")
    void getById() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));
        Reservation savedReservation = reservationRepository.save(
                new Reservation(LocalDate.of(2024, 5, 5), member, reservationTime, theme, ReservationStatus.RESERVED));

        Reservation reservation = reservationRepository.getById(savedReservation.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservation.getId()).isNotNull();
            softly.assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2024, 5, 5));
            softly.assertThat(reservation.getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
            softly.assertThat(reservation.getMember().getEmail()).isEqualTo("ex@gmail.com");
            softly.assertThat(reservation.getTheme().getName()).isEqualTo("테마");
            softly.assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        });
    }

    @Test
    @DisplayName("아이디로 예약을 조회하고, 없을 경우 예외를 발생시킨다.")
    void getByIdWhenNotExist() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));
        reservationRepository.save(
                new Reservation(LocalDate.of(2024, 5, 5), member, reservationTime, theme, ReservationStatus.RESERVED));

        assertThatThrownBy(() -> reservationRepository.getById(-1L))
                .isInstanceOf(DomainNotFoundException.class)
                .hasMessage(String.format("해당 id의 예약이 존재하지 않습니다. (id: %d)", -1L));
    }

    @Test
    @DisplayName("아이디와 예약 상태로 예약을 조회한다.")
    void getByIdAndStatus() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));
        Reservation savedReservation = reservationRepository.save(
                new Reservation(LocalDate.of(2024, 5, 5), member, reservationTime, theme, ReservationStatus.RESERVED));

        Reservation reservation = reservationRepository.getByIdAndStatus(
                savedReservation.getId(),
                ReservationStatus.RESERVED
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservation.getId()).isNotNull();
            softly.assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2024, 5, 5));
            softly.assertThat(reservation.getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
            softly.assertThat(reservation.getMember().getEmail()).isEqualTo("ex@gmail.com");
            softly.assertThat(reservation.getTheme().getName()).isEqualTo("테마");
            softly.assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        });
    }

    @Test
    @DisplayName("아이디와 예약 상태로 예약을 조회하고, 없을 경우 예외를 발생시킨다.")
    void getByIdAndStatusWhenNotExist() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));
        reservationRepository.save(
                new Reservation(LocalDate.of(2024, 5, 5), member, reservationTime, theme, ReservationStatus.RESERVED));

        assertThatThrownBy(() -> reservationRepository.getByIdAndStatus(-1L, ReservationStatus.RESERVED))
                .isInstanceOf(DomainNotFoundException.class)
                .hasMessage("해당 id와 예약 상태의 예약이 존재하지 않습니다. (id: %d, status: %s)", -1L,
                        ReservationStatus.RESERVED);
    }
}
