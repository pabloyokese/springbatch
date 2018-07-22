package com.jp.batchexample;

import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

public class GoodClassifier implements Classifier<Good,ItemWriter<? super Good>> {
    @Override
    public ItemWriter<? super Good> classify(Good good) {
        return null;
    }
}
