package com.dong.focusflow.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Room TypeConverter for converting LocalDateTime objects to and from String representation.
 * Room TypeConverter để chuyển đổi các đối tượng LocalDateTime sang và từ dạng chuỗi.
 */
class LocalDateTimeConverter {
    // Defines the formatter for ISO_LOCAL_DATE_TIME standard.
    // Định nghĩa formatter theo chuẩn ISO_LOCAL_DATE_TIME.
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    /**
     * Chuyển đổi một chuỗi timestamp từ cơ sở dữ liệu thành đối tượng LocalDateTime.
     * @param value The String representation of the LocalDateTime. Dạng chuỗi của LocalDateTime.
     * @return A LocalDateTime object, or null if the input value is null. Đối tượng LocalDateTime, hoặc null nếu giá trị đầu vào là null.
     */
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        // If the value is not null, parse it using the defined formatter.
        // Nếu giá trị không null, phân tích cú pháp nó bằng formatter đã định nghĩa.
        return value?.let { LocalDateTime.parse(it, formatter) }
    }

    /**
     * Converts a LocalDateTime object to a String representation for storage in the database.
     * Chuyển đổi đối tượng LocalDateTime thành dạng chuỗi để lưu trữ trong cơ sở dữ liệu.
     * @param date The LocalDateTime object to convert. Đối tượng LocalDateTime cần chuyển đổi.
     * @return A String representation of the LocalDateTime, or null if the input date is null. Dạng chuỗi của LocalDateTime, hoặc null nếu ngày đầu vào là null.
     */
    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        // If the date is not null, format it into a string using the defined formatter.
        // Nếu ngày không null, định dạng nó thành một chuỗi bằng formatter đã định nghĩa.
        return date?.format(formatter)
    }
}