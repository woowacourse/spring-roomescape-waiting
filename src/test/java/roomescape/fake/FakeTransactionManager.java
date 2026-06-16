package roomescape.fake;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

// 단위 테스트용 no-op 트랜잭션 매니저: 실제 트랜잭션 없이 콜백만 실행되게 한다.
public class FakeTransactionManager implements PlatformTransactionManager {

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) {
        return new SimpleTransactionStatus();
    }

    @Override
    public void commit(TransactionStatus status) {
    }

    @Override
    public void rollback(TransactionStatus status) {
    }
}
