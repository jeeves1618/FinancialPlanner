package net.myphenotype.financialStatements.processing.domain;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Data
@Scope (value = "prototype")
public class TrialBalance {

    private String accountTitle;
    private String debitAmountFmtd;
    private String creditAmountFmtd;
}
