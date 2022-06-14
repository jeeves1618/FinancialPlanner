package net.myphenotype.financialStatements.processing.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import javax.validation.constraints.Max;

@Data
@Entity
@Component
@Table (name = "nlp_entry")
public class NlpEntry {

    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    @Column (name = "nlp_entry_key")
    private Integer nlpEntryKey;
    @Column (name = "tokenized_word")
    private String tokenizedWord;
    @Max(350)
    @Column (name = "entry_category")
    private String entryCategory;
    @Max(100)
    @Column (name = "discretionary_Ind")
    private String discretionaryInd;
    @Max(100)
    @Column (name = "last_used_date")
    private String lastUsedDate;

    public NlpEntry() {
    }

    public NlpEntry(String tokenizedWord, String entryCategory, String discretionaryInd, String lastUsedDate) {
        this.tokenizedWord = tokenizedWord;
        this.entryCategory = entryCategory;
        this.discretionaryInd = discretionaryInd;
        this.lastUsedDate = lastUsedDate;
    }
}

