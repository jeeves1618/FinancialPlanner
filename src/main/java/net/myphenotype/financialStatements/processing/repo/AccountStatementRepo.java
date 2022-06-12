package net.myphenotype.financialStatements.processing.repo;

import net.myphenotype.financialStatements.processing.entity.AccountStatement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountStatementRepo extends JpaRepository<AccountStatement, Integer> {

}
