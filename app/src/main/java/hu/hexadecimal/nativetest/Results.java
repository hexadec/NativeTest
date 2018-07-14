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

    Results(String name, String parameterUnit, String resultUnit, double decimalOffset) {
        this.name = name;
        parameterList = new LinkedList<>();
        resultList = new LinkedList<>();
        paramUnit = parameterUnit;
        this.resultUnit = resultUnit;
        resultDecimalOffset = Math.pow(10, decimalOffset);
    }

    int addPair(long parameter, long result) {
        parameterList.addLast(parameter);
        resultList.addLast(result);
        return parameterList.size();
    }

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
