package net.myphenotype.financialStatements.processing.controller;

import lombok.extern.slf4j.Slf4j;
import net.myphenotype.financialStatements.processing.entity.NlpEntry;
import net.myphenotype.financialStatements.processing.repo.NlpEntriesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping(path = "/nlp")
public class NlpController {

    @Autowired
    NlpEntriesRepo nlpEntriesRepo;

    @Autowired
    NlpEntry nlpEntry;

    @GetMapping(path = "/data/list")
    public String getNlpEntries(Model model){
        List<NlpEntry> nlpEntries = nlpEntriesRepo.findAll();
        model.addAttribute("nlpEntries",nlpEntries);
        log.info("nlpEntries" + nlpEntries);
        return "nlpList";
    }

    @GetMapping(path = "/data/showFormForUpdating")
    public String ShowFormForUpdate(@RequestParam("entryID") int theID, Model model){
        //Get the book using the ID from the Service (in turn from DAO and in turn from Table)
        NlpEntry nlpToBeUpdated = nlpEntriesRepo.getById(theID);

        //Set the Customer as the Model Attribute to Prepopulate the Form
        model.addAttribute("nlpEntry",nlpToBeUpdated);

        //Send the data to the right form
        return "nlpAddForm";
    }

    @GetMapping(path = "/data/showFormForAdding")
    public String ShowFormForAdd(Model model){
        //Set the Customer as the Model Attribute to Prepopulate the Form
        model.addAttribute("nlpEntry",nlpEntry);

        //Send the data to the right form
        return "nlpAddForm";
    }

    @PostMapping(path = "/data/addUpdateEntry")
    public String AddBookToList(@ModelAttribute("nlpEntry") NlpEntry nlpEntry){
        nlpEntriesRepo.save(nlpEntry);
        return "redirect:/nlp/data/list";
    }

    @GetMapping(path = "/data/showFormForDeleting")
    public String ShowFormForDelete(@RequestParam("entryID") int theID, Model model){
        //Get the Chart Entry using the ID from the Service (in turn from DAO and in turn from Table)
        NlpEntry entryToBeDeleted = nlpEntriesRepo.getById(theID);

        //Set the Customer as the Model Attribute to Prepopulate the Form
        model.addAttribute("nlpEntry",entryToBeDeleted);
        log.info(entryToBeDeleted.toString());

        //Send the data to the right form
        return "nlpDeleteForm";
    }

    @GetMapping(path = "/data/delete")
    public String DeleteChartEntry(@RequestParam("entryID") int theID, Model model){
        //Delete the Chart Entry
        log.info("The ID of nlp entry to be deleted : " + theID);
        nlpEntriesRepo.deleteById(theID);
        return "redirect:/nlp/data/list";
    }
}
