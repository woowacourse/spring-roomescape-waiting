/*
package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import roomescape.error.ReservationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.fake.FakeReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservationtime.fake.FakeReservationTimeRepository;
import roomescape.theme.domain.Theme;

class ReservationTimeServiceTest {

    private LocalTime t1 = LocalTime.of(10, 0);
    private LocalTime t2 = LocalTime.of(11, 0);

    private ReservationTime rt1 = new ReservationTime(1L, t1);
    private ReservationTime rt2 = new ReservationTime(2L, t2);

    private Theme theme1 = new Theme(1L, "테마1", "설명1", "썸네일1");
    private Theme theme2 = new Theme(1L, "테마2", "설명2", "썸네일2");

    private Member member1 = new Member(1L, "유저1", "user1@naver.com", "pwd", MemberRole.MEMBER.name());

    private Reservation r1 = new Reservation(1L, LocalDate.of(2025, 5, 11), rt1, theme1, member1);
    private Reservation r2 = new Reservation(2L, LocalDate.of(2025, 6, 11), rt2, theme2, member1);

    private FakeReservationRepository fakeReservationRepo = new FakeReservationRepository(r1, r2);
    private FakeReservationTimeRepository stubReservationTimeRepo = new FakeReservationTimeRepository(rt1, rt2);

    private ReservationTimeService service = new ReservationTimeService(stubReservationTimeRepo,
            fakeReservationRepo);


    @Test
    void 예약_시간이_저장된다() {
        // given
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(12, 30));

        // when
        ReservationTimeResponse response = service.saveTime(request);

        // then
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(response.startAt()).isEqualTo(request.startAt());
            soft.assertThat(stubReservationTimeRepo.findAll())
                    .extracting(ReservationTime::getStartAt)
                    .contains(request.startAt());
        });
    }

    @Test
    void 모든_예약_시간을_조회한다() {
        // when
        List<ReservationTimeResponse> all = service.findAll();

        // then
        assertThat(all)
                .hasSize(2)
                .extracting(ReservationTimeResponse::startAt)
                .containsExactlyInAnyOrder(t1, t2);
    }

    @Test
    void 예약_시간이_삭제된다() {
        // given
        assertThat(service.findAll()).hasSize(2);

        // when
        service.delete(1L);

        // then
        List<ReservationTimeResponse> afterDelete = service.findAll();
        assertThat(afterDelete)
                .hasSize(1)
                .extracting(ReservationTimeResponse::id)
                .doesNotContain(1L);
    }

    @Test
    void 존재하지_않는_아이디면_빈_옵셔널을_반환한다() {
        // given
        Optional<ReservationTime> found = stubReservationTimeRepo.findById(999L);

        // when
        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 예약이_존재하는_시간을_삭제하지_못_한다() {
        // given
        fakeReservationRepo.setExistsByReservationTimeId(true);

        // when
        // then
        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ReservationException.class)
                .hasMessage("해당 시간으로 예약된 건이 존재합니다.");
        fakeReservationRepo.setExistsByReservationTimeId(false);
    }
}
*/
