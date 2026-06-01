package roomescape.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import roomescape.domain.Waitlist;

@Component
public class WaitlistOrderPolicy {

    public int calculateOrder(Waitlist target, List<Waitlist> sameSlotWaitlists) {
        List<Waitlist> sortedWaitlists = sameSlotWaitlists.stream()
                .sorted(Comparator.comparing(Waitlist::getCreatedAt).thenComparing(Waitlist::getId))
                .toList();

        for (int i = 0; i < sortedWaitlists.size(); i++) {
            Waitlist waitlist = sortedWaitlists.get(i);

            if (waitlist.getId().equals(target.getId())) {
                return i + 1;
            }
        }

        throw new IllegalArgumentException("대기 목록에 대상 대기가 없습니다.");
    }
}
