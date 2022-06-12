package net.myphenotype.financialStatements.processing.service;

import lombok.extern.slf4j.Slf4j;
import net.myphenotype.financialStatements.processing.domain.BalanceSheet;
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
public class CashService {

    @Autowired
    private BalanceSheet balanceSheet;

    @Autowired
    private JournalsRepo journalsRepo;

    @Autowired
    private ChartService chartService;

    @Autowired
    private RupeeFormatter rf;

    @Autowired
    ChartOfAccountsRepo chartOfAccountsRepo;

    DecimalFormat ft = new DecimalFormat("Rs ##,##,##0.00");

    public List<BalanceSheet> getCashEntries(){
        List<BalanceSheet> balanceSheetList = new ArrayList<>();
        List<ChartOfAccounts> chartOfAccountsList = chartOfAccountsRepo.findByFinancialStatement("Cash Flow");
        int rowCount = 0;
        String[] formalaArray = {"a","b","c","b - c = d","e","f","g","h","a + d - e + f - g + h = i"};

        double cashFlowFromOperations = 0.00, endingCashBalance = 0.00;
        for (ChartOfAccounts chart: chartOfAccountsList){
            double creditsForAccount = 0.00, debitsForAccount = 0.00;
            List<Journals> journalsList = journalsRepo.findByAccountNumberAndJournalStatus(chart.getAccountNumber(),"Posted");
            for(Journals journals: journalsList){
                if (journals.getCreditDebitInd().equals("Credit")) creditsForAccount = creditsForAccount + journals.getJournalAmount();
                else debitsForAccount = debitsForAccount + journals.getJournalAmount();
            }
            balanceSheet.setFormulaString(formalaArray[rowCount]);
            if (chart.getAccountType().equals("Asset") || chart.getAccountType().equals("Expenditure")) {
                balanceSheet.setBalance(debitsForAccount - creditsForAccount);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(debitsForAccount - creditsForAccount)));
                switch (formalaArray[rowCount]){
                    case "a" :
                        endingCashBalance = endingCashBalance + (debitsForAccount - creditsForAccount);
                        break;
                    case "b" :
                        cashFlowFromOperations = cashFlowFromOperations + (debitsForAccount - creditsForAccount);
                        endingCashBalance = endingCashBalance + (debitsForAccount - creditsForAccount);
                        break;
                    case "f" :
                        endingCashBalance = endingCashBalance + (debitsForAccount - creditsForAccount);
                        break;
                    case "h" :
                        endingCashBalance = endingCashBalance + (debitsForAccount - creditsForAccount);
                        break;
                    default:
                        break;
                }
            } else {
                balanceSheet.setBalance(creditsForAccount - debitsForAccount);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(creditsForAccount - debitsForAccount)));
                switch (formalaArray[rowCount]){
                    case "c" :
                        cashFlowFromOperations = cashFlowFromOperations - (creditsForAccount - debitsForAccount);
                        endingCashBalance = endingCashBalance - (creditsForAccount - debitsForAccount);
                        break;
                    case "e" :
                        endingCashBalance = endingCashBalance - (creditsForAccount - debitsForAccount);
                        break;
                    case "g" :
                        endingCashBalance = endingCashBalance - (creditsForAccount - debitsForAccount);
                        break;
                    default:
                        break;
                }
            }
            if (formalaArray[rowCount].length() == 1)
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            else if (formalaArray[rowCount].equals("b - c = d")) {
                balanceSheet.setBalance(cashFlowFromOperations);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(cashFlowFromOperations)));
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            }
            else if (formalaArray[rowCount].equals("a + d - e + f - g + h = i")) {
                balanceSheet.setBalance(endingCashBalance);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(endingCashBalance)));
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            }
            else
                balanceSheet.setAccountTitle("Unknown Entry");

            balanceSheetList.add(balanceSheet);
            balanceSheet = new BalanceSheet();
            rowCount++;
        }
        return balanceSheetList;
    }
}
