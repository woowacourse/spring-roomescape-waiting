//package roomescape.service;
//
//import org.junit.jupiter.api.Test;
//import roomescape.domain.*;
//import roomescape.exception.DeletionNotAllowedException;
//import roomescape.exception.NotFoundThemeException;
//import roomescape.fake.FakeReservationRepository;
//import roomescape.fake.FakeThemeRepository;
//import roomescape.persistence.ReservationRepository;
//import roomescape.persistence.ThemeRepository;
//import roomescape.persistence.query.CreateReservationQuery;
//import roomescape.service.param.CreateThemeParam;
//import roomescape.service.result.ThemeResult;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//class ThemeServiceTest {
//
//    private final ReservationRepository reservationRepository = new FakeReservationRepository();
//    private final ThemeRepository themeRepository = new FakeThemeRepository();
//    private final ThemeService themeService = new ThemeService(themeRepository, reservationRepository);
//
//    @Test
//    void 테마를_전체_조회할_수_있다() {
//        //given
//        themeRepository.create(new Theme("test1", "description1", "thumbnail1"));
//        themeRepository.create(new Theme("test2", "description2", "thumbnail2"));
//
//        //when
//        List<ThemeResult> themeResults = themeService.findAll();
//
//        //then
//        assertThat(themeResults).isEqualTo(List.of(
//                new ThemeResult(1L, "test1", "description1", "thumbnail1"),
//                new ThemeResult(2L, "test2", "description2", "thumbnail2")
//        ));
//    }
//
//    @Test
//    void 테마를_생성할_수_있다() {
//        //given
//        CreateThemeParam createThemeParam = new CreateThemeParam("test1", "description1", "thumbnail1");
//
//        //when
//        Long id = themeService.create(createThemeParam);
//
//        //then
//        assertThat(themeRepository.findById(id))
//                .hasValue(new Theme(1L, "test1", "description1", "thumbnail1"));
//    }
//
//    @Test
//    void id값으로_테마를_찾을_수_있다() {
//        //given
//        themeRepository.create(new Theme("test1", "description1", "thumbnail1"));
//
//        //when
//        ThemeResult themeResult = themeService.findById(1L);
//
//        //then
//        assertThat(themeResult).isEqualTo(new ThemeResult(1L, "test1", "description1", "thumbnail1"));
//    }
//
//    @Test
//    void id값으로_테마를_찾을때_없다면_예외가_발생한다() {
//        // given & when & then
//        assertThatThrownBy(() -> themeService.findById(1L))
//                .isInstanceOf(NotFoundThemeException.class)
//                .hasMessage("id에 해당하는 Theme이 없습니다.");
//    }
//
//    @Test
//    void id값으로_테마를_삭제할_수_있다() {
//        //given
//        themeRepository.create(new Theme("test1", "description1", "thumbnail1"));
//
//        //when
//        themeService.deleteById(1L);
//
//        //then
//        assertThat(themeRepository.findById(1L)).isEmpty();
//    }
//
//    @Test
//    void id값으로_테마를_삭제할떄_예약에서_id가_사용중이라면_예외를_발생시킨다() {
//        //given
//        themeRepository.create(new Theme("test1", "description1", "thumbnail1"));
//        reservationRepository.create(new CreateReservationQuery(new Member(1L, "test1", MemberRole.USER, "email", "password"),
//                LocalDate.of(2025, 5, 1),
//                new ReservationTime(1L, LocalTime.of(12, 0)),
//                new Theme(1L, "test1", "description1", "thumbnail1")));
//
//        //when & then
//        assertThatThrownBy(() -> themeService.deleteById(1L))
//                .isInstanceOf(DeletionNotAllowedException.class)
//                .hasMessage("해당 테마에 예약이 존재합니다.");
//    }
//
//}
