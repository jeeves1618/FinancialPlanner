package net.myphenotype.financialStatements.processing.repo;

import net.myphenotype.financialStatements.processing.entity.ChartOfAccounts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChartOfAccountsRepo extends JpaRepository<ChartOfAccounts, Integer> {

    public ChartOfAccounts findByAccountNumber(String accountNumber);

    public List<ChartOfAccounts> findByFinancialStatement(String financialStatement);

    public List<ChartOfAccounts> findByFinancialStatementNot(String financialStatement);
}
