package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.IntegrationTestSupport;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;

@Transactional
class ReservationRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("예약 생성")
    @Test
    void save() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.parse("08:00"));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("이름", "설명", "썸네일");
        Theme savedTheme = themeRepository.save(theme);

        Member member = Member.createUser("생강", "email@email.com", "1234");
        Member savedMember = memberRepository.save(member);

        Reservation reservation = new Reservation(savedMember, LocalDate.parse("2025-01-01"), savedReservationTime, savedTheme);
        Reservation savedReservation = reservationRepository.save(reservation);
        assertAll(
                () -> assertThat(savedReservation.getMember().getName()).isEqualTo("생강"),
                () -> assertThat(savedReservation.getDate()).isEqualTo("2025-01-01"),
                () -> assertThat(savedReservation.getTime()).isEqualTo(savedReservationTime),
                () -> assertThat(savedReservation.getTheme()).isEqualTo(savedTheme)
        );
    }

    @DisplayName("존재하는 예약 삭제")
    @Test
    void deleteExistById() {
        assertThatCode(() -> reservationRepository.deleteById(1L))
                .doesNotThrowAnyException();
    }
}
