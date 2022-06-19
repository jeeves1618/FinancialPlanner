package net.myphenotype.financialStatements.processing.repo;

import net.myphenotype.financialStatements.processing.entity.AccountStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.HashMap;
import java.util.List;

public interface AccountStatementRepo extends JpaRepository<AccountStatement, Integer> {

    @Query("select a from AccountStatement a where a.entryCategory = ?1 ")
    public List<AccountStatement> findEntriesByCategory(String Category);

    @Query("select a.entryCategory, sum(a.withdrawalAmount) as totalWithdrawals from AccountStatement a where a.entryCategory = ?1 group by a.entryCategory")
    public HashMap<String,Double> findSumByCategory(String Category);

    @Query("select COALESCE(sum(a.withdrawalAmount + a.depositAmount),0) as totalWithdrawals from AccountStatement a where a.entryCategory = ?1 ")
    public double findWithdrawalSumByCategory(String Category);

}
