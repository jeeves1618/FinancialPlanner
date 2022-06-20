package net.myphenotype.financialStatements.processing.domain;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class NlpCategory {

    private int serialNumber;
    private String entryCategory;
    private double annualWithdrawalAmount;
    private double monthlyWithdrawalAmount;
    private String annualWithdrawalAmountFmtd;
    private String monthlyWithdrawalAmountFmtd;
    private double annualDepositAmount;
    private double monthlyDepositAmount;
    private String annualDepositAmountFmtd;
    private String monthlyDepositAmountFmtd;
    private double percentOfTotal;
    private String percentOfTotalFmtd;
    private String discretionaryIndicator;
}
