package hu.hexadecimal.nativetest;

import java.text.DecimalFormat;
import java.util.LinkedList;

public class Results {
    private String name;
    private LinkedList<Long> parameterList;
    private LinkedList<Long> resultListCPP;
    private LinkedList<Long> resultListJava;
    private String paramUnit;
    private String resultUnitCPP;
    private String resultUnitJava;
    private double resultDecimalOffset;


    /**
     *  Constructor of Results class
     * @param name Name of the measurement
     * @param parameterUnit Unit of the independent variables
     * @param resultUnitCPP  Unit of the dependent variables (C++)
     * @param resultUnitJava Unit of the dependent variables (Java)
     * @param decimalOffset Decimal offset of the dependent variables, used as result * 10^decimalOffset
     *                      Use 0 to change nothing
     *                      If bigger than +/- 8, it will be set to 0
     */
    Results(String name, String parameterUnit, String resultUnitCPP, String resultUnitJava, int decimalOffset) {
        this.name = name;
        parameterList = new LinkedList<>();
        resultListCPP = new LinkedList<>();
        resultListJava = new LinkedList<>();
        paramUnit = parameterUnit;
        this.resultUnitCPP = resultUnitCPP;
        this.resultUnitJava = resultUnitJava;
        if (Math.abs(decimalOffset) > 8)
            decimalOffset = 0;
        resultDecimalOffset = Math.pow(10, decimalOffset);
    }

    /**
     * Adds and independent - dependent pair to the class
     * @param parameter independent variable
     * @param resultCPP dependent variable (C++)
     * @param resultJava dependent variable (Java)
     * @return number of variable pairs
     */
    int addTriple(long parameter, long resultCPP, long resultJava) {
        parameterList.addLast(parameter);
        resultListCPP.addLast(resultCPP);
        resultListJava.addLast(resultJava);
        return parameterList.size();
    }

    /**
     * @return name of Results
     */
    String getName() {
        return name;
    }

    /**
     * Convert class to CSV format
     * @param maxDecPlaces maximum decimal places
     * @return parameter - result pairs in CSV format, results are modified according to decimalOffset
     */
    public String toCSV(int maxDecPlaces) {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append("\n");
        builder.append(paramUnit);
        builder.append(";");
        builder.append(resultUnitCPP);
        builder.append(";");
        builder.append(resultUnitJava);
        builder.append(";\n");
        String pattern = "0";
        if (maxDecPlaces > 0) {
            for (double i = 1; i > resultDecimalOffset && pattern.length() - 2 < maxDecPlaces; i /= 10) {
                if (i == 1) pattern += ".";
                pattern += "0";
            }
        }
        DecimalFormat dec = new DecimalFormat(pattern);
        for (int i = 0; i < parameterList.size(); i++) {
            builder.append(parameterList.get(i));
            builder.append(";");
            builder.append(dec.format(resultListCPP.get(i) * resultDecimalOffset));
            builder.append(";");
            builder.append(dec.format(resultListJava.get(i) * resultDecimalOffset));
            builder.append(";\n");
        }
        return builder.toString();
    }
}
