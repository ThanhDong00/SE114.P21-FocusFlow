package com.dong.focusflow.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Room TypeConverter để chuyển đổi các đối tượng LocalDateTime sang và từ dạng chuỗi.
 */
class LocalDateTimeConverter {
    // Định nghĩa formatter theo chuẩn ISO_LOCAL_DATE_TIME.
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    /**
     * Chuyển đổi một chuỗi timestamp từ cơ sở dữ liệu thành đối tượng LocalDateTime.*/
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        // Nếu giá trị không null, phân tích cú pháp nó bằng formatter đã định nghĩa.
        return value?.let { LocalDateTime.parse(it, formatter) }
    }

    /**
     * Chuyển đổi đối tượng LocalDateTime thành dạng chuỗi để lưu trữ trong cơ sở dữ liệu.*/
    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        // Nếu ngày không null, định dạng nó thành một chuỗi bằng formatter đã định nghĩa.
        return date?.format(formatter)
    }
}