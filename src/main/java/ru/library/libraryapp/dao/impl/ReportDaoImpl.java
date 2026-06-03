package ru.library.libraryapp.dao.impl;

import lombok.extern.slf4j.Slf4j;
import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.ReportDao;
import ru.library.libraryapp.dao.SqlProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ReportDaoImpl implements ReportDao {

    @Override
    public List<Map<String, Object>> generateReport(String reportType, LocalDate start, LocalDate end) {
        List<Map<String, Object>> reportData = new ArrayList<>();
        ReportQuery query = reportQuery(reportType);

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.sql())) {
            if (query.withPeriod()) {
                ps.setDate(1, Date.valueOf(start));
                ps.setDate(2, Date.valueOf(end));
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnLabel(i), rs.getObject(i));
                    }
                    reportData.add(row);
                }
            }
        } catch (SQLException e) {
            log.error("Не удалось сформировать отчет {}", reportType, e);
            throw new RuntimeException(e.getMessage(), e);
        }
        return reportData;
    }

    private ReportQuery reportQuery(String reportType) {
        return switch (reportType) {
            case "reader_activity" -> new ReportQuery(SqlProvider.get("report.reader_activity"), true);
            case "debtors" -> new ReportQuery(SqlProvider.get("report.debtors"), false);
            case "popular_books" -> new ReportQuery(SqlProvider.get("report.popular_books"), true);
            case "unpaid_fines" -> new ReportQuery(SqlProvider.get("report.unpaid_fines"), true);
            case "fund_state" -> new ReportQuery(SqlProvider.get("report.fund_state"), false);
            case "write_offs" -> new ReportQuery(SqlProvider.get("report.write_offs"), true);
            case "deliveries" -> new ReportQuery(SqlProvider.get("report.deliveries"), true);
            default -> throw new IllegalArgumentException("Unknown report type: " + reportType);
        };
    }

    @Override
    public void exportToExcel(List<Map<String, Object>> data, String filePath) {
        exportCsv(data, filePath);
    }

    @Override
    public void exportToPdf(List<Map<String, Object>> data, String path) {
        exportCsv(data, path);
    }

    private void exportCsv(List<Map<String, Object>> data, String filePath) {
        if (data == null || data.isEmpty()) {
            return;
        }
        List<String> lines = new ArrayList<>();
        List<String> headers = new ArrayList<>(data.get(0).keySet());
        lines.add(toCsvLine(headers));
        for (Map<String, Object> row : data) {
            lines.add(toCsvLine(headers.stream()
                    .map(header -> row.get(header) == null ? "" : row.get(header).toString())
                    .toList()));
        }
        try {
            Files.write(Path.of(filePath), lines);
        } catch (IOException e) {
            log.error("Не удалось экспортировать отчет {}", filePath, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String toCsvLine(List<String> values) {
        return values.stream()
                .map(value -> "\"" + value.replace("\"", "\"\"") + "\"")
                .reduce((left, right) -> left + ";" + right)
                .orElse("");
    }

    private record ReportQuery(String sql, boolean withPeriod) {
    }
}
