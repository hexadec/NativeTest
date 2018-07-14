package hu.hexadecimal.nativetest;

import java.text.DecimalFormat;
import java.util.LinkedList;

public class Results {
    private String name;
    private LinkedList<Long> parameterList;
    private LinkedList<Long> resultList;
    private String paramUnit;
    private String resultUnit;
    private double resultDecimalOffset;


    /**
     *  Constructor of Results class
     * @param name Name of the measurement
     * @param parameterUnit Unit of the independent variables
     * @param resultUnit Unit of the dependent variables
     * @param decimalOffset Decimal offset of the dependent variables, used as result * 10^decimalOffset
     *                      Use 0 to change nothing
     */
    Results(String name, String parameterUnit, String resultUnit, double decimalOffset) {
        this.name = name;
        parameterList = new LinkedList<>();
        resultList = new LinkedList<>();
        paramUnit = parameterUnit;
        this.resultUnit = resultUnit;
        resultDecimalOffset = Math.pow(10, decimalOffset);
    }

    /**
     * Adds and independent - dependent pair to the class
     * @param parameter independent variable
     * @param result dependent variable
     * @return number of variable pairs
     */
    int addPair(long parameter, long result) {
        parameterList.addLast(parameter);
        resultList.addLast(result);
        return parameterList.size();
    }

    /**
     * Convert class to CSV format
     * @return parameter - result pairs in CSV format, results are modified according to decimalOffset
     */
    public String toCSV() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append("\n");
        builder.append(paramUnit);
        builder.append(";");
        builder.append(resultUnit);
        builder.append(";\n");
        String pattern = "0";
        for (int i = 1; i > resultDecimalOffset; i /= 10) {
            if (i == 1) pattern += ".";
            pattern += "0";
        }
        DecimalFormat dec = new DecimalFormat(pattern);
        for (int i = 0; i < parameterList.size(); i++) {
            builder.append(parameterList.get(0));
            builder.append(";");
            builder.append(dec.format(resultList.get(0) * resultDecimalOffset));
            builder.append(";\n");
        }
        return builder.toString();
    }
}
