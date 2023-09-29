package com.vasile.linga;

import com.google.common.io.Resources;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalImageAlignment;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;


public class HolydayReport {

    public HolydayReport() {
        build();
    }


    private static void build() {
        try {
            StyleBuilder boldStyle = stl.style().bold();
            StyleBuilder boldLeftStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
            StyleBuilder boldRightStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT);
            StyleBuilder columnTitleStyle = stl.style(boldLeftStyle)
                                                .setBorder(stl.pen1Point())
                                                .setBackgroundColor(Color.CYAN).setPadding(5).setFontSize(13);
            StyleBuilder columnStyle = stl.style()
                    .setBorder(stl.pen1Point())
                    .setPadding(5);
            BufferedImage img = null;
            img = ImageIO.read(getFile("holydaays.png"));
            report()
                    .title(
                            cmp.text("Holydays").setStyle(boldLeftStyle).setStyle(stl.style().setFontSize(15)),
                            cmp.image(img).setDimension(50,50).setHorizontalImageAlignment(HorizontalImageAlignment.RIGHT),
                            cmp.verticalGap(5))

                    .columns(
                            col.column("Country","country",type.stringType()),
                            col.column("Name","name",type.stringType()),
                            col.column("Date","date",type.stringType()))
                    .setColumnTitleStyle(columnTitleStyle.setPadding(8))
                    .setColumnStyle(columnStyle.setPadding(5))
                    .highlightDetailEvenRows()
                    .pageFooter(cmp.pageXofY().setStyle(boldRightStyle))
                    .setDataSource(createDataSource())
                    .show();
            System.out.println("Report successfully created");
        }catch (DRException e){
            System.out.println(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getFile(String name) {
        URL url = Resources.getResource(name);
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            file = new File(url.getPath());
        } finally {
            return file;
        }
    }

    private static JRDataSource createDataSource() {

        try {
            DRDataSource dataSource = new DRDataSource("country","name","date");
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
            String url = "jdbc:sqlserver://CEDINT859;databaseName=HolydaysDate;integratedSecurity=true;trustServerCertificate=true";
            Connection conn = DriverManager.getConnection(url);

            Statement sta = conn.createStatement();
            String Sql = "select * from Holydays";
            ResultSet rs = sta.executeQuery(Sql);

            while (rs.next()) {
                dataSource.add(rs.getString("Country"),rs.getString("Name"),rs.getString("Date"));
            }

            return dataSource;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public static void main(String[] args) {
        new HolydayReport();
    }
}