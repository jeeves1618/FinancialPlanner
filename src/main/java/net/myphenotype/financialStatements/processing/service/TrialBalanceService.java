package net.myphenotype.financialStatements.processing.service;

import lombok.extern.slf4j.Slf4j;
import net.myphenotype.financialStatements.processing.domain.ChartResponsePayload;
import net.myphenotype.financialStatements.processing.domain.Ledger;
import net.myphenotype.financialStatements.processing.domain.LedgerSummary;
import net.myphenotype.financialStatements.processing.domain.TrialBalance;
import net.myphenotype.financialStatements.processing.entity.ChartOfAccounts;
import net.myphenotype.financialStatements.processing.entity.Journals;
import net.myphenotype.financialStatements.processing.repo.ChartOfAccountsRepo;
import net.myphenotype.financialStatements.processing.repo.JournalsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TrialBalanceService {

    @Autowired
    private TrialBalance trialBalance;

    @Autowired
    private JournalsRepo journalsRepo;

    @Autowired
    private ChartService chartService;

    @Autowired
    private RupeeFormatter rf;

    @Autowired
    ChartOfAccountsRepo chartOfAccountsRepo;

    DecimalFormat ft = new DecimalFormat("Rs ##,##,##0.00");

    public List<TrialBalance> getTrialBalanceEntries(){
        List<TrialBalance> trialBalances = new ArrayList<>();
        List<ChartOfAccounts> chartOfAccountsList = chartOfAccountsRepo.findByFinancialStatementNot("Cash Flow");
        double creditTotal = 0.00, debitTotal = 0.00;
        for (ChartOfAccounts chart: chartOfAccountsList){
            double creditsForAccount = 0.00, debitsForAccount = 0.00;
            List<Journals> journalsList = journalsRepo.findByAccountNumberAndJournalStatus(chart.getAccountNumber(),"Balanced");
            for(Journals journals: journalsList){
                if (journals.getCreditDebitInd().equals("Credit")) creditsForAccount = creditsForAccount + journals.getJournalAmount();
                else debitsForAccount = debitsForAccount + journals.getJournalAmount();
            }
            trialBalance.setAccountTitle(chart.getAccountDescription());
            if (creditsForAccount > debitsForAccount) {
                creditsForAccount = creditsForAccount - debitsForAccount;
                debitsForAccount = 0.00;
            } else {
                debitsForAccount = debitsForAccount - creditsForAccount;
                creditsForAccount = 0.00;
            }
            trialBalance.setCreditAmountFmtd(rf.formattedRupee(ft.format(creditsForAccount)));
            trialBalance.setDebitAmountFmtd(rf.formattedRupee(ft.format(debitsForAccount)));
            creditTotal = creditTotal + creditsForAccount;
            debitTotal = debitTotal + debitsForAccount;
            trialBalances.add(trialBalance);
            trialBalance = new TrialBalance();
        }
        trialBalance = new TrialBalance();
        trialBalance.setAccountTitle("  Totals");
        trialBalance.setCreditAmountFmtd(rf.formattedRupee(ft.format(creditTotal)));
        trialBalance.setDebitAmountFmtd(rf.formattedRupee(ft.format(debitTotal)));
        trialBalances.add(trialBalance);
        return trialBalances;
    }
}
