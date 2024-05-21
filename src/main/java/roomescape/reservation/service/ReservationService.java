package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.exceptions.DuplicationException;
import roomescape.exceptions.ValidationException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberRequest;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.MemberReservationWaitingResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository ReservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = ReservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse addReservation(
            ReservationRequest reservationRequest,
            MemberRequest memberRequest
    ) {
        ReservationTime reservationTime = reservationTimeRepository.getById(reservationRequest.timeId());
        Theme theme = themeRepository.getById(reservationRequest.themeId());

        Reservation reservation = new Reservation(
                reservationRequest.date(),
                reservationTime,
                theme,
                memberRequest.toMember()
        );
        validateIsBeforeNow(reservation);
        validateIsDuplicated(reservation);

        return new ReservationResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse addReservation(AdminReservationRequest adminReservationRequest) {
        Member member = memberRepository.getById(adminReservationRequest.memberId());
        ReservationTime reservationTime = reservationTimeRepository.getById(adminReservationRequest.timeId());
        Theme theme = themeRepository.getById(adminReservationRequest.themeId());

        Reservation reservation = new Reservation(
                adminReservationRequest.date(),
                reservationTime,
                theme,
                member
        );
        validateIsBeforeNow(reservation);
        validateIsDuplicated(reservation);

        return new ReservationResponse(reservationRepository.save(reservation));
    }

    private void validateIsBeforeNow(Reservation reservation) {
        if (reservation.isBeforeNow()) {
            throw new ValidationException("과거 시간은 예약할 수 없습니다.");
        }
    }

    private void validateIsDuplicated(Reservation reservation) {
        if (reservationRepository.existsByDateAndReservationTimeAndTheme(reservation.getDate(),
                reservation.getReservationTime(), reservation.getTheme())) {
            throw new DuplicationException("이미 예약이 존재합니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> searchReservations(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        Theme theme = themeRepository.getById(themeId);
        Member member = memberRepository.getById(memberId);
        return reservationRepository.findByThemeAndMember(theme, member)
                .stream()
                .filter(reservation -> reservation.isBetweenInclusive(dateFrom, dateTo))
                .map(ReservationResponse::new)
                .toList();
    }

    public List<MemberReservationWaitingResponse> findReservationsByMember(Member member) {
        return reservationRepository.findByMember(member)
                .stream()
                .map(MemberReservationWaitingResponse::new)
                .toList();
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }
}
