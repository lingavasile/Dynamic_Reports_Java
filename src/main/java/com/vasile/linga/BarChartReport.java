package com.vasile.linga;

import net.sf.dynamicreports.report.builder.chart.BarChartBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.chart.DRIChartCustomizer;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;

import java.io.Serializable;
import java.sql.*;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

public class BarChartReport {
    private static String url = "jdbc:sqlserver://CEDINT859;databaseName=HolydaysDate;integratedSecurity=true;trustServerCertificate=true";
    private static int max = 0;
    public BarChartReport() {
        build();
    }

    private void build() {
        try {

            FontBuilder boldFont = stl.fontArialBold().setFontSize(12);
            FontBuilder boldFontCategory = stl.fontCourierNewBold().setFontSize(10);
            MaxMonth();
            TextColumnBuilder<String> countryColumn = col.column("country", "country", type.stringType());
            TextColumnBuilder<String> monthColumn = col.column("month", "month", type.stringType());
            TextColumnBuilder<Integer> quantityColumn = col.column("quantity", "quantity", type.integerType());

            BarChartBuilder chart = cht.barChart()
                            .customizers(new ChartCustomizer())
                    .setDimension(300,200)
                    .setTitle("Holydays")
                    .setTitleFont(boldFont)
                    .setCategory(monthColumn)
                    .series(
                            cht.serie(quantityColumn).setSeries(countryColumn))
                    .setValueAxisFormat(
                            cht.axisFormat().setRangeMaxValueExpression(max+1))
                    .setCategoryAxisFormat(
                            cht.axisFormat().setLabel("Country").setLabelFont(boldFontCategory)
                    );



            report()
                    .setPageFormat(PageType.A4, PageOrientation.LANDSCAPE)
                    .summary(chart)
                    .setDataSource(createDataSource())
                    .show();

            System.out.println("ChartReport successfully created");
        }catch (DRException e){
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private class ChartCustomizer implements DRIChartCustomizer, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public void customize(JFreeChart chart, ReportParameters reportParameters) {
            BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
            renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
            renderer.setBaseItemLabelsVisible(true);
        }
    }

    private static void MaxMonth() throws SQLException {

        for (int i = 0; i < 12; i++) {
            String sqlMonth = "select Country,Count(Country) as Total from Holydays where Month(Date) ="+i+" group by Country";
            Connection conn = DriverManager.getConnection(url);
            Statement sta = conn.createStatement();
            ResultSet rsMonth = sta.executeQuery(sqlMonth);
            if (rsMonth.isBeforeFirst()){
                while (rsMonth.next()) {
                    int total = Integer.parseInt(rsMonth.getString("Total"));
                  if (total>max){
                      max = total;
                  }
                }
            }
        }
    }

    private static JRDataSource createDataSource() {

        try {
            DRDataSource dataSource = new DRDataSource("country","month","quantity");
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
            Connection conn = DriverManager.getConnection(url);
            Statement sta = conn.createStatement();
            for (int i = 1; i <= 12;i++){
                String monthName="";
                switch (i){
                    case 1 : monthName = "JANUARY";break;
                    case 2 : monthName = "FEBRUARY";break;
                    case 3 : monthName = "MARCH";break;
                    case 4 : monthName = "APRIL";break;
                    case 5 : monthName = "MAY";break;
                    case 6 : monthName = "JUNE";break;
                    case 7 : monthName = "JULY";break;
                    case 8 : monthName = "AUGUST";break;
                    case 9 : monthName = "SEPTEMBER";break;
                    case 10 : monthName = "OCTOBER";break;
                    case 11 : monthName = "NOVEMBER";break;
                    case 12 : monthName = "DECEMBER";break;
                }

                String sqlMonth = "select Country,Count(Country) as Total from Holydays where Month(Date) ="+i+" group by Country";
                ResultSet rsMonth = sta.executeQuery(sqlMonth);
                if (rsMonth.isBeforeFirst()){
                    while (rsMonth.next()) {
                        dataSource.add(rsMonth.getString("Country"),monthName,Integer.parseInt(rsMonth.getString("Total")));
                    }
                }

            }

            return dataSource;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public static void main(String[] args) {
        new BarChartReport();
    }
}
