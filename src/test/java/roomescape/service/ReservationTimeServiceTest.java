//package roomescape.service;
//
//import org.junit.jupiter.api.Test;
//import roomescape.domain.Member;
//import roomescape.domain.MemberRole;
//import roomescape.domain.ReservationTime;
//import roomescape.domain.Theme;
//import roomescape.exception.DeletionNotAllowedException;
//import roomescape.exception.NotFoundReservationTimeException;
//import roomescape.fake.FakeReservationRepository;
//import roomescape.fake.FakeReservationTimeRepository;
//import roomescape.fake.FakeThemeRepository;
//import roomescape.persistence.query.CreateReservationQuery;
//import roomescape.service.param.CreateReservationTimeParam;
//import roomescape.service.result.ReservationTimeResult;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//class ReservationTimeServiceTest {
//
//    FakeReservationRepository reservationRepository = new FakeReservationRepository();
//    FakeThemeRepository themeRepository = new FakeThemeRepository();
//    FakeReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository();
//    ReservationTimeService reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository);
//
//    @Test
//    void 예약_시간을_생성할_수_있다() {
//        //given & when
//        Long createdId = reservationTimeService.create(new CreateReservationTimeParam(LocalTime.of(12, 1)));
//
//        //then
//        assertThat(reservationTimeRepository.findById(createdId))
//                .hasValue(new ReservationTime(1L, LocalTime.of(12, 1)));
//    }
//
//    @Test
//    void id에_해당하는_예약_시간을_찾을_수_있다() {
//        //given
//        reservationTimeRepository.create(new ReservationTime(1L, LocalTime.of(12, 1)));
//
//        //when
//        ReservationTimeResult reservationTimeResult = reservationTimeService.findById(1L);
//
//        //then
//        assertThat(reservationTimeResult).isEqualTo(new ReservationTimeResult(1L, LocalTime.of(12, 1)));
//    }
//
//    @Test
//    void id에_해당하는_예약_시간이_없는경우_예외가_발생한다() {
//        //given & when & then
//        assertThatThrownBy(() -> reservationTimeService.findById(1L))
//                .isInstanceOf(NotFoundReservationTimeException.class)
//                .hasMessage("1에 해당하는 reservation_time 튜플이 없습니다.");
//    }
//
//    @Test
//    void 전체_예약_시간을_조회할_수_있다() {
//        //given
//        reservationTimeRepository.create(new ReservationTime(1L, LocalTime.of(12, 1)));
//
//        //when
//        List<ReservationTimeResult> reservationTimeResults = reservationTimeService.findAll();
//
//        //then
//        assertThat(reservationTimeResults).isEqualTo(List.of(
//                new ReservationTimeResult(1L, LocalTime.of(12, 1))
//        ));
//    }
//
//    @Test
//    void id에_해당하는_예약_시간을_삭제한다() {
//        //given
//        reservationTimeRepository.create(new ReservationTime(1L, LocalTime.of(12, 1)));
//
//        //when
//        reservationTimeService.deleteById(1L);
//
//        //then
//        assertThat(reservationTimeRepository.findById(1L)).isEmpty();
//    }
//
//    @Test
//    void time_id를_사용하는_예약이_존재하면_예외를_던진다() {
//        //given
//        themeRepository.create(new Theme(1L, "name", "description", "thumbnail"));
//        reservationTimeRepository.create(new ReservationTime(1L, LocalTime.of(12, 1)));
//        reservationRepository.create(new CreateReservationQuery(new Member(1L, "Bob", MemberRole.USER, "bob@example.com", "password"), LocalDate.of(2025, 4, 30), new ReservationTime(1L, LocalTime.of(12, 1)), new Theme(1L, "name", "description", "thumbnail")));
//
//        //when & then
//        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
//                .isInstanceOf(DeletionNotAllowedException.class)
//                .hasMessage("해당 예약 시간에 예약이 존재합니다.");
//    }
//}
