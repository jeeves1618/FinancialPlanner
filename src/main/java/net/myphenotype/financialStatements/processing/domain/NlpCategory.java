package net.myphenotype.financialStatements.processing.domain;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class NlpCategory {

    private int serialNumber;
    private String entryCategory;
    private double annualAmount;
    private double monthlyAmount;
    private String annualAmountFmtd;
    private String monthlyAmountFmtd;
    private double percentOfTotal;
    private String percentOfTotalFmtd;
}
