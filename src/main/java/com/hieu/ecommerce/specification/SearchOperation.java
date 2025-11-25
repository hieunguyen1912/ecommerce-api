package com.hieu.ecommerce.specification;

public enum SearchOperation {
    /**
     * So sánh bằng (==)
     * Ví dụ: name = "Product A"
     */
    EQUALITY,

    /**
     * So sánh khác (!=)
     * Ví dụ: status != "DELETED"
     */
    NEGATION,

    /**
     * Lớn hơn (>)
     * Ví dụ: price > 100
     */
    GREATER_THAN,

    /**
     * Nhỏ hơn (<)
     * Ví dụ: stock < 10
     */
    LESS_THAN,

    /**
     * Lớn hơn hoặc bằng (>=)
     * Ví dụ: price >= 50
     */
    GREATER_THAN_OR_EQUAL,

    /**
     * Nhỏ hơn hoặc bằng (<=)
     * Ví dụ: stock <= 100
     */
    LESS_THAN_OR_EQUAL,

    /**
     * Chứa chuỗi (LIKE %value%)
     * Case-insensitive
     * Ví dụ: name chứa "laptop"
     */
    LIKE,

    /**
     * Bắt đầu bằng (LIKE value%)
     * Case-insensitive
     * Ví dụ: name bắt đầu bằng "Apple"
     */
    LIKE_START,

    /**
     * Kết thúc bằng (LIKE %value)
     * Case-insensitive
     * Ví dụ: name kết thúc bằng "Pro"
     */
    LIKE_END,

    /**
     * Trong danh sách (IN)
     * Ví dụ: status IN ("ACTIVE", "INACTIVE")
     */
    IN,

    /**
     * Không trong danh sách (NOT IN)
     * Ví dụ: status NOT IN ("DELETED")
     */
    NOT_IN,

    /**
     * Giá trị null
     * Ví dụ: description IS NULL
     */
    IS_NULL,

    /**
     * Giá trị không null
     * Ví dụ: description IS NOT NULL
     */
    IS_NOT_NULL,

    /**
     * So sánh bằng (case-sensitive)
     * Ví dụ: sku = "SKU-001" (chính xác)
     */
    EQUALITY_CASE_SENSITIVE,

    /**
     * Chứa chuỗi (case-sensitive)
     * Ví dụ: code chứa "ABC" (phân biệt hoa thường)
     */
    LIKE_CASE_SENSITIVE,
}
