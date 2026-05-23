package ru.library.libraryapp.dao.impl;

import ru.library.libraryapp.DBHelper;
import ru.library.libraryapp.dao.ReportDao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDaoImpl implements ReportDao {

    @Override
    public List<Map<String, Object>> generateReport(String type, LocalDate start, LocalDate end) {
        List<Map<String, Object>> reportData = new ArrayList<>();
        String sql = getSqlByReportType(type);

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Если в запросе предусмотрены параметры дат
            if (sql.contains("?")) {
                ps.setDate(1, Date.valueOf(start != null ? start : LocalDate.of(1900, 1, 1)));
                ps.setDate(2, Date.valueOf(end != null ? end : LocalDate.now()));
            }

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    // Имя колонки -> Значение
                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                reportData.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reportData;
    }

    /**
     * Возвращает SQL-запрос в зависимости от выбранного типа отчета.
     */
    private String getSqlByReportType(String type) {
        switch (type) {
            case "Читательская активность":
                return "SELECT r.last_name || ' ' || r.first_name as \"Читатель\", " +
                        "COUNT(l.lending_id) as \"Книг взято\" " +
                        "FROM readers r JOIN lendings l ON r.ticket_number = l.ticket_number " +
                        "WHERE l.lend_date BETWEEN ? AND ? GROUP BY r.ticket_number ORDER BY 2 DESC";

            case "Список должников":
                return "SELECT r.last_name || ' ' || r.first_name as \"Читатель\", r.phone as \"Телефон\", " +
                        "b.title as \"Книга\", l.due_date as \"Срок был до\" " +
                        "FROM readers r JOIN lendings l ON r.ticket_number = l.ticket_number " +
                        "JOIN copies c ON l.inventory_number = c.inventory_number " +
                        "JOIN books b ON c.isbn = b.isbn " +
                        "WHERE l.return_date IS NULL AND l.due_date < CURRENT_DATE";

            case "Топ-20 популярных книг":
                return "SELECT b.title as \"Название\", COUNT(l.lending_id) as \"Раз выдана\" " +
                        "FROM books b JOIN copies c ON b.isbn = c.isbn " +
                        "JOIN lendings l ON c.inventory_number = l.inventory_number " +
                        "WHERE l.lend_date BETWEEN ? AND ? GROUP BY b.isbn ORDER BY 2 DESC LIMIT 20";

            case "Неоплаченные штрафы":
                return "SELECT r.last_name as \"Читатель\", f.amount as \"Сумма\", fa.name as \"Причина\" " +
                        "FROM fines f JOIN fine_articles fa ON f.article_id = fa.article_id " +
                        "JOIN lendings l ON f.lending_id = l.lending_id " +
                        "JOIN readers r ON l.ticket_number = r.ticket_number " +
                        "WHERE f.is_paid = false";

            case "Текущее состояние фонда":
                return "SELECT b.title as \"Книга\", " +
                        "(SELECT count(*) FROM copies WHERE isbn = b.isbn) as \"Всего\", " +
                        "(SELECT count(*) FROM copies c2 WHERE c2.isbn = b.isbn AND c2.inventory_number NOT IN " +
                        "(SELECT inventory_number FROM lendings WHERE return_date IS NULL)) as \"В наличии\" " +
                        "FROM books b";

            default:
                return "SELECT 'Выберите корректный тип отчета' as Error";
        }
    }

    @Override
    public void exportToExcel(List<Map<String, Object>> data, String path) {
        // Логика экспорта (например, через библиотеку Apache POI)
        System.out.println("Экспорт в Excel по пути: " + path);
        // Здесь будет код создания файла .xlsx
    }

    @Override
    public void exportToPdf(List<Map<String, Object>> data, String path) {
        // Логика экспорта (например, через iText)
        System.out.println("Экспорт в PDF по пути: " + path);
    }
}