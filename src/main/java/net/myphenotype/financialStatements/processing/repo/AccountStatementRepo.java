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

    @Query("select COALESCE(sum(a.withdrawalAmount),0) as totalWithdrawals from AccountStatement a where a.entryCategory = ?1 ")
    public double findWithdrawalSumByCategory(String Category);

    @Query("select COALESCE(sum(a.depositAmount),0) as totalDeposits from AccountStatement a where a.entryCategory = ?1 ")
    public double findDepositSumByCategory(String Category);

    @Query("select COALESCE(sum(a.withdrawalAmount),0) as totalWithdrawals from AccountStatement a where a.entryCategory not in ?1")
    public double findWithdrawalTotals(List<String> nonTranCategories);

    @Query("select COALESCE(sum(a.depositAmount),0) as totalDeposits from AccountStatement a where a.entryCategory not in ?1")
    public double findDepositTotals(List<String> nonTranCategories);

    @Query("select COALESCE(sum(a.withdrawalAmount),0) as totalWithdrawals from AccountStatement a where a.discretionarySpendingIndicator = ?1 and a.entryCategory not in ?2 ")
    public double findWithdrawalSumByDisc(String discretionaryInd, List<String> nonTranCategories);
}
