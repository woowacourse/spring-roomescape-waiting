package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.ADMIN_ZEZE;
import static roomescape.TestFixture.DATE_AFTER_1DAY;
import static roomescape.TestFixture.MEMBER_BROWN;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.ROOM_THEME1;
import static roomescape.TestFixture.ROOM_THEME2;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoomTheme;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private RoomThemeRepository roomThemeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        List<Reservation> reservations = reservationRepository.findAll();
        for (Reservation reservation : reservations) {
            reservationRepository.deleteById(reservation.getId());
        }
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        for (ReservationTime reservationTime : reservationTimes) {
            reservationTimeRepository.deleteById(reservationTime.getId());
        }
        List<RoomTheme> roomThemes = roomThemeRepository.findAll();
        for (RoomTheme roomTheme : roomThemes) {
            roomThemeRepository.deleteById(roomTheme.getId());
        }
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            memberRepository.deleteById(member.getId());
        }
    }

    @DisplayName("존재하는 모든 예약을 보여준다.")
    @Test
    void findAll() {
        assertThat(reservationRepository.findAll()).isEmpty();
    }


    @DisplayName("날짜와 시간이 같은 예약이 존재하는지 여부를 반환한다.")
    @Test
    void duplicatedReservationTest() {
        // given
        Member member = memberRepository.save(MEMBER_BROWN);
        ReservationTime savedReservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        RoomTheme savedRoomTheme = roomThemeRepository.save(ROOM_THEME1);
        boolean existsFalse
                = reservationRepository.existsByDateAndTimeIdAndThemeId(DATE_AFTER_1DAY, savedReservationTime.getId(),
                savedRoomTheme.getId());
        reservationRepository.save(new Reservation(member, DATE_AFTER_1DAY, savedReservationTime, savedRoomTheme));
        // when
        boolean existsTrue
                = reservationRepository.existsByDateAndTimeIdAndThemeId(DATE_AFTER_1DAY, savedReservationTime.getId(),
                savedRoomTheme.getId());
        // then
        assertAll(
                () -> assertThat(existsFalse).isFalse(),
                () -> assertThat(existsTrue).isTrue()
        );
    }

    @DisplayName("예약을 저장한다.")
    @Test
    void save() {
        // given
        Member member = memberRepository.save(MEMBER_BROWN);
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        RoomTheme roomTheme = roomThemeRepository.save(ROOM_THEME1);
        // when
        reservationRepository.save(new Reservation(member, DATE_AFTER_1DAY, reservationTime, roomTheme));
        // then
        assertThat(reservationRepository.findAll()).hasSize(1);
    }

    @DisplayName("해당 id의 예약을 삭제한다.")
    @Test
    void deleteById() {
        // given
        Member member = memberRepository.save(MEMBER_BROWN);
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        RoomTheme roomTheme = roomThemeRepository.save(ROOM_THEME1);
        Reservation savedReservation = reservationRepository.save(
                new Reservation(member, DATE_AFTER_1DAY, reservationTime, roomTheme));
        // when
        reservationRepository.deleteById(savedReservation.getId());
        // then
        assertThat(reservationRepository.findAll()).isEmpty();
    }
}
