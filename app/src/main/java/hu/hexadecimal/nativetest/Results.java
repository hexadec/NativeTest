package hu.hexadecimal.nativetest;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
    public Results(String name, String parameterUnit, String resultUnitCPP, String resultUnitJava, int decimalOffset) {
        this(name, parameterUnit, resultUnitCPP, resultUnitJava, Math.abs(decimalOffset) > 8 ? 0 : Math.pow(10, decimalOffset));
    }

    private Results(String name, String parameterUnit, String resultUnitCPP, String resultUnitJava, double decimalOffset) {
        this.name = name;
        parameterList = new LinkedList<>();
        resultListCPP = new LinkedList<>();
        resultListJava = new LinkedList<>();
        paramUnit = parameterUnit;
        this.resultUnitCPP = resultUnitCPP;
        this.resultUnitJava = resultUnitJava;
        resultDecimalOffset = decimalOffset;
    }

    /**
     * Adds an independent - dependent pair to the class
     * @param parameter independent variable
     * @param resultCPP dependent variable (C++)
     * @param resultJava dependent variable (Java)
     * @return number of variable pairs
     */
    public int addTriple(long parameter, long resultCPP, long resultJava) {
        parameterList.addLast(parameter);
        resultListCPP.addLast(resultCPP);
        resultListJava.addLast(resultJava);
        return parameterList.size();
    }

    /**
     * @return name of Results
     */
    public String getName() {
        return name;
    }

    /**
     * @return the number of parameters / independent variables stored
     */
    public int getSize() {
        return parameterList.size();
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
        builder.append(",C++ ");
        builder.append(resultUnitCPP);
        builder.append(",Java ");
        builder.append(resultUnitJava);
        builder.append(",\n");
        StringBuilder pattern = new StringBuilder("0");
        if (maxDecPlaces > 0) {
            for (double i = 1; i > resultDecimalOffset && pattern.length() - 2 < maxDecPlaces; i /= 10) {
                if (i == 1) pattern.append(".");
                pattern.append("0");
            }
        }
        DecimalFormatSymbols sym = new DecimalFormatSymbols();
        sym.setDecimalSeparator('.');
        DecimalFormat dec = new DecimalFormat(pattern.toString(), sym);
        for (int i = 0; i < parameterList.size(); i++) {
            builder.append(parameterList.get(i));
            builder.append(",");
            builder.append(dec.format(resultListCPP.get(i) * resultDecimalOffset));
            builder.append(",");
            builder.append(dec.format(resultListJava.get(i) * resultDecimalOffset));
            builder.append(",\n");
        }
        return builder.toString();
    }

    /**
     * Calculate the average of those results that have the same parameter
     * in the same order
     * @param r1 returned Results will inherit its name and units
     * @param weightOnr1 How many times should r1 be taken?
     * @return null if number of parameters is not equal, else the weighted average
     */
    public static Results average(Results r1, Results r2, int weightOnr1) {
        int size = r1.parameterList.size();
        if (size != r2.parameterList.size()) return null;
        Results ravg = new Results(r1.name, r1.paramUnit, r1.resultUnitCPP, r1.resultUnitJava, r1.resultDecimalOffset);
        if (!ravg.name.endsWith("-average"))
            ravg.name += "-average";
        for (int i = 0; i < size; i++) {
            if (r1.parameterList.get(i).equals(r2.parameterList.get(i))) {
                ravg.parameterList.addLast(r1.parameterList.get(i));
                ravg.resultListCPP.addLast((r1.resultListCPP.get(i) * weightOnr1 +
                        r2.resultListCPP.get(i)) / ( 1 + weightOnr1 ));
                ravg.resultListJava.addLast((r1.resultListJava.get(i) * weightOnr1 +
                        r2.resultListJava.get(i)) / ( 1 + weightOnr1 ));
            }
        }
        return ravg;
    }

    /**
     * Calculate the average of those results that have the same parameter
     * in the same order, both are of the same weight
     * @param r1 returned Results will inherit its name and units
     * @return null if number of parameters is not equal, else the average
     */
    public static Results average(Results r1, Results r2) {
        return average(r1, r2, 1);
    }
}
