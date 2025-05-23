package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createReservationByMember;
import static roomescape.TestFixture.createWaitingByMember;

import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.DBHelper;
import roomescape.DatabaseCleaner;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;
import roomescape.service.dto.result.BookingType;
import roomescape.service.dto.result.MemberBookingResult;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MyPageServiceTest {

    @Autowired
    private MyPageService myPageService;

    @Autowired
    private DBHelper dbHelper;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @Test
    void getMyBookings() {
    }

    @DisplayName("회원ID로 해당 회원의 예약과 대기를 모두 조회할 수 있다.")
    @Test
    void getMemberReservationsById() {
        //given
        Member member = createDefaultMember();
        Reservation reservation = createReservationByMember(member);
        Waiting waiting = createWaitingByMember(member);
        dbHelper.insertReservation(reservation);
        dbHelper.insertWaiting(waiting);

        //when
        List<MemberBookingResult> results = myPageService.getMyBookings(member.getId());

        //then
        assertAll(
                () -> assertThat(results).hasSize(2),
                () -> assertThat(results.getFirst())
                        .isEqualTo(
                                new MemberBookingResult(
                                        reservation.getId(),
                                        reservation.getTheme().getName(),
                                        reservation.getDate(),
                                        reservation.getTime().getStartAt(),
                                        BookingType.RESERVED,
                                        0
                                )
                        )
        );
    }
}
