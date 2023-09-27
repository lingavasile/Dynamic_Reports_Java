package com.vasile.linga;

import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabMeasureBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;



public class CrossTabReport {
    public CrossTabReport() {
        build();
    }

    private static void build() {
        try {
            StyleBuilder boldStyle = stl.style().bold();
            StyleBuilder boldLeftStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);


            CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("country", String.class).setTotalHeader("Total").setHeaderWidth(90);

            CrosstabColumnGroupBuilder<Integer> columnGroup = ctab.columnGroup("month", Integer.class).setTotalHeaderWidth(30).setHeaderHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
            CrosstabMeasureBuilder<Object> quantityMeasure = ctab.measure("quantity", Integer.class, Calculation.SUM).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);

            StyleBuilder cellstyle = stl.style().setFontSize(10).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
            StyleBuilder collumnstyle = stl.style().setFontSize(10).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
            StyleBuilder rowstyle = stl.style().setFontSize(10).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
            StyleBuilder titlestyle = stl.style().setFontSize(10).setBottomPadding(20);

            quantityMeasure.setStyle(cellstyle);
            columnGroup.setHeaderStyle(collumnstyle);
            rowGroup.setHeaderStyle(rowstyle);

            CrosstabBuilder crosstab = ctab.crosstab()
                    .headerCell(cmp.text("State / Month").setStyle(boldLeftStyle))
                    .rowGroups(rowGroup)
                    .columnGroups(columnGroup).setCellWidth(20)
                    .measures(quantityMeasure);




            report()
                    .setPageFormat(PageType.A4, PageOrientation.LANDSCAPE)
                    .title(cmp.text("Crosstab").setStyle(titlestyle))
                    .summary(crosstab)
                    .setDataSource(createDataSource())
                    .show();

            System.out.println("CrossTabReport successfully created");
        }catch (DRException e){
            System.out.println(e.getMessage());
        }
    }


    private static JRDataSource createDataSource() {

        try {
            DRDataSource dataSource = new DRDataSource("country","month","quantity");
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
            String url = "jdbc:sqlserver://CEDINT859;databaseName=HolydaysDate;integratedSecurity=true;trustServerCertificate=true";
            Connection conn = DriverManager.getConnection(url);

            Statement sta = conn.createStatement();
            for (int i = 1; i <= 12;i++){
                String sqlMonth = "select Country,Count(Country) as Total from Holydays where Month(Date) ="+i+" group by Country";
                ResultSet rsMonth = sta.executeQuery(sqlMonth);
                if (rsMonth.isBeforeFirst()){
                    while (rsMonth.next()) {
                        dataSource.add(rsMonth.getString("Country"),i,Integer.parseInt(rsMonth.getString("Total")));
                    }
                }else {
                    dataSource.add("Italia",i,0);
                    dataSource.add("Moldavia",i,0);
                }

            }

            return dataSource;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public static void main(String[] args) {
        new CrossTabReport();
    }
}
