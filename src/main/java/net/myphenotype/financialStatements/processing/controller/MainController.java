package net.myphenotype.financialStatements.processing.controller;

import lombok.extern.slf4j.Slf4j;
import net.myphenotype.financialStatements.processing.domain.*;
import net.myphenotype.financialStatements.processing.entity.AccountStatement;
import net.myphenotype.financialStatements.processing.entity.ChartOfAccounts;
import net.myphenotype.financialStatements.processing.entity.Journals;
import net.myphenotype.financialStatements.processing.entity.UploadInfo;
import net.myphenotype.financialStatements.processing.repo.ChartOfAccountsRepo;
import net.myphenotype.financialStatements.processing.repo.JournalsRepo;
import net.myphenotype.financialStatements.processing.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Controller
@RequestMapping(path = "/fin")
public class MainController {

    @Autowired
    ChartOfAccountsRepo chartOfAccountsRepo;

    @Autowired
    ChartOfAccounts chartOfAccounts;

    @Autowired
    ChartService chartService;

    @Autowired
    Journals journals;

    @Autowired
    JournalsRepo journalsRepo;

    @Autowired
    JournalService journalService;

    @Autowired
    JournalRel journalRel;

    @Autowired
    Ledger ledger;

    @Autowired
    LedgerService ledgerService;

    @Autowired
    TrialBalanceService trialBalanceService;

    @Autowired
    BalanceSheetService balanceSheetService;

    @Autowired
    IncomeService incomeService;

    @Autowired
    CashService cashService;

    @Autowired
    UIMetaData uiMetaData;

    @Autowired
    StatementService statementService;

    @Autowired
    UploadInfo uploadInfo;

    @Autowired
    UploadService uploadService;

    private final String UPLOAD_FILE_PATH = "C:\\Dev\\Data\\";
    private final String USER_NAME = System.getProperty("user.name").substring(0,1).toUpperCase()+ System.getProperty("user.name").substring(1);

    @GetMapping(path = "/meta/coa")
    public String getChartOfAccounts(Model model){
        List<ChartOfAccounts> chartOfAccountsList = chartOfAccountsRepo.findAll();
        model.addAttribute("chartOfAccts",chartOfAccountsList);
        log.info("chartOfAccounts" + chartOfAccountsList);
        return "chartOfAccounts";
    }

    @GetMapping(path = "/meta/showFormForUpdating")
    public String ShowFormForUpdate(@RequestParam("chartID") int theID, Model model){
        //Get the book using the ID from the Service (in turn from DAO and in turn from Table)
        ChartOfAccounts coaToBeUpdated = chartOfAccountsRepo.getById(theID);

        //Set the Customer as the Model Attribute to Prepopulate the Form
        model.addAttribute("chartOfAccount",coaToBeUpdated);

        //Send the data to the right form
        return "coaForm";
    }

    @GetMapping(path = "/meta/showFormForAdding")
    public String ShowFormForAdd(Model model){
        //Get the book using the ID from the Service (in turn from DAO and in turn from Table)


        //Set the Customer as the Model Attribute to Prepopulate the Form
        model.addAttribute("chartOfAccount",chartOfAccounts);

        //Send the data to the right form
        return "coaForm";
    }

    @PostMapping(path = "/meta/addUpdateChart")
    public String AddBookToList(@ModelAttribute("chartOfAccount") ChartOfAccounts chartOfAccounts){
        chartOfAccountsRepo.save(chartOfAccounts);
        return "redirect:/fin/meta/coa";
    }

    @GetMapping(path = "/meta/showFormForDeleting")
    public String ShowFormForDelete(@RequestParam("chartID") int theID, Model model){
        //Get the Chart Entry using the ID from the Service (in turn from DAO and in turn from Table)
        ChartOfAccounts entryToBeDeleted = chartOfAccountsRepo.getById(theID);

        //Set the Customer as the Model Attribute to Prepopulate the Form
        model.addAttribute("chartOfAccount",entryToBeDeleted);
        log.info(entryToBeDeleted.toString());

        //Send the data to the right form
        return "deleteCoaForm";
    }

    @GetMapping(path = "/meta/delete")
    public String DeleteChartEntry(@RequestParam("chartID") int theID, Model model){
        //Delete the Chart Entry
        log.info("The ID of chart entry to be deleted : " + theID);
        chartOfAccountsRepo.deleteById(theID);
        return "redirect:/fin/meta/coa";
    }

    @GetMapping(path = "/txn/entry")
    public String enterJournals(Model model){
        //Set the Customer as the Model Attribute to Prepopulate the Form
        model.addAttribute("journal",new Journals());
        //Send the data to the right form
        return "journalEntry";
    }

    @GetMapping(path = "/txn/relentry")
    public String enterRelJournals(@RequestParam("journalKey") int theID, Model model){
        //Set the Customer as the Model Attribute to Prepopulate the Form
        log.info("relID : " + theID);
        journals.setJournalsRelKey(theID);
        journals.setJournalsKey(null);
        journals.setAccountNumber(null);
        journals.setIncreaseOrDecreaseInd(null);
        journals.setJournalMessage(null);
        model.addAttribute("journal",journals);
        //Send the data to the right form
        return "journalEntry";
    }

    @GetMapping(path = "/txn/updateForm")
    public String updateJournalsForm(@RequestParam("journalKey") int theID, Model model){
        //Set the Customer as the Model Attribute to Prepopulate the Form
        model.addAttribute("journal",journals);
        //Send the data to the right form
        return "journalEntry";
    }

    @GetMapping(path = "/txn/deleteForm")
    public String deleteJournalsForm(@RequestParam("journalKey") int theID, Model model){
        //Get the Chart Entry using the ID from the Service (in turn from DAO and in turn from Table)
        Journals entryToBeDeleted = journalsRepo.getById(theID);

        //Set the Customer as the Model Attribute to Prepopulate the Form
        model.addAttribute("journal",entryToBeDeleted);
        log.info(entryToBeDeleted.toString());

        //Send the data to the right form
        return "deleteJnlForm";
    }

    @PostMapping(path = "/txn/addUpdateJournal")
    public String addJournal(Model model, @ModelAttribute("journal") Journals journal){
        log.info("journal.getCashAccountNumber() " + journal.getCashAccountNumber());
        if (journal.getCashAccountNumber() != null) {
            journal.setAccountNumber(journal.getCashAccountNumber());
        }
        if (chartService.getStatementType(journal.getAccountNumber()).equals("Cash Flow"))
            journal.setJournalsKey(null);
        journals = journalService.saveJournal(journal);
        log.info("journals.getJournalMessage() = " + journals.getJournalMessage());
        journalRel.setJournalMessage(journals.getJournalMessage());
        if (journal.getAccountNumber().equals("00000") && (journal.getCashAccountNumber() == null))
        {
            journal.setAccountNumber("00");
            model.addAttribute(journal);
            return "cashAccountEntry";
        }

        else
            return "redirect:/fin/txn/listadd";
    }

    @GetMapping(path = "/txn/list")
    public String getJournals(Model model){
        List<Journals> journalsList = journalsRepo.findNonCashJournals("Cash Flow");
        List<JournalRel> journalRels = new ArrayList<>();
        Journals journal;
        if (journalRel.getJournalMessage() != null) journalRels.add(journalRel);
        if (journalsList.isEmpty())
        {
            journal = new Journals();
        } else {
            journal = journalsList.get(journalsList.size()-1);
            log.info("Index " + (journalsList.size()-1));
            log.info("Last Element " + journalsList.get(journalsList.size()-1).toString());
        }
        uiMetaData.setTitleText("List of Journals");
        uiMetaData.setEnableButtonIndicator("Yes");
        model.addAttribute("uiMeta",uiMetaData);
        model.addAttribute("journal",journal);
        model.addAttribute("journals",journalsList);
        return "journalSummary";
    }

    @GetMapping(path = "/txn/oblist")
    public String getOutOfBalance(Model model){
        List<Journals> journalsList = journalsRepo.findByJournalStatus("Pending");
        List<JournalRel> journalRels = new ArrayList<>();
        Journals journal;
        if (journalRel.getJournalMessage() != null) journalRels.add(journalRel);
        if (journalsList.isEmpty())
        {
            journal = new Journals();
        } else {
            journal = journalsList.get(journalsList.size()-1);
            log.info("Index " + (journalsList.size()-1));
            log.info("Last Element " + journalsList.get(journalsList.size()-1).toString());
        }
        uiMetaData.setTitleText("Out of Balance Journals");
        uiMetaData.setEnableButtonIndicator("Yes");
        model.addAttribute("uiMeta",uiMetaData);
        model.addAttribute("journal",journal);
        model.addAttribute("journals",journalsList);
        return "journalSummary";
    }

    @GetMapping(path = "/txn/cshlist")
    public String getCashEntries(Model model){
        List<Journals> journalsList = journalsRepo.findByJournalStatus("Posted");
        List<JournalRel> journalRels = new ArrayList<>();
        Journals journal;
        if (journalRel.getJournalMessage() != null) journalRels.add(journalRel);
        if (journalsList.isEmpty())
        {
            journal = new Journals();
        } else {
            journal = journalsList.get(journalsList.size()-1);
            log.info("Index " + (journalsList.size()-1));
            log.info("Last Element " + journalsList.get(journalsList.size()-1).toString());
        }
        uiMetaData.setTitleText("List of Cash Entries");
        uiMetaData.setEnableButtonIndicator("No");
        model.addAttribute("uiMeta",uiMetaData);
        model.addAttribute("journal",journal);
        model.addAttribute("journals",journalsList);
        return "journalSummary";
    }

    @GetMapping(path = "/txn/listadd")
    public String getAddReadBack(Model model){
        List<Journals> journalsList = journalsRepo.findAll();
        List<JournalRel> journalRels = new ArrayList<>();
        if (journalRel.getJournalMessage() != null) journalRels.add(journalRel);
        uiMetaData.setTitleText("List of Journals");
        uiMetaData.setEnableButtonIndicator("Yes");
        model.addAttribute("uiMeta",uiMetaData);
        model.addAttribute("journal",journals);
        model.addAttribute("journalRels",journalRels);
        model.addAttribute("journals",journalsList);
        return "journalSummary";
    }

    @GetMapping(path = "/txn/ledger")
    public String getLedger(Model model){
        List<Ledger> ledgerList = ledgerService.getLedgerEntries();
        model.addAttribute("ledgers",ledgerList);
        return "accountLedger";
    }

    @GetMapping(path = "/txn/tbalance")
    public String getTrialBalance(Model model){
        List<TrialBalance> trialBalances = trialBalanceService.getTrialBalanceEntries();
        model.addAttribute("trialbalances", trialBalances);
        return "trialBalance";
    }

    @GetMapping(path = "/rpt/bsheet")
    public String getBalanceSheet(Model model){
        List<BalanceSheet> balanceSheetEntries = balanceSheetService.getBalanceSheetEntries();
        model.addAttribute("balances", balanceSheetEntries);
        return "balanceSheet";
    }


    @GetMapping(path = "/rpt/istmt")
    public String getIncomeStatement(Model model){
        List<BalanceSheet> incomeEntries = incomeService.getIncomeEntries();
        model.addAttribute("balances", incomeEntries);
        return "incomeStatement";
    }

    @GetMapping(path = "/txn/cflist")
    public String getCashFlow(Model model){
        List<BalanceSheet> cashEntries = cashService.getCashEntries();
        model.addAttribute("balances", cashEntries);
        return "cashFlow";
    }

    @GetMapping(path = "/exp/entry")
    public String getFileToAdd(Model model){
        List<UploadInfo> uploadInfoList = uploadService.findall();
        model.addAttribute("uploadEntries", uploadInfoList);
        model.addAttribute("messagetext", "Please select the type of statement and upload it here");
        model.addAttribute("UIMetaData",uiMetaData);
        return "statementUpload";
    }

    @GetMapping(path = "/exp/list")
    public String getStatementList(Model model){
        List<AccountStatement> accountEntries = statementService.findAll();
        model.addAttribute("accountEntries", accountEntries);
        model.addAttribute("messagetext", "Please upload your bank statement here");
        return "accountStatement";
    }

    @PostMapping("/exp/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,  @ModelAttribute("UIMetaData") UIMetaData uiMetaData,
            RedirectAttributes attributes, Model model) {

        // check if file is empty
        if (file.isEmpty()) {
            attributes.addFlashAttribute("message", "Please select a file to upload.");
            return "accountStatement";
        }

        // normalize the file path
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        log.info("The File name is : " + UPLOAD_FILE_PATH + fileName + " and the user is " + USER_NAME);
        uploadInfo.setFileUploadKey(null);
        uploadInfo.setFileName(fileName);
        uploadInfo.setContainingFolder(UPLOAD_FILE_PATH);
        uploadInfo.setUploadedBy(USER_NAME);
        uploadInfo.setFileType(uiMetaData.getTypeOfStatement());
        uploadInfo.setFileSize(file.getSize());

        uploadService.saveUploadInfo(uploadInfo);

        //Call getAccountEntries of Statement Service here. The excel to Account Statement Mapping may have to change a bit.

        // save the file on the local file system
        /*
        try {
            Path path = Paths.get(UPLOAD_FILE_PATH + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        // return success response
        //attributes.addFlashAttribute("message", "You successfully uploaded " + fileName + '!');
        List<AccountStatement> draftAccountEntries = statementService.getAccountEntries(UPLOAD_FILE_PATH + fileName);
        statementService.saveAccountEntries(draftAccountEntries);

        List<UploadInfo> uploadInfoList = uploadService.findall();
        model.addAttribute("uploadEntries", uploadInfoList);
        model.addAttribute("messagetext", "You successfully uploaded " + fileName + '!');
        return "statementUpload";
    }
}
