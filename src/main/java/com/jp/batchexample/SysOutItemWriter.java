package com.jp.batchexample;

import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class SysOutItemWriter extends StepListenerSupport implements ItemWriter<Good> {

    @Override
    public void write(List<? extends Good> items) throws Exception {
        System.out.println("The size of the chuck is " + items.size());
        for (Good item: items){
            System.out.println(">> "+ item);
        }
    }
}
