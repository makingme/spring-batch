package org.kkb.listener;

import org.kkb.model.KoreanFoodStore;
import org.kkb.util.AESEncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemReadListener;

public class CsvItemReadListener<T> implements ItemReadListener<T> {
    private static final Logger log = LoggerFactory.getLogger(CsvItemReadListener.class);

    public CsvItemReadListener(String taskName) {
        this.taskName = taskName;
    }

    private final String taskName;

    @Override
    public void beforeRead() {
    }

    @Override
    public void afterRead(T item) {
        log.info("{} Read Item: {}", taskName, item);
    }

    @Override
    public void onReadError(Exception ex) {
        log.error("{} Read Error: {}", taskName, ex.getMessage());
    }


}
