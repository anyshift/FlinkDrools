package com.example.config;

import com.example.util.KieUtil;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;

@Configuration
public class DroolsAutoConfiguration {

    private static final String RULES_PATH = "rules/";

    private KieServices getKieServices() {
        KieServices kieServices = KieServices.Factory.get();
        KieUtil.kieServices = kieServices;
        return kieServices;
    }

    // 获取所有规则文件
    private Resource[] getRuleFiles() throws IOException {
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        return resourcePatternResolver.getResources("classpath:" + RULES_PATH + "**/*.*");
    }

    // 虚拟的文件系统，不会真实写入硬盘，而是写入内存中。由于采用了规则文件与业务分离的方案，所以此处的 KieFileSystem 用不上了。
    @Bean
    @ConditionalOnMissingBean(KieFileSystem.class)
    public KieFileSystem kieFileSystem() throws IOException {
        KieFileSystem kieFileSystem = this.getKieServices().newKieFileSystem();
        for (Resource file : getRuleFiles()) {
            kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_PATH + file.getFilename(), "UTF-8"));
        }
        return kieFileSystem;
    }

    // 初始化 KieContainer
    @Bean
    @ConditionalOnMissingBean(KieContainer.class)
    public KieContainer kieContainer() {
        KieServices kieServices = this.getKieServices();

        // LATEST 和 RELEASE 可以获取到最新的正式版本，获取最新的快照版本需要用：[1.0-SNAPSHOT,)
        ReleaseId releaseId = kieServices.newReleaseId("com.drools", "DroolsRules", "LATEST");

        KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        KieScanner kieScanner = kieServices.newKieScanner(kieContainer);
        kieScanner.start(1000L);

        /*
         *  Flink 的一系列函数都需要静态处理，无法通过依赖注入的方式将 KieContainer 注入到 Flink 中的各种静态实现类中。
         *  这里需要用一个静态变量接收新初始化出来的 KieContainer，包括 KieScanner 自动更新后的 KieContainer 对象都要存入一个静态变量中。
         */
        KieUtil.kieContainer = kieContainer;

        return kieContainer;
    }

    @Bean
    @ConditionalOnMissingBean(KieBase.class)
    public KieBase kieBase() {
        return kieContainer().getKieBase();
    }
}
