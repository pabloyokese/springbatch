package com.jp.batchexample;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class JobConfiguration {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public ListItemReader<Good> itemReader() {
        int inicialCapacity = 100000;
        List<Good> items = new ArrayList<>(inicialCapacity);
        for (int i = 1; i <= inicialCapacity; i++) {
            items.add(new Good(Long.valueOf(i),"b","location good "+i));
        }
        return new ListItemReader<>(items);
    }

    @Bean
    SysOutItemWriter itemWriter() {
        return new SysOutItemWriter();
    }

    @Bean
    public StaxEventItemWriter<Good> goodItemWriter() throws Exception {
        XStreamMarshaller marshaller = new XStreamMarshaller();
        Map<String, Class> aliases = new HashMap<>();
        aliases.put("good", Good.class);
        marshaller.setAliases(aliases);

        StaxEventItemWriter<Good> itemWriter = new StaxEventItemWriter<>();
        itemWriter.setRootTagName("goods");
        itemWriter.setMarshaller(marshaller);
//        String goodOutputPath = File.createTempFile("goods",".xml").getAbsolutePath();
//////        /Users/pablo/IdeaProjects/batchexample
////        File file = new File("/Users/pablo/IdeaProjects/batchexample/good.xml");
////        System.out.println(">> Output path:"  + goodOutputPath);
        itemWriter.setResource(new FileSystemResource("src/main/resources/good.xml"));
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

//    @Bean
//    public ClassifierCompositeItemWriter<Good> classifierCompositeItemProcessor(){
//        ClassifierCompositeItemWriter<Good> itemWriter = new ClassifierCompositeItemWriter<>();
//        itemWriter.setClassifier(null);
//        return itemWriter;
//    }

    @Bean
    public RangePartitioner partitioner(){
        RangePartitioner partitioner = new RangePartitioner();
        partitioner.setMax(100000);
        partitioner.setMin(1);
        return partitioner;
    }

    @Bean
    public Step step() throws Exception {
//        return stepBuilderFactory.get("step")
//                .<Good, Good>chunk(50000)
//                .reader(itemReader())
//                .writer(goodItemWriter())
//                .build();
        return stepBuilderFactory.get("step1")
                .partitioner(slaveStep().getName(), partitioner())
                .step(slaveStep())
                .gridSize(4)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Step slaveStep() throws Exception {
        return stepBuilderFactory.get("slaveStep")
                .<Good,Good>chunk(1000)
                .reader(itemReader())
                .writer(goodItemWriter())
                .build();
    }

    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("job")
                .incrementer(new RunIdIncrementer())
                .start(step())
                .build();
    }
}