package net.myphenotype.financialStatements.processing.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@Entity
@Component
@Table (name = "chart_of_accounts")
public class ChartOfAccounts {

    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    @Column (name = "chart_of_accounts_key")
    private Integer chartOfAccountsKey;
    @Max(5)
    @Min(5)
    @Column (name = "account_number")
    private String accountNumber;
    @Max(350)
    @Column (name = "account_description")
    private String accountDescription;
    @Max(100)
    @Column (name = "account_type")
    private String accountType;
    @Max(100)
    @Column (name = "financial_Statement")
    private String financialStatement;
    @Column (name = "prior_accounting_period_bal")
    private double priorAcctPeriodBal;
    @Column (name = "reporting_order")
    private Integer reportingOrder;

    public ChartOfAccounts() {
    }

    public ChartOfAccounts(String accountNumber, String accountDescription, String accountType, String financialStatement, double priorAcctPeriodBal) {
        this.accountNumber = accountNumber;
        this.accountDescription = accountDescription;
        this.accountType = accountType;
        this.financialStatement = financialStatement;
        this.priorAcctPeriodBal = priorAcctPeriodBal;
    }
}

