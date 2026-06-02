package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.MemberDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.dao.jdbc.MemberJdbcDao;
import roomescape.dao.jdbc.ReservationJdbcDao;
import roomescape.dao.jdbc.ThemeJdbcDao;
import roomescape.dao.jdbc.TimeJdbcDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.vo.Name;
import roomescape.dto.request.AdminReservationRequestDto;
import roomescape.dto.request.ReservationPatchDto;
import roomescape.dto.response.PageResponse;

@JdbcTest
@Import({AdminReservationService.class, ReservationJdbcDao.class, TimeJdbcDao.class, ThemeJdbcDao.class, MemberJdbcDao.class})
@ActiveProfiles("test")
class AdminReservationServiceTest {

    @Autowired
    private AdminReservationService adminReservationService;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private TimeDao timeDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member;
    private Time savedTime1;
    private Time savedTime2;
    private Theme savedTheme1;
    private Theme savedTheme2;
    private AdminReservationRequestDto requestDto1;
    private AdminReservationRequestDto requestDto2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO members(name, email, password, role) VALUES (?, ?, ?, ?)",
                "유저", "user@test.com", "password", "USER"
        );
        member = memberDao.findByEmail("user@test.com").orElseThrow();
        savedTime1 = timeDao.insert(new Time(LocalTime.of(13, 0)));
        savedTime2 = timeDao.insert(new Time(LocalTime.of(14, 0)));
        savedTheme1 = themeDao.insert(new Theme(new Name("방탈출 이름1"), "http://thumbnail_url", "방탈출을 할 수 있다."));
        savedTheme2 = themeDao.insert(new Theme(new Name("방탈출 이름2"), "http://thumbnail_url", "방탈출을 할 수 있다."));
        requestDto1 = new AdminReservationRequestDto(member.getId(), LocalDate.now().plusDays(1), savedTime1.getId(), savedTheme1.getId(), null);
        requestDto2 = new AdminReservationRequestDto(member.getId(), LocalDate.now().plusDays(2), savedTime2.getId(), savedTheme2.getId(), null);
    }

    @Nested
    class FindAll {

        @Test
        @DisplayName("예약이 없으면 빈 목록과 totalElements 0을 반환한다")
        void returnsEmptyPage() {
            PageResponse<Reservation> result = adminReservationService.findAll(0, 10);

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }

        @Test
        @DisplayName("페이지 크기만큼 예약 목록을 반환한다")
        void returnsPagedReservations() {
            List<Reservation> saved = new ArrayList<>();
            saved.add(adminReservationService.createByAdmin(requestDto1));
            saved.add(adminReservationService.createByAdmin(requestDto2));
            Collections.reverse(saved);

            PageResponse<Reservation> result = adminReservationService.findAll(0, 10);

            assertThat(result.content()).isEqualTo(saved);
            assertThat(result.totalElements()).isEqualTo(saved.size());
        }

        @Test
        @DisplayName("size보다 데이터가 많으면 size만큼만 반환한다")
        void returnsOnlySizeItems() {
            adminReservationService.createByAdmin(requestDto1);
            Reservation saved2 = adminReservationService.createByAdmin(requestDto2);

            PageResponse<Reservation> result = adminReservationService.findAll(0, 1);

            assertThat(result.content()).isEqualTo(List.of(saved2));
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.totalPages()).isEqualTo(2);
        }
    }

    @Nested
    class FindById {

        @Test
        @DisplayName("존재하는 id로 예약을 조회한다")
        void returnsReservationById() {
            Reservation saved = adminReservationService.createByAdmin(requestDto1);

            assertThat(adminReservationService.findById(saved.getId())).isEqualTo(saved);
        }

        @Test
        @DisplayName("존재하지 않는 id를 조회하면 예외를 반환한다")
        void throwsWhenIdNotFound() {
            assertThatThrownBy(() -> adminReservationService.findById(-1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class CreateByAdmin {

        @Test
        @DisplayName("과거 날짜로도 예약을 생성한다")
        void createsReservationWithPastDate() {
            AdminReservationRequestDto pastDto = new AdminReservationRequestDto(
                    member.getId(), LocalDate.now().minusDays(1), savedTime1.getId(), savedTheme1.getId(), null);

            Reservation saved = adminReservationService.createByAdmin(pastDto);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getDate()).isEqualTo(LocalDate.now().minusDays(1));
        }

        @Test
        @DisplayName("미래 날짜로도 예약을 생성한다")
        void createsReservationWithFutureDate() {
            Reservation saved = adminReservationService.createByAdmin(requestDto1);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getDate()).isEqualTo(requestDto1.date());
        }
    }

    @Nested
    class Update {

        @Test
        @DisplayName("예약의 날짜와 시간을 수정한다")
        void updatesReservation() {
            Reservation saved = adminReservationService.createByAdmin(requestDto1);
            LocalDate newDate = LocalDate.now().plusDays(3);
            ReservationPatchDto updateDto = new ReservationPatchDto(newDate, savedTime2.getId());

            Reservation updated = adminReservationService.update(saved.getId(), updateDto);

            assertThat(updated.getDate()).isEqualTo(newDate);
            assertThat(updated.getTime()).isEqualTo(savedTime2);
        }

        @Test
        @DisplayName("존재하지 않는 id를 수정하면 예외를 반환한다")
        void throwsWhenIdNotFound() {
            ReservationPatchDto updateDto = new ReservationPatchDto(LocalDate.now().plusDays(3), savedTime1.getId());

            assertThatThrownBy(() -> adminReservationService.update(-1L, updateDto))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("존재하지 않는 시간으로 수정하면 예외를 반환한다")
        void throwsWhenTimeNotFound() {
            Reservation saved = adminReservationService.createByAdmin(requestDto1);
            ReservationPatchDto updateDto = new ReservationPatchDto(LocalDate.now().plusDays(3), -1L);

            assertThatThrownBy(() -> adminReservationService.update(saved.getId(), updateDto))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class CancelByAdmin {

        @Test
        @DisplayName("과거 예약도 취소할 수 있다")
        void cancelsPastReservation() {
            Reservation saved = reservationDao.insert(
                    Reservation.createByAdmin(member, LocalDate.now().minusDays(1), savedTime1, savedTheme1));

            adminReservationService.cancelByAdmin(saved.getId());

            Reservation canceled = reservationDao.findById(saved.getId()).orElseThrow();
            assertThat(canceled.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        }

        @Test
        @DisplayName("존재하지 않는 id를 취소하면 예외를 반환한다")
        void throwsWhenIdNotFound() {
            assertThatThrownBy(() -> adminReservationService.cancelByAdmin(-1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("예약을 삭제한다")
        void deletesReservation() {
            Reservation saved = adminReservationService.createByAdmin(requestDto1);
            adminReservationService.delete(saved.getId());

            assertThat(reservationDao.existsById(saved.getId())).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 id를 삭제하면 예외를 반환한다")
        void throwsWhenDeletingNonExistentId() {
            assertThatThrownBy(() -> adminReservationService.delete(-1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
