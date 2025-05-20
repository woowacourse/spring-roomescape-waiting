//package roomescape.repository;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
//import org.springframework.jdbc.core.JdbcTemplate;
//import roomescape.domain.ReservationTheme;
//
//@JdbcTest
//@Disabled
//class ReservationThemeRepositoryImplTest {
//
//    @Autowired
//    private JdbcTemplate template;
//
//    private ReservationThemeRepository repository;
//
//    @BeforeEach
//    void setUp() {
//        repository = new ReservationThemeRepositoryImpl(template, null);
//    }
//
//
//    @DisplayName("id로 테마 데이터를 성공적으로 가져온다.")
//    @Test
//    void findById() {
//        //given
//        final long id = 1L;
//
//        //when
//        final ReservationTheme theme = repository.findById(id).get();
//
//        //then
//        assertThat(theme.getId()).isEqualTo(id);
//    }
//
//    @DisplayName("모든 테마 데이터를 성공적으로 가져온다.")
//    @Test
//    void findAll() {
//        //given & when
//        final List<ReservationTheme> themes = repository.findAll();
//
//        //then
//        assertThat(themes).isNotEmpty();
//    }
//
//    @DisplayName("주간 인기테마를 성공적으로 가져온다.")
//    @Test
//    void findWeeklyThemeOrderByCountDesc() {
//        //given & when
//        final List<ReservationTheme> weeklyThemeOrderByCountDesc = repository.findWeeklyThemeOrderByCountDesc();
//
//        //then
//        assertThat(weeklyThemeOrderByCountDesc).hasSizeLessThanOrEqualTo(10);
//    }
//
//    @DisplayName("테마를 성공적으로 저장한다.")
//    @Test
//    void save() {
//        //given
//        final ReservationTheme reservationTheme = new ReservationTheme("테마1", "디스크립션1", "썸네일1");
//
//        //when
//        final ReservationTheme expected = repository.save(reservationTheme);
//
//        //then
//        assertAll(
//                () -> assertThat(expected.getName()).isEqualTo(reservationTheme.getName()),
//                () -> assertThat(expected.getDescription()).isEqualTo(reservationTheme.getDescription()),
//                () -> assertThat(expected.getThumbnail()).isEqualTo(reservationTheme.getThumbnail())
//        );
//    }
//
//    @DisplayName("id로 테마를 성공적으로 삭제한다.")
//    @Test
//    void deleteById() {
//        //given
//        final ReservationTheme reservationTheme = new ReservationTheme("테마1", "디스크립션1", "썸네일1");
//
//        //when
//        final ReservationTheme expected = repository.save(reservationTheme);
//        final int result = repository.deleteById(expected.getId());
//
//        //
//        assertThat(result).isEqualTo(1);
//    }
//
//    @DisplayName("이미 존재하는 테마이므로 true를 반환한다.")
//    @Test
//    void existsByName() {
//        //given
//        final String name = "테마1";
//        final ReservationTheme reservationTheme = new ReservationTheme(name, "디스크립션1", "썸네일1");
//        repository.save(reservationTheme);
//
//        //when
//        final boolean expected = repository.existsByName(name);
//
//        //then
//        assertThat(expected).isTrue();
//    }
//}
