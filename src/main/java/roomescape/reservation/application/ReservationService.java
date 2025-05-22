package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.application.dto.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.reservation.application.dto.AdminReservationRequest;
import roomescape.reservation.application.dto.AdminReservationSearchRequest;
import roomescape.reservation.application.dto.MyReservationResponse;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.application.dto.UserReservationRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.exception.ReservationAlreadyExistsException;
import roomescape.reservation.exception.ReservationInPastException;
import roomescape.reservationTime.application.dto.TimeResponse;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.reservationTime.exception.TimeNotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.domain.WaitingWithRank;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public List<MyReservationResponse> findAllByMemberId(final Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);
        List<WaitingWithRank> waitings = waitingRepository.findWithRankByMemberId(memberId).stream().toList();
        return MyReservationResponse.of(reservations, waitings);
    }

    public List<ReservationResponse> findFiltered(
            final AdminReservationSearchRequest adminReservationSearchRequest
    ) {
        Long memberId = adminReservationSearchRequest.memberId();
        Long themeId = adminReservationSearchRequest.themeId();
        LocalDate from = adminReservationSearchRequest.from();
        LocalDate to = adminReservationSearchRequest.to();

        return reservationRepository.findFiltered(memberId, themeId, from, to)
                .stream().map(reservation -> new ReservationResponse(
                        reservation.getId(),
                        MemberResponse.from(reservation.getMember()),
                        ThemeResponse.from(reservation.getTheme()),
                        reservation.getDate(),
                        TimeResponse.from(reservation.getTime()))
                )
                .toList();
    }

    public ReservationResponse createByUser(final Long memberId, final UserReservationRequest request) {
        return create(memberId, request.date(), request.timeId(), request.themeId());
    }

    public ReservationResponse createByAdmin(final AdminReservationRequest request) {
        return create(request.memberId(), request.date(), request.timeId(), request.themeId());
    }

    private ReservationResponse create(Long memberId, LocalDate dateInput, Long timeId, Long themeId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        ReservationDate date = new ReservationDate(dateInput);
        ReservationTime time = timeRepository.findById(timeId).orElseThrow(TimeNotFoundException::new);
        validateInPast(date, time);

        Theme theme = themeRepository.findById(themeId).orElseThrow(ThemeNotFoundException::new);

        ReservationSpec spec = new ReservationSpec(date, time, theme);
        validateDuplicated(spec);

        Reservation reservation = new Reservation(member, spec);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    private void validateDuplicated(ReservationSpec spec) {
        if (reservationRepository.existsBySpec(spec)) {
            throw new ReservationAlreadyExistsException();
        }
    }

    private void validateInPast(ReservationDate date, ReservationTime time) {
        if (date.isInPast() || date.isToday() && time.isBeforeNow()) {
            throw new ReservationInPastException();
        }
    }

    public void deleteById(final Long id) {
        reservationRepository.deleteById(id);
    }
}
