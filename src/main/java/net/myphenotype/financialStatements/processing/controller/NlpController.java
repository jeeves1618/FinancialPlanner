package net.myphenotype.financialStatements.processing.controller;

import lombok.extern.slf4j.Slf4j;
import net.myphenotype.financialStatements.processing.entity.NlpEntry;
import net.myphenotype.financialStatements.processing.repo.NlpEntriesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping(path = "/nlp")
public class NlpController {

    @Autowired
    NlpEntriesRepo nlpEntriesRepo;

    @GetMapping(path = "/data/list")
    public String getNlpEntries(Model model){
        List<NlpEntry> nlpEntries = nlpEntriesRepo.findAll();
        model.addAttribute("nlpEntries",nlpEntries);
        log.info("nlpEntries" + nlpEntries);
        return "nlpList";
    }


}
