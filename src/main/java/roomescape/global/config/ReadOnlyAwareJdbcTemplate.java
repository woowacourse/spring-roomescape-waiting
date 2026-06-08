package roomescape.global.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

public class ReadOnlyAwareJdbcTemplate extends JdbcTemplate {

    public ReadOnlyAwareJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    private void rejectIfReadOnly() {
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            throw new ReadOnlyViolationException(
                    "읽기 전용 트랜잭션에서는 쓰기 작업을 수행할 수 없습니다.");
        }
    }

    @Override
    public int update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder) {
        rejectIfReadOnly();
        return super.update(psc, generatedKeyHolder);
    }

    @Override
    public int update(String sql, Object... args) {
        rejectIfReadOnly();
        return super.update(sql, args);
    }

    @Override
    public int update(String sql) {
        rejectIfReadOnly();
        return super.update(sql);
    }

    @Override
    public int update(String sql, PreparedStatementSetter pss) {
        rejectIfReadOnly();
        return super.update(sql, pss);
    }

    @Override
    public int update(PreparedStatementCreator psc) {
        rejectIfReadOnly();
        return super.update(psc);
    }

    @Override
    public int update(String sql, Object[] args, int[] argTypes) {
        rejectIfReadOnly();
        return super.update(sql, args, argTypes);
    }
}
