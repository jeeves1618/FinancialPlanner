package net.myphenotype.financialStatements.processing.service;

import lombok.extern.slf4j.Slf4j;
import net.myphenotype.financialStatements.processing.domain.ChartResponsePayload;
import net.myphenotype.financialStatements.processing.entity.Journals;
import net.myphenotype.financialStatements.processing.repo.JournalsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;

@Slf4j
@Service
public class JournalService {

    @Autowired
    private JournalsRepo journalsRepo;

    @Autowired
    private ChartService chartService;

    @Autowired
    private RupeeFormatter rf;

    DecimalFormat ft = new DecimalFormat("Rs ##,##,##0.00");

    public Journals saveJournal(Journals journal) {
        Integer relKey;
        ChartResponsePayload chartResponsePayload = chartService.getCreditOrDebit(journal.getAccountNumber(), journal.getIncreaseOrDecreaseInd());
        journal.setJournalAmountFmtd(rf.formattedRupee(ft.format(journal.getJournalAmount())));
        journal.setCreditDebitInd(chartResponsePayload.getCreditOrDebit());
        if (journal.getCreditDebitInd().equals("Credit")){
            journal.setCreditAmountFmtd(journal.getJournalAmountFmtd());
        }
        else {
            journal.setDebitAmountFmtd(journal.getJournalAmountFmtd());
        }
        journal.setAccountTitle(chartResponsePayload.getAccountTitle());
        if (chartService.getStatementType(journal.getAccountNumber()).equals("Cash Flow"))
            journal.setJournalStatus("Posted");
        else
            journal.setJournalStatus("Pending");
        log.info("Saving the journal : " + journal.toString());
        journalsRepo.save(journal);
        if (journal.getJournalsRelKey() != null) relKey = journal.getJournalsRelKey();
        else relKey = journal.getJournalsKey();
        journal.setJournalMessage("Journal Entry Created with Relation Key : " + relKey);
        journal.setJournalsRelKey(relKey);

        if (differenceCreditDebit(relKey) == 0) {
            List<Journals> journalsList = journalsRepo.findNonStatementJournals(relKey,"Cash Flow");
            for (Journals journals : journalsList) {
                journals.setJournalStatus("Balanced");
                journalsRepo.save(journals);
            }
        } else {
            List<Journals> journalsList = journalsRepo.findNonStatementJournals(relKey,"Cash Flow");
            for (Journals journals : journalsList) {
                journals.setJournalStatus("Pending");
                journalsRepo.save(journals);
            }
        }
        journalsRepo.save(journal);
        log.info(journal.getJournalMessage());
        return journal;
    }

    private double differenceCreditDebit(Integer relKey){
        log.info("Rel Key " + relKey);
        double creditSum = 0.00, debitSum = 0.00;

        List<Journals> journalsList = journalsRepo.findNonStatementJournals(relKey,"Cash Flow");
        if (journalsList.isEmpty()) {
            Journals journal = journalsRepo.getById(relKey);
            if (journal.getCreditDebitInd().equals("Credit")) creditSum = creditSum + journal.getJournalAmount();
            else debitSum = debitSum + journal.getJournalAmount();
            log.info("Debit Sum for first entry" + debitSum);
            log.info("Credit Sum for first entry" + creditSum);
        }
        for(Journals journal: journalsList) {
            if (journal.getCreditDebitInd().equals("Credit")) creditSum = creditSum + journal.getJournalAmount();
            else debitSum = debitSum + journal.getJournalAmount();
        }
        log.info("Debit Sum " + debitSum);
        log.info("Credit Sum " + creditSum);
        return creditSum - debitSum;
    }
}
