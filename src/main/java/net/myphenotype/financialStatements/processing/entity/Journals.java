package net.myphenotype.financialStatements.processing.entity;

import lombok.Data;
import net.myphenotype.financialStatements.processing.domain.JournalOptions;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@Entity
@Component
@Table (name = "journal_entries")
public class Journals {

    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    @Column (name = "journals_key")
    private Integer journalsKey;
    @Column (name = "account_number")
    private String accountNumber;
    @Column (name = "account_title")
    private String accountTitle;
    @Transient
    private String increaseOrDecreaseInd;
    @Column (name = "journal_amount")
    private double journalAmount;
    @Column (name = "journal_date")
    private String journalDate;
    @Column (name = "journal_amount_fmtd")
    private String journalAmountFmtd;
    @Column (name = "credit_debit_ind")
    private String creditDebitInd;
    @Column (name = "journal_reason")
    private String journalReason;
    @Column (name = "journal_status")
    private String journalStatus;
    @Column (name = "journals_rel_key")
    private Integer journalsRelKey;
    @Column (name = "debit_amount_fmtd")
    private String debitAmountFmtd;
    @Column (name = "credit_amount_fmtd")
    private String creditAmountFmtd;
    @Transient
    private JournalOptions journalOptions;
    @Transient
    private String journalMessage;
    @Transient
    private String cashAccountNumber;
    @Column (name = "origin_indicator")
    private String originIndicator;

    public Journals() {
    }
}

