package com.jp.batchexample;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
        int inicialCapacity = 10;
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
    @StepScope
    SysOutItemWriter itemWriter2(@Value("#{stepExecutionContext[maxValue]}") Integer maxValue,
                                 @Value("#{stepExecutionContext[minValue]}") Integer minValue) {
        String fileName = minValue + "-" + maxValue;
        System.out.println(fileName);
        return new SysOutItemWriter();
    }


    @Bean
    @StepScope
    public StaxEventItemWriter<Good> goodItemWriter(@Value("#{stepExecutionContext[maxValue]}") Integer maxValue,
                                                    @Value("#{stepExecutionContext[minValue]}") Integer minValue) throws Exception {
        String fileName = minValue + "-" + maxValue;
        System.out.println(fileName);
        XStreamMarshaller marshaller = new XStreamMarshaller();
        Map<String, Class> aliases = new HashMap<>();
        aliases.put("good", Good.class);
        marshaller.setAliases(aliases);
        StaxEventItemWriter<Good> itemWriter = new StaxEventItemWriter<>();
        itemWriter.setRootTagName("goods");
        itemWriter.setMarshaller(marshaller);
        itemWriter.setResource(new FileSystemResource("src/main/resources/"+fileName+".xml"));
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Good> flatFileItemWriter(@Value("#{stepExecutionContext[maxValue]}") Integer maxValue,
                                                       @Value("#{stepExecutionContext[minValue]}") Integer minValue) throws Exception {
        String fileName = minValue + "-" + maxValue;
        System.out.println(fileName);
        System.out.println(Thread.currentThread().getName());

        FlatFileItemWriter itemWriter = new FlatFileItemWriter();
        itemWriter.setAppendAllowed(false);
        DelimitedLineAggregator agregator = new DelimitedLineAggregator();
        BeanWrapperFieldExtractor<Good> wrapper= new BeanWrapperFieldExtractor<>();
        agregator.setDelimiter(",");
        wrapper.setNames(new String[]{"id","goodMaster","location"});
        agregator.setFieldExtractor(wrapper);

        itemWriter.setLineAggregator(agregator);

        itemWriter.setResource(new FileSystemResource("src/main/resources/"+fileName+".txt"));

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
        partitioner.setMax(10);
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
                .<Good,Good>chunk(2)
                .reader(itemReader())
                .writer(itemWriter2(null,null))
//                .writer(itemWriter())
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