package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import roomescape.config.JpaConfig;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationitem.ReservationItem;
import roomescape.domain.reservationitem.ReservationItemRepository;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationitem.ReservationTheme;
import roomescape.domain.reservationitem.ReservationThemeRepository;
import roomescape.domain.reservationitem.ReservationTime;
import roomescape.domain.reservationitem.ReservationTimeRepository;
import roomescape.repository.impl.ReservationItemRepositoryImpl;
import roomescape.repository.impl.ReservationRepositoryImpl;
import roomescape.repository.impl.ReservationThemeRepositoryImpl;
import roomescape.repository.impl.ReservationTimeRepositoryImpl;
import roomescape.repository.jpa.ReservationItemJpaRepository;
import roomescape.repository.jpa.ReservationJpaRepository;
import roomescape.repository.jpa.ReservationThemeJpaRepository;
import roomescape.repository.jpa.ReservationTimeJpaRepository;

@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(JpaConfig.class)
@DataJpaTest
class ReservationThemeRepositoryImplTest {

    private ReservationThemeRepository repository;

    @Autowired
    private ReservationThemeJpaRepository reservationThemeJpaRepository;
    @Autowired
    private ReservationJpaRepository reservationJpaRepository;
    @Autowired
    private ReservationTimeJpaRepository reservationTimeJpaRepository;
    @Autowired
    private ReservationItemJpaRepository reservationItemJpaRepository;

    private ReservationTheme savedTheme1;
    private ReservationTheme savedTheme2;
    private ReservationTheme savedTheme3;


    @BeforeEach
    void setUp() {
        repository = new ReservationThemeRepositoryImpl(reservationThemeJpaRepository);

        ReservationTheme theme1 = new ReservationTheme("Theme 1", "Description", "Thumbnail");
        ReservationTheme theme2 = new ReservationTheme("Theme 2", "Description", "Thumbnail");
        ReservationTheme theme3 = new ReservationTheme("Theme 3", "Description", "Thumbnail");

        savedTheme1 = repository.save(theme1);
        savedTheme2 = repository.save(theme2);
        savedTheme3 = repository.save(theme3);
    }

    @DisplayName("id로 테마 데이터를 성공적으로 가져온다.")
    @Test
    void findById() {
        //when
        final Optional<ReservationTheme> theme = repository.findById(savedTheme1.getId());

        //then
        assertAll(
                () -> assertThat(theme).isPresent(),
                () -> assertThat(theme.get().getId()).isEqualTo(savedTheme1.getId()),
                () -> assertThat(theme.get().getName()).isEqualTo(savedTheme1.getName()),
                () -> assertThat(theme.get().getDescription()).isEqualTo(savedTheme1.getDescription()),
                () -> assertThat(theme.get().getThumbnail()).isEqualTo(savedTheme1.getThumbnail())
        );
    }

    @DisplayName("모든 테마 데이터를 성공적으로 가져온다.")
    @Test
    void findAll() {
        //when
        final List<ReservationTheme> themes = repository.findAll();

        //then
        assertAll(
                () -> assertThat(themes).isNotEmpty(),
                () -> assertThat(themes).hasSize(3),
                () -> assertThat(themes.get(0).getId()).isEqualTo(savedTheme1.getId()),
                () -> assertThat(themes.get(0).getName()).isEqualTo(savedTheme1.getName())
        );
    }

    @DisplayName("주간 인기테마를 성공적으로 가져온다.")
    @Test
    void findWeeklyThemeOrderByCountDesc() {
        // given
        saveDummyReservation();

        // when
        final List<ReservationTheme> weeklyThemeOrderByCountDesc = repository.findWeeklyThemeOrderByCountDesc(
                2,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)
        );

        // then
        assertAll(
                () -> assertThat(weeklyThemeOrderByCountDesc).hasSize(2),
                () -> assertThat(
                        weeklyThemeOrderByCountDesc.stream()
                                .map(ReservationTheme::getName)
                                .toList()
                ).containsExactly(savedTheme1.getName(), savedTheme2.getName())
        );
    }

    @DisplayName("테마를 성공적으로 저장한다.")
    @Test
    void save() {
        //given
        final ReservationTheme newTheme = new ReservationTheme("new Theme", "new Description", "new Thumbnail");

        //when
        final ReservationTheme saved = repository.save(newTheme);

        //then
        assertAll(
                () -> assertThat(saved.getId()).isNotNull(),
                () -> assertThat(saved.getName()).isEqualTo(newTheme.getName()),
                () -> assertThat(saved.getDescription()).isEqualTo(newTheme.getDescription()),
                () -> assertThat(saved.getThumbnail()).isEqualTo(newTheme.getThumbnail())
        );
    }

    @DisplayName("id로 테마를 성공적으로 삭제한다.")
    @Test
    void deleteById() {
        //given
        Long themeId = savedTheme1.getId();

        //when & then
        assertAll(
                () -> assertThatCode(() -> repository.deleteById(themeId)).doesNotThrowAnyException(),
                () -> assertThat(repository.findById(themeId)).isEmpty()
        );
    }

    @DisplayName("이미 존재하는 테마이므로 true를 반환한다.")
    @Test
    void existsByName() {
        //given
        String themeName = savedTheme1.getName();

        //when
        final boolean expected = repository.existsByName(themeName);

        //then
        assertThat(expected).isTrue();
    }

    private void saveDummyReservation() {
        ReservationTimeRepository reservationTimeRepository = new ReservationTimeRepositoryImpl(reservationTimeJpaRepository);
        ReservationRepository reservationRepository = new ReservationRepositoryImpl(reservationJpaRepository);
        ReservationItemRepository reservationItemRepository = new ReservationItemRepositoryImpl(reservationItemJpaRepository);

        final ReservationTime reservationTime1 = reservationTimeRepository.save(new ReservationTime(LocalTime.now().plusHours(1)));
        final ReservationTime reservationTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.now().plusHours(2)));
        final ReservationTime reservationTime3 = reservationTimeRepository.save(new ReservationTime(LocalTime.now().plusHours(3)));

        final ReservationItem reservationItem1 = reservationItemRepository.save(new ReservationItem(LocalDate.now().plusDays(1), reservationTime1, savedTheme1));
        final ReservationItem reservationItem2 = reservationItemRepository.save(new ReservationItem(LocalDate.now().plusDays(1), reservationTime2, savedTheme1));
        final ReservationItem reservationItem3 = reservationItemRepository.save(new ReservationItem(LocalDate.now().plusDays(1), reservationTime3, savedTheme1));
        final ReservationItem reservationItem4 = reservationItemRepository.save(new ReservationItem(LocalDate.now().plusDays(2), reservationTime1, savedTheme2));
        final ReservationItem reservationItem5 = reservationItemRepository.save(new ReservationItem(LocalDate.now().plusDays(2), reservationTime2, savedTheme2));
        final ReservationItem reservationItem6 = reservationItemRepository.save(new ReservationItem(LocalDate.now().plusDays(3), reservationTime1, savedTheme3));
        final ReservationItem reservationItem7 = reservationItemRepository.save(new ReservationItem(LocalDate.now().plusDays(3), reservationTime2, savedTheme3));
        final ReservationItem reservationItem8 = reservationItemRepository.save(new ReservationItem(LocalDate.now().plusDays(3), reservationTime3, savedTheme3));

        reservationRepository.save(new Reservation(null, reservationItem1, ReservationStatus.ACCEPTED));
        reservationRepository.save(new Reservation(null, reservationItem2, ReservationStatus.ACCEPTED));
        reservationRepository.save(new Reservation(null, reservationItem3, ReservationStatus.ACCEPTED));

        reservationRepository.save(new Reservation(null, reservationItem4, ReservationStatus.ACCEPTED));
        reservationRepository.save(new Reservation(null, reservationItem5, ReservationStatus.ACCEPTED));

        reservationRepository.save(new Reservation(null, reservationItem6, ReservationStatus.ACCEPTED));
        reservationRepository.save(new Reservation(null, reservationItem7, ReservationStatus.ACCEPTED));
        reservationRepository.save(new Reservation(null, reservationItem8, ReservationStatus.ACCEPTED));
    }
}
