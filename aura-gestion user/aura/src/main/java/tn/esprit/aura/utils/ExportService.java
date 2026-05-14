package tn.esprit.aura.utils;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import tn.esprit.aura.entities.User;

import java.io.FileOutputStream;
import java.util.List;
import java.awt.Color;

public class ExportService {

    public static void exportUserToPDF(User user, String filePath) throws Exception {
        Document document = new Document(PageSize.A5);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLACK);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

        // Header
        Paragraph title = new Paragraph("AURA - FICHE UTILISATEUR", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        Paragraph separator = new Paragraph("--------------------------------------------------", subtitleFont);
        separator.setAlignment(Element.ALIGN_CENTER);
        document.add(separator);
        document.add(new Paragraph("\n"));

        // Table for details
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        addCell(table, "Nom complet", boldFont);
        addCell(table, user.getFullName(), normalFont);

        addCell(table, "Email", boldFont);
        addCell(table, user.getEmail(), normalFont);

        addCell(table, "Role", boldFont);
        addCell(table, user.getRole(), normalFont);

        addCell(table, "Telephone", boldFont);
        addCell(table, user.getTelephone() == null ? "N/A" : user.getTelephone(), normalFont);

        addCell(table, "Ville", boldFont);
        addCell(table, user.getVille() == null ? "N/A" : user.getVille(), normalFont);

        addCell(table, "Genre", boldFont);
        addCell(table, user.getGenre() == null ? "N/A" : user.getGenre(), normalFont);

        addCell(table, "Statut", boldFont);
        addCell(table, user.isActive() ? "Actif" : "Inactif", normalFont);

        document.add(table);

        // Footer
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph("Genere par le systeme admin AURA", subtitleFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
    }

    private static void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    public static void exportUsersToExcel(List<User> users, String filePath) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("AURA Utilisateurs");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Nom complet", "Email", "Role", "Telephone", "Ville", "Statut"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate Data Rows
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getFullName());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getRole());
                row.createCell(4).setCellValue(user.getTelephone() == null ? "" : user.getTelephone());
                row.createCell(5).setCellValue(user.getVille() == null ? "" : user.getVille());
                row.createCell(6).setCellValue(user.isActive() ? "Actif" : "Inactif");
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }
}
