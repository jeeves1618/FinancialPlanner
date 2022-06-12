package net.myphenotype.financialStatements.processing.service;

import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class RupeeFormatter {
    public String formattedRupee(String fmtdAmount){

        String currencyFormat = "Rs ##,##,##0.00";
        String rupeeFormatted;
        if (currencyFormat.equals("Rs ##,##,##0.00")) {
            int stringLength = fmtdAmount.length();
            char[] inptAmtChar = fmtdAmount.toCharArray();
            char[] outpAmtChar = new char[stringLength*2];
            int outIterator = stringLength*2, inIterator;
            boolean swapFlag = true;

            for(inIterator = 1; inIterator < stringLength && inptAmtChar[stringLength - inIterator] != ' '; inIterator++){
                if (inIterator >= 10 && inptAmtChar[stringLength - inIterator] != ',' && swapFlag == true) {
                    outpAmtChar[--outIterator] = ',';
                    outpAmtChar[--outIterator] = inptAmtChar[stringLength - inIterator];
                    swapFlag = false;
                } else if (inIterator >= 10 && inptAmtChar[stringLength - inIterator] != ',' && swapFlag == false){
                    outpAmtChar[--outIterator] = inptAmtChar[stringLength - inIterator];
                    swapFlag = true;
                } else if (inIterator < 10){
                    outpAmtChar[--outIterator] = inptAmtChar[stringLength - inIterator];
                    swapFlag = true;
                }
            }
            if((stringLength - inIterator) == 2)
                rupeeFormatted = "Rs." + new String(outpAmtChar).replace("\u0000", "");
            else
                rupeeFormatted = "-Rs." + new String(outpAmtChar).replace("\u0000", "");
        } else {
            rupeeFormatted = fmtdAmount;
        }
        return rupeeFormatted;
    }
    public static void main(String args[]){
        RupeeFormatter r1 = new RupeeFormatter();
        Scanner scan = new Scanner(System.in);

        System.out.println("Please Enter your Annual Salary in String format :");
        String fmtdAmt = scan.nextLine();
        System.out.println(r1.formattedRupee(fmtdAmt));
    }
}
