package com.jp.batchexample;

import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class SysOutItemWriter extends StepListenerSupport implements ItemWriter<String> {

    @Override
    public void write(List<? extends String> items) throws Exception {
        System.out.println("The size of the chuck is " + items.size());
        for (String item: items){
            System.out.println(">> "+ item);
        }
    }
}
