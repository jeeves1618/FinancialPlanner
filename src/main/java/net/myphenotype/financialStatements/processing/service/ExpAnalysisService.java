package net.myphenotype.financialStatements.processing.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.myphenotype.financialStatements.processing.domain.NlpCategory;
import net.myphenotype.financialStatements.processing.domain.UIMetaData;
import net.myphenotype.financialStatements.processing.entity.AccountStatement;
import net.myphenotype.financialStatements.processing.entity.NlpEntry;
import net.myphenotype.financialStatements.processing.repo.AccountStatementRepo;
import net.myphenotype.financialStatements.processing.repo.NlpEntriesRepo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Service
@Data
@Slf4j
public class ExpAnalysisService {

    @Autowired
    AccountStatement accountStatement;

    @Autowired
    AccountStatementRepo accountStatementRepo;

    @Autowired
    NlpEntriesRepo nlpEntriesRepo;

    @Autowired
    NlpCategory nlpCategory;

    @Autowired
    private RupeeFormatter rf;

    DecimalFormat ft = new DecimalFormat("Rs ##,##,##0.00");
    DecimalFormat df = new DecimalFormat("##.##%");
    ArrayList<AccountStatement> AccountStatementList = new ArrayList<AccountStatement>();

    String accountNumber = null;

    public List<NlpCategory> getUniqueEntries() {
        int sNo = 1;
        List<NlpCategory> NlpCategorizedWithdrawalList = new ArrayList<>();
        List<NlpCategory> NlpCategorizedDepositList = new ArrayList<>();
        List<String> nonTranCategories = Arrays.asList("Bookentries");
        double totalWithdrawals = accountStatementRepo.findWithdrawalTotals(nonTranCategories), totalDeposits = accountStatementRepo.findDepositTotals(nonTranCategories);
        List<String> nlpCategories = nlpEntriesRepo.findUniqueEntries();
        for (String category: nlpCategories)
        {
            nlpCategory.setSerialNumber(sNo);
            nlpCategory.setEntryCategory(category);
            nlpCategory.setAnnualWithdrawalAmount(accountStatementRepo.findWithdrawalSumByCategory(category));
            nlpCategory.setMonthlyWithdrawalAmount(nlpCategory.getAnnualWithdrawalAmount()/12);
            nlpCategory.setMonthlyWithdrawalAmountFmtd(rf.formattedRupee(ft.format(nlpCategory.getMonthlyWithdrawalAmount())));
            nlpCategory.setAnnualWithdrawalAmountFmtd(rf.formattedRupee(ft.format(nlpCategory.getAnnualWithdrawalAmount())));
            nlpCategory.setAnnualDepositAmount(accountStatementRepo.findDepositSumByCategory(category));
            nlpCategory.setMonthlyDepositAmount(nlpCategory.getAnnualDepositAmount()/12);
            nlpCategory.setMonthlyDepositAmountFmtd(rf.formattedRupee(ft.format(nlpCategory.getMonthlyDepositAmount())));
            nlpCategory.setAnnualDepositAmountFmtd(rf.formattedRupee(ft.format(nlpCategory.getAnnualDepositAmount())));
            List<String> discretionaryInd = nlpEntriesRepo.findDiscretionary(category);
            nlpCategory.setDiscretionaryIndicator(discretionaryInd.get(0));
            if (nlpCategory.getAnnualWithdrawalAmount() > nlpCategory.getAnnualDepositAmount())
                nlpCategory.setPercentOfTotal((nlpCategory.getAnnualWithdrawalAmount()/totalWithdrawals));
            else
                nlpCategory.setPercentOfTotal((nlpCategory.getAnnualDepositAmount()/totalDeposits));
            nlpCategory.setPercentOfTotalFmtd(df.format(nlpCategory.getPercentOfTotal()));

            if (nlpCategory.getEntryCategory().equals("Bookentries"))
            {
                log.info("Ignoring Bookentries for calculation");
            }
            else
            {
                if (nlpCategory.getAnnualWithdrawalAmount() > 0) {
                    NlpCategorizedWithdrawalList.add(nlpCategory);
                    nlpCategory = new NlpCategory();
                    sNo = sNo + 1;
                }
            }
        }
        nlpCategory = new NlpCategory();
        nlpCategory.setSerialNumber(sNo);
        nlpCategory.setEntryCategory("Total Discretionary Spending");
        nlpCategory.setAnnualWithdrawalAmountFmtd(rf.formattedRupee(ft.format(accountStatementRepo.findWithdrawalSumByDisc("Y",nonTranCategories))));
        nlpCategory.setMonthlyWithdrawalAmountFmtd(rf.formattedRupee(ft.format(accountStatementRepo.findWithdrawalSumByDisc("Y",nonTranCategories)/12)));
        nlpCategory.setPercentOfTotal((accountStatementRepo.findWithdrawalSumByDisc("Y",nonTranCategories)/totalWithdrawals));
        nlpCategory.setPercentOfTotalFmtd(df.format(nlpCategory.getPercentOfTotal()));
        nlpCategory.setDiscretionaryIndicator("Y");
        NlpCategorizedWithdrawalList.add(nlpCategory);
        sNo = sNo + 1;

        nlpCategory = new NlpCategory();
        nlpCategory.setSerialNumber(sNo);
        nlpCategory.setEntryCategory("Total Non Discretionary Spending");
        nlpCategory.setAnnualWithdrawalAmountFmtd(rf.formattedRupee(ft.format(accountStatementRepo.findWithdrawalSumByDisc("N",nonTranCategories))));
        nlpCategory.setMonthlyWithdrawalAmountFmtd(rf.formattedRupee(ft.format(accountStatementRepo.findWithdrawalSumByDisc("N",nonTranCategories)/12)));
        nlpCategory.setPercentOfTotal((accountStatementRepo.findWithdrawalSumByDisc("N",nonTranCategories)/totalWithdrawals));
        nlpCategory.setPercentOfTotalFmtd(df.format(nlpCategory.getPercentOfTotal()));
        nlpCategory.setDiscretionaryIndicator("N");
        NlpCategorizedWithdrawalList.add(nlpCategory);
        sNo = sNo + 1;

        nlpCategory = new NlpCategory();
        nlpCategory.setSerialNumber(sNo);
        nlpCategory.setEntryCategory("Total Spending");
        nlpCategory.setAnnualWithdrawalAmountFmtd(rf.formattedRupee(ft.format(totalWithdrawals)));
        nlpCategory.setMonthlyWithdrawalAmountFmtd(rf.formattedRupee(ft.format(totalWithdrawals/12)));
        nlpCategory.setPercentOfTotal((totalWithdrawals/totalWithdrawals));
        nlpCategory.setPercentOfTotalFmtd(df.format(nlpCategory.getPercentOfTotal()));
        NlpCategorizedWithdrawalList.add(nlpCategory);

        return NlpCategorizedWithdrawalList;
    }

    public List<AccountStatement> getAccountEntries(String fileWithPathname) {
        int bsIterator = 0;
        try {
            FileInputStream file = new FileInputStream(new File(fileWithPathname));
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);

            //Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();

            //Read through the Header Rows

            Row headerRow = rowIterator.next();
            boolean skipProcessing = true, moreTransactions = true;
            while (rowIterator.hasNext() && skipProcessing) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                Cell cell = cellIterator.next();
                cell = cellIterator.next();
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains("Transactions List")) {
                    String accountString = cell.getStringCellValue();
                    accountNumber = accountString.substring(accountString.length() - 12);
                    log.info("accountString : " + accountString.substring(accountString.length() - 12));
                }
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains("S No.")) {
                    skipProcessing = false;
                }
            }

            while (rowIterator.hasNext() && moreTransactions) {
                Row row = rowIterator.next();
                //For each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();
                //Instantiate an Object for each individual member of Array
                //accountStatement = new AccountStatement();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();

                    //Check the cell type and format accordingly
                    switch (cell.getCellType()) {
                        case NUMERIC:
                            //System.out.print(cell.getNumericCellValue() + "t");
                            switch (cell.getColumnIndex()) {
                                case 6:
                                    accountStatement.setWithdrawalAmount(cell.getNumericCellValue());
                                    accountStatement.setWithdrawalAmountFmtd(rf.formattedRupee(ft.format(accountStatement.getWithdrawalAmount())));
                                    //System.out.print(accountStatement.cashValue + "t");
                                    break;
                                case 7:
                                    accountStatement.setDepositAmount(cell.getNumericCellValue());
                                    accountStatement.setDepositAmountFmtd(rf.formattedRupee(ft.format(accountStatement.getDepositAmount())));
                                    //System.out.print(accountStatement.cashValue + "t");
                                    break;
                                case 8:
                                    accountStatement.setBalanceAmount(cell.getNumericCellValue());
                                    accountStatement.setBalanceAmountFmtd(rf.formattedRupee(ft.format(accountStatement.getBalanceAmount())));
                                    //System.out.print(accountStatement.cashValue + "t");
                                    break;
                                default:
                                    throw new IllegalStateException("Unexpected NUMERIC Cell Value in the Spreadsheet :" + bsIterator + " and " + cell.getColumnIndex() + " Value = " + cell.getNumericCellValue());
                            }
                            break;
                        case STRING:
                            //System.out.print(cell.getStringCellValue() + "t");
                            switch (cell.getColumnIndex()) {
                                case 1:
                                    accountStatement.setSerialNumber(cell.getStringCellValue());
                                    if (cell.getStringCellValue().contains("Legends")) moreTransactions = false;
                                    //System.out.print(accountStatement.typeAssetOrLiability + "t");

                                    break;
                                case 2:
                                    accountStatement.setValueDate(cell.getStringCellValue());
                                    //System.out.print(accountStatement.subType + "t");
                                    break;
                                case 3:
                                    accountStatement.setTransactionDate(cell.getStringCellValue());
                                    //System.out.print(accountStatement.itemDescription + "t");
                                    break;
                                case 4:
                                    accountStatement.setCheckNumber(cell.getStringCellValue());
                                    //System.out.print(accountStatement.itemDescription + "t");
                                    break;
                                case 5:
                                    accountStatement.setTransactionRemarks(cell.getStringCellValue());
                                    //System.out.print(accountStatement.itemDescription + "t");
                                    break;
                                default:
                                    throw new IllegalStateException("Unexpected STRING Cell Value in the Spreadsheet :" + bsIterator + " and " + cell.getColumnIndex());
                            }
                            break;
                        case BLANK:
                            //System.out.print(cell.getStringCellValue() + "t");
                            break;
                        default:
                            throw new IllegalStateException("Unexpected Cell Value in the Spreadsheet :" + bsIterator + " and " + cell.getColumnIndex());
                    }
                }
                accountStatement.setAccountNumber(accountNumber);
                if (accountStatement.getTransactionRemarks().contains("Default Comments"))
                    log.info("Non transactional rows are skipped");
                else
                    AccountStatementList.add(accountStatement);
                accountStatement = new AccountStatement();
                bsIterator++;
            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return getEnrichedList(AccountStatementList);
    }

    public void saveAccountEntries(List<AccountStatement> accountStatements) {
        accountStatementRepo.saveAll(accountStatements);
    }

    public List<AccountStatement> findAll() {
        return accountStatementRepo.findAll();
    }

    public List<AccountStatement> getCreditEntries(String fileWithPathname, UIMetaData uiMetaData) {
        int bsIterator = 0;
        double balanceAmount = 0.00;
        try {
            FileInputStream file = new FileInputStream(new File(fileWithPathname));
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);

            //Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();

            //Read through the Header Rows

            Row headerRow = rowIterator.next();
            boolean skipProcessing = true, moreTransactions = true;
            while (rowIterator.hasNext() && skipProcessing) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                Cell cell = cellIterator.next();
                cell = cellIterator.next();
                cell = cellIterator.next();
                cell = cellIterator.next();
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains("Transaction Date")) {
                    skipProcessing = false;
                }
            }

            while (rowIterator.hasNext() && moreTransactions) {
                Row row = rowIterator.next();
                //For each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();
                //Instantiate an Object for each individual member of Array
                //accountStatement = new AccountStatement();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();

                    //Check the cell type and format accordingly
                    switch (cell.getCellType()) {
                        case STRING:
                            //System.out.print(cell.getStringCellValue() + "t");
                            switch (cell.getColumnIndex()) {
                                case 3:
                                    accountStatement.setValueDate(cell.getStringCellValue());
                                    accountStatement.setTransactionDate(cell.getStringCellValue());
                                    //System.out.print(accountStatement.typeAssetOrLiability + "t");
                                    break;
                                case 4:
                                    accountStatement.setTransactionRemarks(cell.getStringCellValue());
                                    //System.out.print(accountStatement.subType + "t");
                                    break;
                                case 8:
                                    String numberStringWithCommas = cell.getStringCellValue().substring(0,cell.getStringCellValue().length()-4);
                                    String numberString = numberStringWithCommas.replaceAll("[^a-zA-Z0-9.]", "");
                                    if (cell.getStringCellValue().substring(cell.getStringCellValue().length()-3).equals("Dr.")){
                                        accountStatement.setWithdrawalAmount(Double.parseDouble(numberString));
                                        accountStatement.setDepositAmount(0.00);
                                        balanceAmount = balanceAmount + accountStatement.getWithdrawalAmount();
                                        accountStatement.setWithdrawalAmountFmtd(rf.formattedRupee(ft.format(accountStatement.getWithdrawalAmount())));
                                    } else {
                                        accountStatement.setDepositAmount(Double.parseDouble(numberString));
                                        accountStatement.setWithdrawalAmount(0.00);
                                        accountStatement.setDepositAmountFmtd(rf.formattedRupee(ft.format(accountStatement.getDepositAmount())));
                                    }
                                    accountStatement.setBalanceAmount(balanceAmount);
                                    accountStatement.setBalanceAmountFmtd(rf.formattedRupee(ft.format(accountStatement.getBalanceAmount())));
                                    //System.out.print(accountStatement.itemDescription + "t");
                                    break;
                                case 11:
                                    accountStatement.setCheckNumber(cell.getStringCellValue());
                                    //System.out.print(accountStatement.itemDescription + "t");
                                    break;
                                default:
                                    throw new IllegalStateException("Unexpected STRING Cell Value in the Spreadsheet :" + bsIterator + " and " + cell.getColumnIndex());
                            }
                            break;
                        case BLANK:
                            //System.out.print(cell.getStringCellValue() + "t");
                            break;
                        default:
                            throw new IllegalStateException("Unexpected Cell Value in the Spreadsheet :" + bsIterator + " and " + cell.getColumnIndex());
                    }
                }
                accountStatement.setAccountNumber("Credit Card");
                AccountStatementList.add(accountStatement);
                accountStatement = new AccountStatement();
                bsIterator++;
            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (uiMetaData.getJournalsKey() != null)
        accountStatementRepo.deleteById(uiMetaData.getJournalsKey());

        return getEnrichedList(AccountStatementList);
    }

    public List<AccountStatement> getAccountListForAnYear(){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate transactionDateHigh = LocalDate.parse("0001-01-01");
        LocalDate transactionDateLow  = LocalDate.parse("9999-12-31");

        List<AccountStatement> accountStatements = accountStatementRepo.findAll();
        for (AccountStatement entry: accountStatements){
            LocalDate transactionDate = LocalDate.parse(entry.getTransactionDate(), formatter);
            if (transactionDateHigh.isBefore(transactionDate))
                transactionDateHigh = transactionDate;
            if (transactionDateLow.isAfter(transactionDate))
                transactionDateLow = transactionDate;
        }
        transactionDateLow = transactionDateHigh.minusYears(1l).plusDays(1l);
        return accountStatements;
    }

    private List<AccountStatement> getEnrichedList(List<AccountStatement> accountStatements) {
        for (AccountStatement entry: accountStatements){
            List<NlpEntry> nlpEntries = nlpEntriesRepo.findAll();

            for(NlpEntry nlpEntry:nlpEntries){
                if (entry.getTransactionRemarks().toUpperCase().contains(nlpEntry.getTokenizedWord())){
                    entry.setEntryCategory(nlpEntry.getEntryCategory());
                    entry.setDiscretionarySpendingIndicator(nlpEntry.getDiscretionaryInd());
                    accountStatementRepo.save(entry);
                    break;
                }
            }
        }
        return accountStatements;
    }
}

