package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DATE_AFTER_1DAY;
import static roomescape.TestFixture.MEMBER_BROWN;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.ROOM_THEME1;
import static roomescape.TestFixture.TIME;

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
import roomescape.exception.NotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationTimeRepositoryTest {

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

    @DisplayName("모든 예약 시간을 보여준다")
    @Test
    void findAll() {
        assertThat(reservationTimeRepository.findAll()).isEmpty();
    }

    @DisplayName("예약 시간을 저장한다.")
    @Test
    void save() {
        // given & when
        reservationTimeRepository.save(RESERVATION_TIME_10AM);
        // then
        assertThat(reservationTimeRepository.findAll()).hasSize(1);
    }

    @DisplayName("해당 id의 예약 시간을 보여준다.")
    @Test
    void findById() {
        // given & when
        ReservationTime savedReservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        Long id = savedReservationTime.getId();
        // then
        ReservationTime reservationTime = reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약시간을 찾을 수 없습니다."));
        assertThat(reservationTime.getStartAt()).isEqualTo(TIME);
    }

    @DisplayName("중복된 예약 시간이 존재하는 지 여부를 반환한다.")
    @Test
    void existsByStartAt() {
        // given
        boolean existsFalse = reservationTimeRepository.existByStartAt(TIME);
        reservationTimeRepository.save(RESERVATION_TIME_10AM);
        // when
        boolean existsTrue = reservationTimeRepository.existByStartAt(TIME);
        // then
        assertAll(
                () -> assertThat(existsFalse).isFalse(),
                () -> assertThat(existsTrue).isTrue()
        );
    }

    @DisplayName("해당 id의 예약 시간을 삭제한다.")
    @Test
    void deleteById() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        // when
        reservationTimeRepository.deleteById(reservationTime.getId());
        // then
        assertThat(reservationTimeRepository.findAll()).isEmpty();
    }

    @DisplayName("해당 id의 예약 시간을 삭제하는 경우, 그 id를 참조하는 예약도 삭제한다.")
    @Test
    void deleteByIdDeletesReservationAlso() {
        // given
        Member member = memberRepository.save(MEMBER_BROWN);
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        RoomTheme roomTheme = roomThemeRepository.save(ROOM_THEME1);
        reservationRepository.save(new Reservation(member, DATE_AFTER_1DAY, reservationTime, roomTheme));
        // when
        reservationTimeRepository.deleteById(reservationTime.getId());
        // then
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @DisplayName("삭제 대상이 존재하면 true를 반환한다.")
    @Test
    void returnTrueWhenDeleted() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        // when
        boolean deleted = reservationTimeRepository.deleteById(reservationTime.getId());
        // then
        assertThat(deleted).isTrue();
    }

    @DisplayName("삭제 대상이 존재하지 않으면 false를 반환한다.")
    @Test
    void returnFalseWhenNotDeleted() {
        // given & when
        boolean deleted = reservationTimeRepository.deleteById(1L);
        // then
        assertThat(deleted).isFalse();
    }
}
