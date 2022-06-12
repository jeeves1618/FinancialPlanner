package net.myphenotype.financialStatements.processing.service;

import lombok.extern.slf4j.Slf4j;
import net.myphenotype.financialStatements.processing.domain.ChartResponsePayload;
import net.myphenotype.financialStatements.processing.domain.Ledger;
import net.myphenotype.financialStatements.processing.domain.LedgerSummary;
import net.myphenotype.financialStatements.processing.entity.Journals;
import net.myphenotype.financialStatements.processing.repo.JournalsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LedgerService {

    @Autowired
    private Ledger ledger;

    @Autowired
    private JournalsRepo journalsRepo;

    @Autowired
    private ChartService chartService;

    @Autowired
    private RupeeFormatter rf;

    @Autowired
    LedgerSummary ledgerSummary;

    DecimalFormat ft = new DecimalFormat("Rs ##,##,##0.00");

    public List<Ledger> getLedgerEntries(){
        List<Ledger> ledgerList = new ArrayList<>();
        List<Journals> journalsList = journalsRepo.findByJournalStatusOrderByAccountNumberAsc("Balanced");
        Ledger blankledger = new Ledger();
        String increaseDecreaseInd = null;
        double creditTotal = 0.00, debitTotal = 0.00, balance = 0.00;

        String tempJrnlKey = "00000";
        for(Journals journals: journalsList) {
            if (tempJrnlKey.equals("00000")) {
                blankledger.setDescriptions("Account No : " + journals.getAccountNumber() + "     Account : " + journals.getAccountTitle());
                ledgerList.add(blankledger);
                tempJrnlKey = journals.getAccountNumber();
                ChartResponsePayload chartResponsePayload = chartService.getIncreaseOrDecrease(journals.getAccountNumber(),journals.getCreditDebitInd());
                balance = chartResponsePayload.getPriorAcctPeriodBal();
                increaseDecreaseInd = chartResponsePayload.getIncreaseOrDecreaseInd();
            }
            ledger.setDescriptions(journals.getAccountTitle() + ". " + journals.getJournalReason());
            ledger.setJournalsRelKey(journals.getJournalsRelKey());
            ledger.setCreditDebitInd(journals.getCreditDebitInd());
            ledger.setDebitAmountFmtd(journals.getDebitAmountFmtd());
            ledger.setCreditAmountFmtd(journals.getCreditAmountFmtd());
            if (journals.getCreditDebitInd().equals("Credit")) creditTotal = creditTotal + journals.getJournalAmount();
            else debitTotal = debitTotal + journals.getJournalAmount();
            ledger.setLedgerDate(journals.getJournalDate());

            log.info("ledger : " + ledger.toString());

            log.info("Inputs : " + journals.getAccountNumber() + journals.getCreditDebitInd());
            ChartResponsePayload chartResponsePayload = chartService.getIncreaseOrDecrease(journals.getAccountNumber(),journals.getCreditDebitInd());
            increaseDecreaseInd = chartResponsePayload.getIncreaseOrDecreaseInd();
            if (!tempJrnlKey.equals(journals.getAccountNumber())){
                blankledger = new Ledger();
                blankledger.setDescriptions("Account No : " + journals.getAccountNumber() + "     Account : " + journals.getAccountTitle());
                ledgerList.add(blankledger);
                tempJrnlKey = journals.getAccountNumber();
                balance = chartResponsePayload.getPriorAcctPeriodBal();
            }
            log.info("Output : " + increaseDecreaseInd);
            if (increaseDecreaseInd.equals("I"))
                balance = balance + journals.getJournalAmount();
            else
                balance = balance - journals.getJournalAmount();
            ledger.setBalanceAmountFmtd(rf.formattedRupee(ft.format(balance)));
            ledgerList.add(ledger);
            ledger = new Ledger();
        }
        ledgerSummary.setDescriptions("Totals");
        ledgerSummary.setCreditAmountFmtd(rf.formattedRupee(ft.format(creditTotal)));
        ledgerSummary.setDebitAmountFmtd(rf.formattedRupee(ft.format(debitTotal)));
        ledger = new Ledger();
        ledger.setDescriptions(ledgerSummary.getDescriptions());
        ledger.setCreditAmountFmtd(ledgerSummary.getCreditAmountFmtd());
        ledger.setDebitAmountFmtd(ledgerSummary.getDebitAmountFmtd());
        ledgerList.add(ledger);
        return ledgerList;
    }
}
