package org.kkb.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;

import java.util.List;

public class CsvItemWriteListener<T> implements ItemWriteListener<T> {
    private static final Logger log = LoggerFactory.getLogger(CsvItemWriteListener.class);

    private final String taskName;

    public CsvItemWriteListener(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public void afterWrite(Chunk<? extends T> items) {
        List<?> itemList = items.getItems();
        List<?> skippedItems = items.getSkips();
        List<?> errorItems = items.getErrors();
        log.info("{} Writing Count:{}, Skip Count:{}, Error Count:{}", taskName, itemList.size(), skippedItems.size(), errorItems.size());
        log.debug("{} Writing Items:{}", taskName, itemList);
        log.debug("{} Writing Skips:{}", taskName, skippedItems);
        log.debug("{} Writing Errors:{}", taskName, errorItems);
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends T> items) {
        log.error("{} Write Error: {}", taskName, exception.getMessage());
    }
}
