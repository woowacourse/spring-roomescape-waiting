package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;

class ReservationServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("예약들을 조회한다.")
    void getReservations() {
        Member member = memberRepository.save(new Member("new@gmail.com", "password", "nickname", Role.USER));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 30)));

        Reservation reservation = new Reservation(
                LocalDate.of(2024, 4, 9),
                member,
                time,
                theme,
                ReservationStatus.RESERVED
        );
        reservationRepository.save(reservation);

        List<ReservationResponse> responses = reservationService.getReservationsByConditions(
                member.getId(),
                theme.getId(),
                LocalDate.of(2024, 4, 9),
                LocalDate.of(2024, 4, 9)
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(1);
            softly.assertThat(responses.get(0).date()).isEqualTo("2024-04-09");
            softly.assertThat(responses.get(0).member().id()).isEqualTo(member.getId());
            softly.assertThat(responses.get(0).member().email()).isEqualTo("new@gmail.com");
            softly.assertThat(responses.get(0).member().name()).isEqualTo("nickname");
            softly.assertThat(responses.get(0).theme().id()).isEqualTo(theme.getId());
            softly.assertThat(responses.get(0).theme().name()).isEqualTo("테마");
            softly.assertThat(responses.get(0).theme().description()).isEqualTo("테마 설명");
            softly.assertThat(responses.get(0).theme().thumbnail()).isEqualTo("https://example.com");
            softly.assertThat(responses.get(0).time().id()).isEqualTo(time.getId());
            softly.assertThat(responses.get(0).time().startAt()).isEqualTo("10:30");
        });
    }

    @Test
    @DisplayName("예약을 추가한다.")
    void addReservation() {
        Member member = memberRepository.save(new Member("new@gmail.com", "password", "nickname", Role.USER));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 30)));

        ReservationResponse response = reservationService.addReservation(
                LocalDate.of(2024, 4, 9),
                time.getId(),
                theme.getId(),
                member.getId()
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.date()).isEqualTo("2024-04-09");
            softly.assertThat(response.member().id()).isEqualTo(member.getId());
            softly.assertThat(response.member().email()).isEqualTo("new@gmail.com");
            softly.assertThat(response.member().name()).isEqualTo("nickname");
            softly.assertThat(response.theme().id()).isEqualTo(theme.getId());
            softly.assertThat(response.theme().name()).isEqualTo("테마");
            softly.assertThat(response.theme().description()).isEqualTo("테마 설명");
            softly.assertThat(response.theme().thumbnail()).isEqualTo("https://example.com");
            softly.assertThat(response.time().id()).isEqualTo(time.getId());
            softly.assertThat(response.time().startAt()).isEqualTo("10:30");
        });
    }

    @Test
    @DisplayName("id로 예약을 삭제한다.")
    void deleteReservationById() {
        Member member = memberRepository.save(new Member("new@gmail.com", "password", "nickname", Role.USER));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 30)));

        Reservation reservation = new Reservation(
                LocalDate.of(2024, 4, 9),
                member,
                time,
                theme,
                ReservationStatus.RESERVED
        );
        Reservation savedReservation = reservationRepository.save(reservation);

        reservationService.deleteReservationById(savedReservation.getId());

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).isEmpty();
    }

    @Test
    @DisplayName("나의 예약들을 조회한다.")
    void getReservationsByMemberId() {
        Member member = memberRepository.save(new Member("new@gmail.com", "password", "nickname", Role.USER));
        Theme theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 30)));

        Reservation reservation = new Reservation(
                LocalDate.of(2024, 4, 9),
                member,
                time,
                theme,
                ReservationStatus.RESERVED
        );
        reservationRepository.save(reservation);

        List<MyReservationResponse> responses = reservationService.getMyReservationWithRanks(member.getId());
        MyReservationResponse myReservationResponse = responses.get(0);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(myReservationResponse.id()).isEqualTo(1L);
            softly.assertThat(myReservationResponse.date()).isEqualTo(LocalDate.of(2024, 4, 9));
            softly.assertThat(myReservationResponse.time()).isEqualTo(LocalTime.of(10, 30));
            softly.assertThat(myReservationResponse.theme()).isEqualTo("테마");
            softly.assertThat(myReservationResponse.status()).isEqualTo(ReservationStatus.RESERVED);
            softly.assertThat(myReservationResponse.rank()).isEqualTo(0L);
        });
    }
}
