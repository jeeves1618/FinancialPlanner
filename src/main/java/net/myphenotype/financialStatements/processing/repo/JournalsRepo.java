package net.myphenotype.financialStatements.processing.repo;

import net.myphenotype.financialStatements.processing.entity.Journals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JournalsRepo extends JpaRepository<Journals,Integer> {

    public List<Journals> findByJournalsRelKey(Integer journalsRelKey);

    @Query("select j from Journals j, ChartOfAccounts c where j.accountNumber = c.accountNumber and j.journalsRelKey = ?1 and c.financialStatement != ?2 ")
    public List<Journals> findNonStatementJournals(Integer journalsRelKey, String Statement);

    @Query("select j from Journals j, ChartOfAccounts c where j.accountNumber = c.accountNumber and c.financialStatement != ?1 order by j.journalsKey")
    public List<Journals> findNonCashJournals(String Statement);

    @Query("select j from Journals j, ChartOfAccounts c where j.accountNumber = c.accountNumber and j.journalsRelKey = ?1 and c.financialStatement = ?2 ")
    public List<Journals> findJournalsByStatement(Integer journalsRelKey, String Statement);

    public List<Journals> findByJournalStatus(String journalStatus);

    public List<Journals> findByJournalStatusOrderByJournalsRelKeyAsc(String journalStatus);

    public List<Journals> findByJournalStatusOrderByAccountNumberAsc(String journalStatus);

    public List<Journals> findByAccountNumberAndJournalStatus(String accountNumber, String journalStatus);
}
