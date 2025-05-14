package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.InvalidIdException;
import roomescape.common.exception.InvalidTimeException;
import roomescape.common.exception.message.IdExceptionMessage;
import roomescape.common.exception.message.ReservationExceptionMessage;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.admin.AdminReservationRequest;
import roomescape.reservation.dto.admin.AdminReservationSearchRequest;
import roomescape.reservation.dto.user.UserReservationRequest;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.reservationTime.dto.admin.ReservationTimeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.dto.ThemeResponse;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository, ReservationTimeRepository timeRepository,
                              ThemeRepository themeRepository, MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(reservation -> new ReservationResponse(
                        reservation.getId(),
                        MemberResponse.from(reservation.getMember()),
                        ThemeResponse.from(reservation.getTheme()),
                        reservation.getDate(),
                        ReservationTimeResponse.from(reservation.getTime()))
                )
                .toList();
    }

    public List<MyReservationResponse> findAllByMemberId(final Long memberId) {
        return reservationRepository.findAllByMemberId(memberId).stream()
                .map(reservation -> new MyReservationResponse(
                        reservation.getId(),
                        reservation.getTheme().getName(),
                        reservation.getDate(),
                        reservation.getTime().getStartAt(),
                        "예약")
                )
                .toList();
    }

    public List<ReservationResponse> findAllByMemberAndThemeAndDate(
            final AdminReservationSearchRequest adminReservationSearchRequest
    ) {
        Long memberId = adminReservationSearchRequest.memberId();
        Long themeId = adminReservationSearchRequest.themeId();
        LocalDate dateFrom = adminReservationSearchRequest.dateFrom();
        LocalDate dateTo = adminReservationSearchRequest.dateTo();

        return reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(memberId, themeId, dateFrom, dateTo)
                .stream().map(reservation -> new ReservationResponse(
                        reservation.getId(),
                        MemberResponse.from(reservation.getMember()),
                        ThemeResponse.from(reservation.getTheme()),
                        reservation.getDate(),
                        ReservationTimeResponse.from(reservation.getTime()))
                )
                .toList();
    }

    public ReservationResponse add(final Long memberId, final UserReservationRequest userReservationRequest) {
        Member memberResult = searchMember(memberId);
        ReservationTime reservationTimeResult = searchReservationTime(userReservationRequest.timeId());
        validateTime(userReservationRequest.date(), reservationTimeResult);
        validateAvailability(userReservationRequest.date(), reservationTimeResult);
        Theme themeResult = searchTheme(userReservationRequest.themeId());

        Reservation newReservation = new Reservation(
                memberResult,
                userReservationRequest.date(),
                reservationTimeResult,
                themeResult
        );
        Reservation savedReservation = reservationRepository.save(newReservation);

        return new ReservationResponse(
                savedReservation.getId(),
                MemberResponse.from(savedReservation.getMember()),
                ThemeResponse.from(savedReservation.getTheme()),
                savedReservation.getDate(),
                ReservationTimeResponse.from(savedReservation.getTime())
        );
    }

    public ReservationResponse addByAdmin(final AdminReservationRequest adminReservationRequest) {
        Member memberResult = searchMember(adminReservationRequest.memberId());
        ReservationTime reservationTimeResult = searchReservationTime(adminReservationRequest.timeId());
        validateTime(adminReservationRequest.date(), reservationTimeResult);
        validateAvailability(adminReservationRequest.date(), reservationTimeResult);
        Theme themeResult = searchTheme(adminReservationRequest.themeId());

        Reservation newReservation = new Reservation(
                memberResult,
                adminReservationRequest.date(),
                reservationTimeResult,
                themeResult
        );
        Reservation savedReservation = reservationRepository.save(newReservation);

        return new ReservationResponse(
                savedReservation.getId(),
                MemberResponse.from(savedReservation.getMember()),
                ThemeResponse.from(savedReservation.getTheme()),
                savedReservation.getDate(),
                ReservationTimeResponse.from(savedReservation.getTime())
        );
    }

    private void validateTime(final LocalDate reservationDate, final ReservationTime reservationTimeResult) {
        if (reservationDate.isEqual(LocalDate.now())
                && reservationTimeResult.getStartAt().isBefore(LocalTime.now())) {
            throw new InvalidTimeException(ReservationExceptionMessage.TIME_BEFORE_NOW.getMessage());
        }
    }

    private void validateAvailability(
            final LocalDate reservationDate,
            final ReservationTime reservationTimeResult
    ) {
        boolean isDuplicate = reservationRepository.existsByDateAndTimeId(reservationDate,
                reservationTimeResult.getId());

        if (isDuplicate) {
            throw new DuplicateException(ReservationExceptionMessage.DUPLICATE_RESERVATION.getMessage());
        }
    }

    public void deleteById(final Long id) {
        searchReservation(id);
        reservationRepository.deleteById(id);
    }

    private Reservation searchReservation(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_RESERVATION_ID.getMessage()));
    }

    private Member searchMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_MEMBER_ID.getMessage()));
    }

    private ReservationTime searchReservationTime(final Long timeId) {
        return timeRepository.findById(timeId)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_TIME_ID.getMessage()));
    }

    private Theme searchTheme(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_THEME_ID.getMessage()));
    }
}
