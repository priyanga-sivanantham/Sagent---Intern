package com.project.eventplan.Util;

import com.project.eventplan.Dto.PageResponse;

import java.util.List;

public final class PaginationUtils {

    private PaginationUtils() {
    }

    public static <T> PageResponse<T> paginate(List<T> items, Integer page, Integer size) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size < 1 ? 20 : Math.min(size, 100);
        int totalItems = items.size();
        int fromIndex = Math.min(safePage * safeSize, totalItems);
        int toIndex = Math.min(fromIndex + safeSize, totalItems);
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / safeSize);
        return new PageResponse<>(items.subList(fromIndex, toIndex), safePage, safeSize, totalItems, totalPages);
    }
}
