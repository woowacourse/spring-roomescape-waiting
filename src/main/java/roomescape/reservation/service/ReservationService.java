package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.dto.request.AdminReservationSaveRequest;
import roomescape.reservation.controller.dto.request.ReservationSaveRequest;
import roomescape.reservation.controller.dto.response.MemberReservationResponse;
import roomescape.reservation.controller.dto.response.ReservationDeleteResponse;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberService memberService;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeService reservationTimeService,
                              final ThemeService themeService,
                              final MemberService memberService) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.memberService = memberService;
    }

    public ReservationResponse save(final ReservationSaveRequest saveRequest, final Member member) {
        ReservationTime reservationTime = reservationTimeService.getById(saveRequest.timeId());
        Theme theme = themeService.getById(saveRequest.themeId());
        validateDuplicateReservation(saveRequest);

        Reservation reservation = saveRequest.toEntity(member, reservationTime, theme, Status.RESERVATION);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    private void validateDuplicateReservation(ReservationSaveRequest saveRequest) {
        if (hasDuplicateReservation(saveRequest.date(), saveRequest.timeId(), saveRequest.themeId())) {
            throw new IllegalArgumentException("[ERROR] 중복된 예약이 존재합니다.");
        }
    }

    private boolean hasDuplicateReservation(final LocalDate date, final long timeId, final long themeId) {
        return !reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId).isEmpty();
    }

    public ReservationResponse save(final AdminReservationSaveRequest adminReservationSaveRequest) {
        Member member = memberService.getById(adminReservationSaveRequest.memberId());
        return save(adminReservationSaveRequest.toReservationSaveRequest(), member);
    }

    public List<ReservationResponse> getAll() {
        return StreamSupport.stream(reservationRepository.findAll().spliterator(), false)
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findByFilter(final Long memberId, final Long themeId,
                                                  final LocalDate dateFrom, final LocalDate dateTo) {
        return reservationRepository.findByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MemberReservationResponse> findByMemberId(final long memberId) {
        return reservationRepository.findByMemberId(memberId)
                .stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    public ReservationDeleteResponse delete(final long id) {
        validateNotExitsReservationById(id);
        return new ReservationDeleteResponse(reservationRepository.deleteById(id));
    }

    private void validateNotExitsReservationById(final long id) {
        if (reservationRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException("[ERROR] (themeId : " + id + ") 에 대한 예약이 존재하지 않습니다.");
        }
    }
}
