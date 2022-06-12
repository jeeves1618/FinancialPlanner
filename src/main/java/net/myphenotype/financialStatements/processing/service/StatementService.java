package net.myphenotype.financialStatements.processing.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.myphenotype.financialStatements.processing.entity.AccountStatement;
import net.myphenotype.financialStatements.processing.repo.AccountStatementRepo;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Data
@Slf4j
public class StatementService {
    
    @Autowired
    AccountStatement accountStatement;

    @Autowired
    AccountStatementRepo accountStatementRepo;

    DecimalFormat ft = new DecimalFormat("Rs ##,##,##0.00");
    RupeeFormatter rf = new RupeeFormatter();
    ArrayList<AccountStatement> AccountStatementList = new ArrayList<AccountStatement>();
    String accountNumber = null;

    public List<AccountStatement> getAccountEntries(String fileWithPathname) {
        int bsIterator = 0;
        try {
            FileInputStream file=new FileInputStream(new File(fileWithPathname));
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);

            //Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();

            //Read through the Header Rows

            Row headerRow = rowIterator.next();
            boolean skipProcessing =  true, moreTransactions = true;
            while (rowIterator.hasNext() && skipProcessing)
            {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                Cell cell = cellIterator.next();
                cell = cellIterator.next();
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains("Transactions List")){
                    String accountString = cell.getStringCellValue();
                    accountNumber = accountString.substring(accountString.length()-12);
                    log.info("accountString : " + accountString.substring(accountString.length()-12));
                }
                if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains("S No.")){
                    skipProcessing = false;
                }
            }

            while (rowIterator.hasNext() && moreTransactions)
            {
                Row row = rowIterator.next();
                //For each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();
                //Instantiate an Object for each individual member of Array
                //accountStatement = new AccountStatement();

                while (cellIterator.hasNext())
                {
                    Cell cell = cellIterator.next();

                    //Check the cell type and format accordingly
                    switch (cell.getCellType())
                    {
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return AccountStatementList;
    }

    public void saveAccountEntries(List<AccountStatement> accountStatements){
        accountStatementRepo.saveAll(accountStatements);
    }

    public List<AccountStatement> findAll(){
        return accountStatementRepo.findAll();
    }
}

