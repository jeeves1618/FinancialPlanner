package net.myphenotype.financialStatements.processing.service;

import lombok.extern.slf4j.Slf4j;
import net.myphenotype.financialStatements.processing.domain.BalanceSheet;
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
public class BalanceSheetService {

    @Autowired
    private BalanceSheet balanceSheet;

    @Autowired
    private JournalsRepo journalsRepo;

    @Autowired
    private ChartService chartService;

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private RupeeFormatter rf;

    @Autowired
    ChartOfAccountsRepo chartOfAccountsRepo;

    DecimalFormat ft = new DecimalFormat("Rs ##,##,##0.00");

    public List<BalanceSheet> getBalanceSheetEntries(){
        List<BalanceSheet> balanceSheetList = new ArrayList<>();
        List<ChartOfAccounts> chartOfAccountsList = chartOfAccountsRepo.findByFinancialStatement("Balance Sheet");
        int rowCount = 0;
        String[] formalaArray = {"A","B","C","D","A + B + C + D = E","F","G","H","G - H = I","E + F + I = J",
                "K","L","M","N","K + L + M + N = O","P","Q","R","Q + R = S","O + P + S = T"};

        double creditTotal = 0.00, retainedEarnings = 0.00, currentAssets = 0.00, netFixedAssets = 0.00,
                otherAssets = 0.00, currentLiabilities = 0.00, longTermDebt = 0.00, shareHolderEquity = 0.00;
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
                    case "A" :
                    case "B" :
                    case "C" :
                    case "D" :
                        currentAssets = currentAssets + debitsForAccount - creditsForAccount;
                        break;
                    case "F" :
                        otherAssets = otherAssets + debitsForAccount - creditsForAccount;
                        break;
                    case "G" :
                        netFixedAssets = netFixedAssets + debitsForAccount - creditsForAccount;
                        break;
                    case "H" :
                        netFixedAssets = netFixedAssets - (debitsForAccount - creditsForAccount);
                        break;
                    default:
                        break;
                }
            } else {
                balanceSheet.setBalance(creditsForAccount - debitsForAccount);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(creditsForAccount - debitsForAccount)));
                switch (formalaArray[rowCount]){
                    case "K" :
                    case "L" :
                    case "M" :
                    case "N" :
                        currentLiabilities = currentLiabilities + creditsForAccount - debitsForAccount;
                        break;
                    case "P" :
                        longTermDebt = longTermDebt + creditsForAccount - debitsForAccount;
                        break;
                    case "Q" :
                        shareHolderEquity = shareHolderEquity + creditsForAccount - debitsForAccount;
                        break;
                    case "R" :
                        retainedEarnings = retainedEarnings + getBottomLine();
                        break;
                    default:
                        break;
                }
            }
            if (formalaArray[rowCount].length() == 1 && !formalaArray[rowCount].equals("R"))
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            else if (formalaArray[rowCount].equals("R")){
                balanceSheet.setBalance(retainedEarnings);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(retainedEarnings)));
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            }
            else if (formalaArray[rowCount].equals("A + B + C + D = E")) {
                balanceSheet.setBalance(currentAssets);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(currentAssets)));
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            }
            else if (formalaArray[rowCount].equals("G - H = I")) {
                balanceSheet.setBalance(netFixedAssets);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(netFixedAssets)));
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            }
            else if (formalaArray[rowCount].equals("E + F + I = J"))
            {
                balanceSheet.setBalance(otherAssets + netFixedAssets +currentAssets);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(otherAssets + netFixedAssets +currentAssets)));
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            }
            else if (formalaArray[rowCount].equals("K + L + M + N = O")){
                balanceSheet.setBalance(currentLiabilities);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(currentLiabilities)));
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            }
            else if (formalaArray[rowCount].equals("Q + R = S")){
                balanceSheet.setBalance(shareHolderEquity + retainedEarnings);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(shareHolderEquity + retainedEarnings)));
                balanceSheet.setAccountTitle(chart.getAccountDescription());
            }
            else if (formalaArray[rowCount].equals("O + P + S = T")){
                balanceSheet.setBalance(shareHolderEquity + currentLiabilities + longTermDebt + + retainedEarnings);
                balanceSheet.setBalanceFmtd(rf.formattedRupee(ft.format(shareHolderEquity + currentLiabilities + longTermDebt + retainedEarnings)));
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

    private double getBottomLine(){
        List<BalanceSheet> incomeStatement = incomeService.getIncomeEntries();
        double netIncome = 0.00;
        for(BalanceSheet entry:incomeStatement){
            switch (entry.getFormulaString()){
                case "8 + 9 - 10 = 11" :
                    netIncome = entry.getBalance();
                    break;
                default:
                    break;
            }

        }
        return netIncome;
    }
}
