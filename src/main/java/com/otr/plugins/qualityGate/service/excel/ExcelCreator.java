package com.otr.plugins.qualityGate.service.excel;

import com.otr.plugins.qualityGate.service.handler.Handler;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.awt.Color.lightGray;

@Component
@Slf4j
public class ExcelCreator {

    private static final List<String> COLUMNS_DEFAULT = List.of(
            "commit id", "commit title", "commit message", "Задачи из текста коммита", "Найденная задача в jira", "Тип", "Статус", "Патч", "Доработка", "Автор коммита"
    );
    private static final List<String> COLUMNS_ERROR = List.of(
            "commit id", "commit title", "commit message", "Автор коммита"
    );
    private static final Map<String, String> HUMAN_NAMES_COLUMN_MAP = Map.of(
            "commit id", "id",
            "commit title", "title",
            "commit message", "message",
            "Задачи из текста коммита", "issues",
            "Найденная задача в jira", "key",
            "Тип", "type",
            "Статус", "status",
            "Патч", "patch",
            "Доработка", "source",
            "Автор коммита", "committer_name"
    );

    public String create(Map<Handler.ResulType, Handler.Result> content) {
        final String fileName = "./" + UUID.randomUUID() + ".xls";
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            content.forEach((k, v) -> {
                final HSSFSheet sheet = workbook.createSheet(k.name());
                setColumnWidth(sheet);
                // счетчик для строк
                int rowNum = 0;

                // создаем подписи к столбцам (это будет первая строчка в листе Excel файла)
                Row row = sheet.createRow(rowNum);
                final CellStyle style = createHeaderStyle(workbook);
                final CellStyle rowStyle = createRowStyle(workbook);
                final CellStyle patchRowStyle = createPatchRowStyle(workbook);

                List<String> columns = Handler.ResulType.ERROR.equals(k) ? COLUMNS_ERROR : COLUMNS_DEFAULT;
                for (int i =0; i < columns.size(); i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellStyle(style);
                    cell.setCellValue(columns.get(i));
                }

                // заполняем лист данными
                for (Map<String, String> data : v.getContent()) {
                    createSheetHeader(columns, rowStyle, patchRowStyle, sheet, ++rowNum, data);
                }

                try (FileOutputStream out = new FileOutputStream(fileName)) {
                    workbook.write(out);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            });

        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }

        return fileName;

    }

    private void createSheetHeader(List<String> columns, CellStyle cellStyle, CellStyle patchRowStyle, HSSFSheet sheet, int rowNum, Map<String, String> data) {
        Row row = sheet.createRow(rowNum);
        IntStream.range(0, columns.size()).forEach(i -> {
            String dataSystemName = HUMAN_NAMES_COLUMN_MAP.get(columns.get(i));

            Cell cell = row.createCell(i);
            cell.setCellStyle("patch".equals(dataSystemName) ? patchRowStyle : cellStyle);
            String content = data.get(dataSystemName);
            cell.setCellValue(content);
        });
    }

    private void setColumnWidth(HSSFSheet sheet) {
        int colNum = 0;

        sheet.setColumnWidth(colNum++, 41 * 256);     //id
        sheet.setColumnWidth(colNum++, 150 * 256);    //title
        sheet.setColumnWidth(colNum++, 150 * 256);    //message
        sheet.setColumnWidth(colNum++, 30 * 256);     //issues
        sheet.setColumnWidth(colNum++, 30 * 256);     //key
        sheet.setColumnWidth(colNum++, 23 * 256);     //type
        sheet.setColumnWidth(colNum++, 15 * 256);     //status
        sheet.setColumnWidth(colNum++, 12 * 256);     //patch
        sheet.setColumnWidth(colNum++, 12 * 256);     //source
        sheet.setColumnWidth(colNum++, 33 * 256);     //committer_name
    }

    private CellStyle createRowStyle(Workbook workbook) {
        return workbook.createCellStyle();
    }

    private CellStyle createPatchRowStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Courier New");
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillBackgroundColor(new HSSFColor(0, 1, lightGray));
        return style;
    }
}
