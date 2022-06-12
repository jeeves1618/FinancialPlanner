package net.myphenotype.financialStatements.processing.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Entity
@Data
@Component
@Table(name = "account_statement")
public class AccountStatement{
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    @Column(name = "acct_statement_key")
    private int acctStaementKey;
    private String serialNumber;
    private String valueDate;
    private String transactionDate;
    private String checkNumber;
    private String transactionRemarks;
    private double withdrawalAmount;
    private double depositAmount;
    private double balanceAmount;
    private String withdrawalAmountFmtd;
    private String depositAmountFmtd;
    private String balanceAmountFmtd;
    private String entryCategory;
    private int numofInstanceElements;
    private String discretionarySpendingIndicator;
    private String accountNumber;

    public AccountStatement(){
        this.valueDate = "23/09/1980";
        this.transactionDate = "23/09/1980";
        this.checkNumber = "00000";
        this.transactionRemarks = "Default Comments";
        this.withdrawalAmount = 0;
        this.depositAmount = 0;
        this.balanceAmount = 0;
        this.discretionarySpendingIndicator = "N";
        this.accountNumber = "Unknown";
    }
}