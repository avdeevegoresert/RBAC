package com.rbac.util;

import java.util.List;

public class FormatUtils {
    
    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return "";
        }
        
        int[] colWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].length();
        }
        
        for (String[] row : rows) {
            for (int i = 0; i < row.length && i < colWidths.length; i++) {
                if (row[i] != null && row[i].length() > colWidths[i]) {
                    colWidths[i] = row[i].length();
                }
            }
        }
        
        StringBuilder sb = new StringBuilder();
        
        String separator = "+";
        for (int width : colWidths) {
            separator += repeat("-", width + 2) + "+";
        }
        sb.append(separator).append("\n");
        
        sb.append("|");
        for (int i = 0; i < headers.length; i++) {
            sb.append(" ").append(padRight(headers[i], colWidths[i])).append(" |");
        }
        sb.append("\n").append(separator).append("\n");
        
        for (String[] row : rows) {
            sb.append("|");
            for (int i = 0; i < row.length && i < colWidths.length; i++) {
                sb.append(" ").append(padRight(row[i] != null ? row[i] : "", colWidths[i])).append(" |");
            }
            sb.append("\n");
        }
        
        sb.append(separator).append("\n");
        return sb.toString();
    }
    
    public static String formatBox(String text) {
        String[] lines = text.split("\n");
        int maxLen = 0;
        for (String line : lines) {
            maxLen = Math.max(maxLen, line.length());
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("+" + repeat("-", maxLen + 2) + "+\n");
        for (String line : lines) {
            sb.append("| " + padRight(line, maxLen) + " |\n");
        }
        sb.append("+" + repeat("-", maxLen + 2) + "+\n");
        return sb.toString();
    }
    
    public static String formatHeader(String text) {
        return "\n--- " + text + " ---\n";
    }
    
    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    public static String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return text + repeat(" ", length - text.length());
    }
    
    public static String padLeft(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return repeat(" ", length - text.length()) + text;
    }
    
    private static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}