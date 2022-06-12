package net.myphenotype.financialStatements.processing.domain;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@Scope (value = "prototype")
public class Ledger {

    private String ledgerDate;
    private String descriptions;
    private Integer journalsRelKey;
    private String creditDebitInd;
    private String debitAmountFmtd;
    private String creditAmountFmtd;
    private String balanceAmountFmtd;
}
