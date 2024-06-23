package net.myphenotype.financialStatements.processing.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.myphenotype.financialStatements.processing.domain.NlpCategory;
import net.myphenotype.financialStatements.processing.domain.TimeLineData;
import net.myphenotype.financialStatements.processing.entity.AccountStatement;
import net.myphenotype.financialStatements.processing.entity.NlpEntry;
import net.myphenotype.financialStatements.processing.repo.AccountStatementRepo;
import net.myphenotype.financialStatements.processing.repo.NlpEntriesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Data
@Slf4j
@PropertySource("classpath:accounts.properties")
public class ExpAnalysisService {

    @Autowired
    AccountStatement accountStatement;

    @Autowired
    AccountStatementRepo accountStatementRepo;

    @Autowired
    NlpEntriesRepo nlpEntriesRepo;

    @Autowired
    NlpCategory nlpCategory;

    @Autowired
    TimeLineData timeLineData;

    @Autowired
    private RupeeFormatter rf;

    @Value("${one.income.account}")
    private String oneIncomeAccount;

    @Value("${two.income.account}")
    private String twoIncomeAccount;

    DecimalFormat ft = new DecimalFormat("Rs ##,##,##0.00");
    DecimalFormat df = new DecimalFormat("#0.00%");
    ArrayList<AccountStatement> AccountStatementList = new ArrayList<AccountStatement>();

    int monthsBetween = 0;

    String accountNumber = null;

    public List<NlpCategory> getSavingsEntry(String category){
        monthsBetween = getTimeLine().getMonthsBetween();
        List<NlpCategory> NlpCategorizedSavingsList = new ArrayList<>();
        int sNoSaving = 1;
        List<String> accountNumbers = Arrays.asList(twoIncomeAccount,oneIncomeAccount);
        nlpCategory.setEntryCategory(category);
        nlpCategory.setAnnualWithdrawalAmount(accountStatementRepo.findSavingsSumByCategory(category, accountNumbers));
        nlpCategory.setMonthlyWithdrawalAmount(nlpCategory.getAnnualWithdrawalAmount()/monthsBetween);
        nlpCategory.setMonthlyWithdrawalAmountFmtd(rf.formattedRupee(ft.format(nlpCategory.getMonthlyWithdrawalAmount())));
        nlpCategory.setAnnualWithdrawalAmountFmtd(rf.formattedRupee(ft.format(nlpCategory.getAnnualWithdrawalAmount()/monthsBetween*12.0)));
        nlpCategory.setAnnualDepositAmount(accountStatementRepo.findDepositSumByCategory(category));
        nlpCategory.setMonthlyDepositAmount(nlpCategory.getAnnualDepositAmount()/monthsBetween);
        nlpCategory.setMonthlyDepositAmountFmtd(rf.formattedRupee(ft.format(nlpCategory.getMonthlyDepositAmount())));
        nlpCategory.setAnnualDepositAmountFmtd(rf.formattedRupee(ft.format(nlpCategory.getAnnualDepositAmount())));
        log.info("Zero Amount Debug: " + rf.formattedRupee(ft.format(nlpCategory.getAnnualDepositAmount())));
        List<String> discretionaryInd = nlpEntriesRepo.findDiscretionary(category);
        nlpCategory.setDiscretionaryIndicator(discretionaryInd.get(0));
        nlpCategory.setPercentOfTotal(1);
        nlpCategory.setPercentOfTotalFmtd(df.format(nlpCategory.getPercentOfTotal()));
        nlpCategory.setSerialNumber(sNoSaving);
        NlpCategorizedSavingsList.add(nlpCategory);
        return NlpCategorizedSavingsList;
    }

    public List<NlpCategory> getUniqueEntries(String typeOfEntries) {
        monthsBetween = getTimeLine().getMonthsBetween();
        int sNoWithdrawal = 1, sNoDeposit=1;
        List<NlpCategory> NlpCategorizedWithdrawalList = new ArrayList<>();
        List<NlpCategory> NlpCategorizedDepositList = new ArrayList<>();
        List<String> nonTranCategories = Arrays.asList("Bookentries","Savings","Reversal","Capital Market Transactions");
        double totalWithdrawals = accountStatementRepo.findWithdrawalTotals(nonTranCategories), totalDeposits = accountStatementRepo.findDepositTotals(nonTranCategories);
        List<String> nlpCategories = nlpEntriesRepo.findUniqueEntries();
        for (String category: nlpCategories)
        {

            nlpCategory.setEntryCategory(category);
            nlpCategory.setAnnualWithdrawalAmount(accountStatementRepo.findWithdrawalSumByCategory(category));
            nlpCategory.setMonthlyWithdrawalAmount(nlpCategory.getAnnualWithdrawalAmount()/monthsBetween);
            nlpCategory.setMonthlyWithdrawalAmountFmtd(rf.formattedRupee(ft.format(nlpCategory.getMonthlyWithdrawalAmount())));
            nlpCategory.setAnnualWithdrawalAmountFmtd(rf.formattedRupee(ft.format(nlpCategory.getAnnualWithdrawalAmount()/monthsBetween*12.0)));
            nlpCategory.setAnnualDepositAmount(accountStatementRepo.findDepositSumByCategory(category));
            nlpCategory.setMonthlyDepositAmount(nlpCategory.getAnnualDepositAmount()/monthsBetween);
            nlpCategory.setMonthlyDepositAmountFmtd(rf.formattedRupee(ft.format(nlpCategory.getMonthlyDepositAmount())));
            nlpCategory.setAnnualDepositAmountFmtd(rf.formattedRupee(ft.format(nlpCategory.getAnnualDepositAmount())));
            log.info("Zero Amount Debug: " + rf.formattedRupee(ft.format(nlpCategory.getAnnualDepositAmount())));
            List<String> discretionaryInd = nlpEntriesRepo.findDiscretionary(category);
            nlpCategory.setDiscretionaryIndicator(discretionaryInd.get(0));
            if (nlpCategory.getAnnualWithdrawalAmount() > nlpCategory.getAnnualDepositAmount())
                nlpCategory.setPercentOfTotal((nlpCategory.getAnnualWithdrawalAmount()/totalWithdrawals));
            else
                nlpCategory.setPercentOfTotal((nlpCategory.getAnnualDepositAmount()/totalDeposits));
            nlpCategory.setPercentOfTotalFmtd(df.format(nlpCategory.getPercentOfTotal()));

            if (nonTranCategories.contains(nlpCategory.getEntryCategory()))
            {
                log.info("Ignoring " + nonTranCategories + " for calculation");
            }
            else
            {
                if (nlpCategory.getAnnualWithdrawalAmount() > 0) {
                    nlpCategory.setSerialNumber(sNoWithdrawal);
                    NlpCategorizedWithdrawalList.add(nlpCategory);
                    nlpCategory = new NlpCategory();
                    sNoWithdrawal = sNoWithdrawal + 1;
                } else if (nlpCategory.getAnnualDepositAmount() > 0) {
                    nlpCategory.setSerialNumber(sNoDeposit);
                    NlpCategorizedDepositList.add(nlpCategory);
                    nlpCategory = new NlpCategory();
                    sNoDeposit = sNoDeposit + 1;
                }
            }
        }
        nlpCategory = new NlpCategory();
        nlpCategory.setSerialNumber(sNoWithdrawal);
        nlpCategory.setEntryCategory("Unknown Entries");
        double annualWithdrawal = accountStatementRepo.findWithdrawalSumByUnknown();
        if (annualWithdrawal > 0.00)
        {
            nlpCategory.setAnnualWithdrawalAmountFmtd(rf.formattedRupee(ft.format(annualWithdrawal)));
            nlpCategory.setMonthlyWithdrawalAmountFmtd(rf.formattedRupee(ft.format(accountStatementRepo.findWithdrawalSumByUnknown()/monthsBetween)));
            nlpCategory.setPercentOfTotal((accountStatementRepo.findWithdrawalSumByUnknown()/totalWithdrawals));
            nlpCategory.setPercentOfTotalFmtd(df.format(nlpCategory.getPercentOfTotal()));
            nlpCategory.setDiscretionaryIndicator("N");
            NlpCategorizedWithdrawalList.add(nlpCategory);
            sNoWithdrawal = sNoWithdrawal + 1;
        }

        nlpCategory = new NlpCategory();
        nlpCategory.setSerialNumber(sNoWithdrawal);
        nlpCategory.setEntryCategory("Total Discretionary Spending");
        nlpCategory.setAnnualWithdrawalAmountFmtd(rf.formattedRupee(ft.format(accountStatementRepo.findWithdrawalSumByDisc("Y",nonTranCategories))));
        nlpCategory.setMonthlyWithdrawalAmountFmtd(rf.formattedRupee(ft.format(accountStatementRepo.findWithdrawalSumByDisc("Y",nonTranCategories)/monthsBetween)));
        nlpCategory.setPercentOfTotal((accountStatementRepo.findWithdrawalSumByDisc("Y",nonTranCategories)/totalWithdrawals));
        nlpCategory.setPercentOfTotalFmtd(df.format(nlpCategory.getPercentOfTotal()));
        nlpCategory.setDiscretionaryIndicator("Y");
        NlpCategorizedWithdrawalList.add(nlpCategory);
        sNoWithdrawal = sNoWithdrawal + 1;

        nlpCategory = new NlpCategory();
        nlpCategory.setSerialNumber(sNoWithdrawal);
        nlpCategory.setEntryCategory("Total Non Discretionary Spending");
        nlpCategory.setAnnualWithdrawalAmountFmtd(rf.formattedRupee(ft.format(accountStatementRepo.findWithdrawalSumByDisc("N",nonTranCategories))));
        nlpCategory.setMonthlyWithdrawalAmountFmtd(rf.formattedRupee(ft.format(accountStatementRepo.findWithdrawalSumByDisc("N",nonTranCategories)/monthsBetween)));
        nlpCategory.setPercentOfTotal((accountStatementRepo.findWithdrawalSumByDisc("N",nonTranCategories)/totalWithdrawals));
        nlpCategory.setPercentOfTotalFmtd(df.format(nlpCategory.getPercentOfTotal()));
        nlpCategory.setDiscretionaryIndicator("N");
        NlpCategorizedWithdrawalList.add(nlpCategory);
        sNoWithdrawal = sNoWithdrawal + 1;

        nlpCategory = new NlpCategory();


        if (typeOfEntries.equals("Expenses"))
        {
            nlpCategory.setSerialNumber(sNoWithdrawal);
            nlpCategory.setEntryCategory("Total Spending");
            nlpCategory.setAnnualWithdrawalAmountFmtd(rf.formattedRupee(ft.format(totalWithdrawals)));
            nlpCategory.setMonthlyWithdrawalAmountFmtd(rf.formattedRupee(ft.format(totalWithdrawals/monthsBetween)));
            nlpCategory.setPercentOfTotal((totalWithdrawals/totalWithdrawals));
            nlpCategory.setPercentOfTotalFmtd(df.format(nlpCategory.getPercentOfTotal()));
            NlpCategorizedWithdrawalList.add(nlpCategory);
            return NlpCategorizedWithdrawalList;
        }
        else {
            nlpCategory.setSerialNumber(sNoDeposit);
            nlpCategory.setEntryCategory("Total Income");
            nlpCategory.setAnnualWithdrawalAmountFmtd("Rs.0.00");
            nlpCategory.setMonthlyWithdrawalAmountFmtd("Rs.0.00");
            nlpCategory.setAnnualDepositAmountFmtd(rf.formattedRupee(ft.format(totalDeposits)));
            nlpCategory.setMonthlyDepositAmountFmtd(rf.formattedRupee(ft.format(totalDeposits/monthsBetween)));
            nlpCategory.setPercentOfTotal((totalDeposits/totalDeposits));
            nlpCategory.setPercentOfTotalFmtd(df.format(nlpCategory.getPercentOfTotal()));
            NlpCategorizedDepositList.add(nlpCategory);
            return NlpCategorizedDepositList;
        }
    }

    public void saveAccountEntries(List<AccountStatement> accountStatements) {
        accountStatementRepo.saveAll(accountStatements);
    }

    public List<AccountStatement> findAll() {
        return accountStatementRepo.findAll();
    }

    public TimeLineData getTimeLine(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate transactionDateHigh = LocalDate.parse("0001-01-01");
        LocalDate transactionDateLow  = LocalDate.parse("9999-12-31");

        List<AccountStatement> accountStatements = accountStatementRepo.findAll();
        for (AccountStatement entry: accountStatements){
            LocalDate transactionDate = LocalDate.parse(entry.getTransactionDate(), formatter);
            if (transactionDateHigh.isBefore(transactionDate))
                transactionDateHigh = transactionDate;
            if (transactionDateLow.isAfter(transactionDate))
                transactionDateLow = transactionDate;
        }
        timeLineData.setTransactionDateLow(transactionDateLow);
        timeLineData.setTransactionDateHigh(transactionDateHigh);
        timeLineData.setDaysBetween((int)transactionDateLow.until(transactionDateHigh, ChronoUnit.DAYS));
        timeLineData.setMonthsBetween((int)transactionDateLow.until(transactionDateHigh, ChronoUnit.MONTHS)+1);
        return timeLineData;
    }

    private List<AccountStatement> getEnrichedList(List<AccountStatement> accountStatements) {
        for (AccountStatement entry: accountStatements){
            List<NlpEntry> nlpEntries = nlpEntriesRepo.findAll();
            for(NlpEntry nlpEntry:nlpEntries){
                if (entry.getTransactionRemarks().toUpperCase().contains(nlpEntry.getTokenizedWord())){
                    entry.setEntryCategory(nlpEntry.getEntryCategory());
                    entry.setDiscretionarySpendingIndicator(nlpEntry.getDiscretionaryInd());
                    accountStatementRepo.save(entry);
                    break;
                }
            }
        }
        return accountStatements;
    }
}

