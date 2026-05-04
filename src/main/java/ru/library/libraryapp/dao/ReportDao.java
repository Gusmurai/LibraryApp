package ru.library.libraryapp.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Интерфейс для формирования аналитической отчетности.
 */
public interface ReportDao {

    /**
     * Универсальный метод для получения данных отчёта.
     * Возвращает список строк, где каждая строка — это Map (Имя колонки -> Значение).
     * Это позволяет динамически заполнять таблицу в UI.
     *
     * @param reportType тип отчета из ComboBox
     * @param start дата начала периода
     * @param end дата конца периода
     */
    List<Map<String, Object>> generateReport(String reportType, LocalDate start, LocalDate end);

    /**
     * Метод для экспорта текущих данных таблицы в файл (например, CSV или Excel).
     * @param data данные для экспорта
     * @param filePath путь к файлу
     */
    void exportToExcel(List<Map<String, Object>> data, String filePath);

    /**
     * Экспорт данных отчета в формат PDF (удобно для печати).
     */
    void exportToPdf(List<Map<String, Object>> data, String path);
}