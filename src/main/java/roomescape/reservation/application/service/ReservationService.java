package roomescape.reservation.application.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.application.repository.MemberRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.application.dto.CreateReservationRequest;
import roomescape.reservation.application.repository.ReservationRepository;
import roomescape.reservation.application.repository.ReservationTimeRepository;
import roomescape.reservation.application.repository.ThemeRepository;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.presentation.dto.AdminReservationRequest;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository,
                              final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ReservationResponse createReservation(final ReservationRequest reservationRequest, final Long memberId) {
        ReservationDate reservationDate = new ReservationDate(reservationRequest.getDate());
        ReservationTime reservationTime = getReservationTime(reservationRequest.getTimeId());
        Theme theme = getTheme(reservationRequest.getThemeId());
        validateReservationDateTime(reservationDate, reservationTime);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));

        CreateReservationRequest createReservationRequest = new CreateReservationRequest(
                member,
                theme,
                reservationDate,
                reservationTime
        );

        return new ReservationResponse(reservationRepository.insert(createReservationRequest));
    }

    @Transactional
    public ReservationResponse createReservation(final AdminReservationRequest adminReservationRequest) {
        ReservationDate reservationDate = new ReservationDate(adminReservationRequest.getDate());
        ReservationTime reservationTime = getReservationTime(adminReservationRequest.getTimeId());
        Theme theme = getTheme(adminReservationRequest.getThemeId());
        validateReservationDateTime(reservationDate, reservationTime);

        Member member = memberRepository.findById(adminReservationRequest.getMemberId())
                .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));

        CreateReservationRequest createReservationRequest = new CreateReservationRequest(
                member,
                theme,
                reservationDate,
                reservationTime
        );

        return new ReservationResponse(reservationRepository.insert(createReservationRequest));
    }

    public List<ReservationResponse> getReservations(Long memberId, Long themeId, LocalDate dateFrom,
                                                     LocalDate dateTo) {

        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom은 dateTo보다 이전이어야 합니다.");
        }

        return reservationRepository.findReservationsBy(memberId, themeId, dateFrom, dateTo).stream()
                .map(ReservationResponse::new)
                .toList();
    }

    @Transactional
    public void deleteReservation(final Long id) {
        if (reservationRepository.delete(id) == 0) {
            throw new IllegalStateException("이미 삭제되어 있는 리소스입니다.");
        }
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchElementException("예약 시간 정보를 찾을 수 없습니다."));
    }

    private Theme getTheme(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchElementException("테마 정보를 찾을 수 없습니다."));
    }

    private void validateReservationDateTime(ReservationDate reservationDate, ReservationTime reservationTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate.getReservationDate(),
                reservationTime.getStartAt());

        validateIsPast(reservationDateTime);
        validateIsDuplicate(reservationDateTime);
    }

    private static void validateIsPast(LocalDateTime reservationDateTime) {
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new DateTimeException("지난 일시에 대한 예약 생성은 불가능합니다.");
        }
    }

    private void validateIsDuplicate(LocalDateTime reservationDateTime) {
        if (reservationRepository.existsByDateTime(reservationDateTime)) {
            throw new IllegalStateException("중복된 일시의 예약은 불가능합니다.");
        }
    }
}
