package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.controller.dto.request.AdminReservationSaveRequest;
import roomescape.reservation.controller.dto.request.ReservationSaveRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

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

    public Reservation save(final ReservationSaveRequest saveRequest, final Member member) {
        ReservationTime reservationTime = reservationTimeRepository.getById(saveRequest.timeId());
        Theme theme = themeRepository.getById(saveRequest.themeId());
        validateDuplicateReservation(saveRequest);

        Reservation reservation = saveRequest.toEntity(member, reservationTime, theme, Status.RESERVATION);
        return reservationRepository.save(reservation);
    }

    private void validateDuplicateReservation(ReservationSaveRequest saveRequest) {
        if (hasDuplicateReservation(saveRequest.date(), saveRequest.timeId(), saveRequest.themeId())) {
            throw new IllegalArgumentException("[ERROR] 중복된 예약이 존재합니다.");
        }
    }

    private boolean hasDuplicateReservation(final LocalDate date, final long timeId, final long themeId) {
        return !reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId).isEmpty();
    }

    public Reservation save(final AdminReservationSaveRequest adminReservationSaveRequest) {
        Member member = memberRepository.getById(adminReservationSaveRequest.memberId());
        return save(adminReservationSaveRequest.toReservationSaveRequest(), member);
    }

    public List<Reservation> getAll() {
        return StreamSupport.stream(reservationRepository.findAll().spliterator(), false).toList();
    }

    public List<Reservation> findByFilter(final Long memberId, final Long themeId,
                                          final LocalDate dateFrom, final LocalDate dateTo) {
        return reservationRepository.findByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo);
    }

    public List<Reservation> findByMemberId(final long memberId) {
        return reservationRepository.findByMemberId(memberId);
    }

    public int delete(final long id) {
        validateNotExitsReservationById(id);
        return reservationRepository.deleteById(id);
    }

    private void validateNotExitsReservationById(final long id) {
        if (reservationRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException("[ERROR] (themeId : " + id + ") 에 대한 예약이 존재하지 않습니다.");
        }
    }
}
