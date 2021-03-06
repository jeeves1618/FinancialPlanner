package net.myphenotype.financialStatements.processing.repo;

import net.myphenotype.financialStatements.processing.entity.NlpEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NlpEntriesRepo extends JpaRepository<NlpEntry,Integer> {
    @Query("select distinct n.entryCategory from NlpEntry n ")
    public List<String> findUniqueEntries();

    @Query("select distinct n.discretionaryInd from NlpEntry n where n.entryCategory = ?1")
    public List<String> findDiscretionary(String category);
}
