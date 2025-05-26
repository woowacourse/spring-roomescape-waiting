package roomescape.reservation.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.auth.service.dto.LoginMember;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.service.dto.request.ReservationCreateRequest;
import roomescape.reservation.service.dto.response.ReservationResponse;
import roomescape.reservation.service.dto.response.ReservationTimeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.dto.response.ThemeResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ActiveProfiles("test")
@DataJpaTest
@Import(CreateReservationService.class)
class CreateReservationServiceTest {

    private final LocalDateTime now = LocalDateTime.now();
    private final Theme theme = new Theme("포스티", "공포", "wwww.um.com");
    private final ReservationTime time = new ReservationTime(LocalTime.of(8, 0));
    private final Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CreateReservationService reservationService;

    @BeforeEach
    void setup() {
        entityManager.persist(theme);
        entityManager.persist(time);
        entityManager.persist(member);
    }

    @DisplayName("예약을 추가한다.")
    @Test
    void test3() {
        // given
        LocalDate date = nextDay();
        ReservationCreateRequest request = new ReservationCreateRequest(date, time.getId(), theme.getId(), LoginMember.of(member));

        // when
        ReservationResponse result = reservationService.create(request);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.member().name()).isEqualTo("포스티");
            softly.assertThat(result.date()).isEqualTo(date);
            softly.assertThat(result.time()).isEqualTo(new ReservationTimeResponse(time.getId(), time.getStartAt()));
            softly.assertThat(result.theme()).isEqualTo(new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail()));
        });
    }

    @DisplayName("이미 존재하는 예약과 날짜, 시간, 테마가 동일하면 예외가 발생한다.")
    @Test
    void test4() {
        // given
        LocalDate date = nextDay();

        Member otherMember = new Member("밍곰", "test@test.com", "12341234", Role.MEMBER);
        entityManager.persist(otherMember);

        ReservationCreateRequest request = new ReservationCreateRequest(date, time.getId(), theme.getId(), LoginMember.of(member));
        reservationService.create(request);

        ReservationCreateRequest duplicatedRequest = new ReservationCreateRequest(date, time.getId(), theme.getId(), LoginMember.of(otherMember));

        // when & then
        assertThatThrownBy(() -> reservationService.create(duplicatedRequest))
                .isInstanceOf(DuplicatedException.class);
    }

    @DisplayName("과거 날짜에 예약을 추가하면 예외가 발생한다.")
    @Test
    void test5() {
        // given
        LocalDate date = now.toLocalDate();

        ReservationTime pastTime = new ReservationTime(now.toLocalTime().minusMinutes(1));
        entityManager.persist(pastTime);

        ReservationCreateRequest requestDto = new ReservationCreateRequest(date, pastTime.getId(), theme.getId(), LoginMember.of(member));

        // when & then
        assertThatThrownBy(() -> reservationService.create(requestDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("존재하지 않는 예약 시간 ID로 저장하면 예외를 반환한다.")
    @Test
    void test6() {
        LocalDate date = nextDay();

        Long notExistId = 1000L;
        ReservationCreateRequest requestDto =
                new ReservationCreateRequest(date, notExistId, theme.getId(), LoginMember.of(member));

        assertThatThrownBy(() -> reservationService.create(requestDto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("존재하지 않는 테마 ID로 저장하면 예외를 반환한다.")
    @Test
    void notExistThemeId() {
        LocalDate date = now.toLocalDate().plusDays(1);

        ReservationTime time = new ReservationTime(LocalTime.of(8, 0));
        entityManager.persist(time);

        Long notExistId = 1000L;
        ReservationCreateRequest requestDto = new ReservationCreateRequest(date, time.getId(), notExistId, LoginMember.of(member));

        assertThatThrownBy(() -> reservationService.create(requestDto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private LocalDate nextDay() {
        return LocalDate.now().plusDays(1);
    }
}
