package roomescape.theme.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.common.util.time.DateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.infrastructure.JdbcMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.infrastructure.JdbcReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.exception.ThemeException;
import roomescape.theme.infrastructure.JdbcThemeRepository;
import roomescape.theme.presentation.dto.PopularThemeResponse;

class ThemeServiceTest {

    private final static DataSource DATASOURCE = DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:testDatabase")
            .username("sa")
            .build();

    private ThemeService themeService;

    @BeforeEach
    void beforeEach() {
        DatabaseInitializationSettings databaseInitializationSettings = new DatabaseInitializationSettings();
        databaseInitializationSettings.setSchemaLocations(List.of("classpath:/schema.sql"));
        DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer = new DataSourceScriptDatabaseInitializer(DATASOURCE, databaseInitializationSettings);
        dataSourceScriptDatabaseInitializer.initializeDatabase();

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(DATASOURCE);

        ThemeRepository themeRepository = new JdbcThemeRepository(namedParameterJdbcTemplate, DATASOURCE);
        Theme theme1 = Theme.createWithoutId("테스트1", "설명", "썸네일");
        Theme theme2 = Theme.createWithoutId("테스트2", "설명", "썸네일");
        Theme theme3 = Theme.createWithoutId("테스트3", "설명", "썸네일");

        Long themeId1 = themeRepository.save(theme1);
        Long themeId2 = themeRepository.save(theme2);
        Long themeId3 = themeRepository.save(theme3);

        theme1 = themeRepository.findById(themeId1);
        themeRepository.findById(themeId2);
        theme3 = themeRepository.findById(themeId3);

        MemberRepository memberRepository = new JdbcMemberRepository(namedParameterJdbcTemplate, DATASOURCE);
        Long memberId1 = memberRepository.save(Member.createWithoutId("유저1", "email1@email.com", "password"));
        Long memberId2 = memberRepository.save(Member.createWithoutId("유저2", "email2@email.com", "password"));
        Long memberId3 = memberRepository.save(Member.createWithoutId("유저3", "email3@email.com", "password"));

        Member member1 = memberRepository.findById(memberId1);
        Member member2 = memberRepository.findById(memberId2);
        Member member3 = memberRepository.findById(memberId3);

        ReservationRepository reservationRepository = new JdbcReservationRepository(namedParameterJdbcTemplate, DATASOURCE);
        reservationRepository.save(Reservation.createWithoutId(LocalDate.of(2025, 12, 5), ReservationTime.createWithoutId(LocalTime.of(1, 0)), theme1, member1));
        reservationRepository.save(Reservation.createWithoutId(LocalDate.of(2025, 12, 6), ReservationTime.createWithoutId(LocalTime.of(1, 0)), theme1, member2));
        reservationRepository.save(Reservation.createWithoutId(LocalDate.of(2025, 12, 4), ReservationTime.createWithoutId(LocalTime.of(1, 0)), theme3, member3));

        DateTime dateTime = () -> LocalDateTime.of(2025, 12, 7, 10, 0);

        themeService = new ThemeService(dateTime, themeRepository, reservationRepository);
    }

    @DisplayName("존재하는 예약의 테마는 삭제할 수 없다.")
    @Test
    void can_not_remove_exists_reservation() {
        Assertions.assertThatThrownBy(() -> themeService.deleteThemeById(1L))
                .isInstanceOf(ThemeException.class);
    }

    @DisplayName("인기 테마를 가져올 수 있다.")
    @Test
    void can_get_popular_theme() {
        List<PopularThemeResponse> popularThemes = themeService.getPopularThemes();

        Assertions.assertThat(popularThemes).containsExactly(
                new PopularThemeResponse("테스트1", "설명", "썸네일"),
                new PopularThemeResponse("테스트3", "설명", "썸네일")
        );
    }
}
