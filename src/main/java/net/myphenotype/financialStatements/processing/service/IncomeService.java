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
public class IncomeService {

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

    public List<BalanceSheet> getIncomeEntries(){
        List<BalanceSheet> balanceSheetList = new ArrayList<>();
        List<ChartOfAccounts> chartOfAccountsList = chartOfAccountsRepo.findByFinancialStatement("Income Statement");
        int rowCount = 0;
        String[] formalaArray = {"1","2","1 - 2 = 3","4","5","6","4 + 5 + 6 = 7","3 - 7 = 8","9","10","8 + 9 - 10 = 11"};

        double grossMargin = 0.00, operatingExpenses = 0.00, interestIncome = 0.00, incomeTaxes = 0.00;
        for (ChartOfAccounts chart: chartOfAccountsList){
            double creditsForAccount = 0.00, debitsForAccount = 0.00;
            List<Journals> journalsList = journalsRepo.findByAccountNumberAndJournalStatus(chart.getAccountNumber(),"Balanced");
            for(Journals journals: journalsList){
                if (journals.getCreditDebitInd().equals("Credit")) creditsForAccount = creditsForAccount + journals.getJournalAmount();
                else debitsForAccount = debitsForAccount + journals.getJournalAmount();
            }
            balanceSheet.setFormulaString(formalaArray[rowCount]);
            if (chart.getAccountType().equals("Asset") || chart.getAccountType().equals("Expenditure")) {
                balanceSheet.setBalance(debitsForAccount - creditsForAccount);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(debitsForAccount - creditsForAccount)));
                switch (formalaArray[rowCount]){
                    case "1" :
                    case "2" :
                        grossMargin = grossMargin - (debitsForAccount - creditsForAccount);
                        break;
                    case "4" :
                    case "5" :
                    case "6" :
                        operatingExpenses = operatingExpenses + debitsForAccount - creditsForAccount;
                        break;
                    case "9" :
                        interestIncome = interestIncome - debitsForAccount + creditsForAccount;
                        log.info("Interest Income : " + interestIncome);
                        break;
                    case "10" :
                        incomeTaxes = incomeTaxes - (debitsForAccount - creditsForAccount);
                        break;
                    default:
                        break;
                }
            } else {
                balanceSheet.setBalance(creditsForAccount - debitsForAccount);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(creditsForAccount - debitsForAccount)));
                switch (formalaArray[rowCount]){
                    case "1" :
                    case "2" :
                        grossMargin = grossMargin + creditsForAccount - debitsForAccount;
                        break;
                    case "9" :
                        interestIncome = interestIncome - debitsForAccount + creditsForAccount;
                        log.info("Interest Income : " + interestIncome);
                        break;
                    default:
                        break;
                }
            }
            if (formalaArray[rowCount].length() <= 2)
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            else if (formalaArray[rowCount].equals("1 - 2 = 3")) {
                balanceSheet.setBalance(grossMargin);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(grossMargin)));
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            }
            else if (formalaArray[rowCount].equals("4 + 5 + 6 = 7")) {
                balanceSheet.setBalance(operatingExpenses);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(operatingExpenses)));
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            }
            else if (formalaArray[rowCount].equals("3 - 7 = 8"))
            {
                balanceSheet.setBalance(grossMargin - operatingExpenses);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(grossMargin - operatingExpenses)));
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            }
            else if (formalaArray[rowCount].equals("8 + 9 - 10 = 11")){
                balanceSheet.setBalance(grossMargin - operatingExpenses + interestIncome -incomeTaxes);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(grossMargin - operatingExpenses + interestIncome -incomeTaxes)));
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
