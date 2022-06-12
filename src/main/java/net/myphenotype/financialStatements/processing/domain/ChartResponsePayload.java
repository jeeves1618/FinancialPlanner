package net.myphenotype.financialStatements.processing.domain;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class ChartResponsePayload {

    private String creditOrDebit;
    private String accountTitle;
    private double priorAcctPeriodBal;
    private String increaseOrDecreaseInd;
    private String financialStatement;
}
