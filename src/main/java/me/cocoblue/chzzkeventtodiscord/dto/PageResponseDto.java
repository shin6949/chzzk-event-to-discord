package me.cocoblue.chzzkeventtodiscord.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseDto<T>(
    List<T> content,
    int number,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
    public static <T> PageResponseDto<T> from(Page<T> page) {
        return new PageResponseDto<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}
