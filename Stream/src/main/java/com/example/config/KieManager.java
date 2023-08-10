package com.example.config;

import com.example.entity.Rule;
import com.example.mapper.RuleMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.StringReader;
import java.util.List;

/**
 * 实现规则动态更新的另一种方案：从数据库中获取规则内容（不建议）
 * 该方式不推荐使用，
 */
@Component
public class KieManager {

    @Resource
    private RuleMapper ruleMapper;

    InternalKnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();

    KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

    /**
     * 从数据库中获取规则并生成KieSession
     * @return kieSession
     */
    public KieSession getKieSession() {
        List<Rule> rules = ruleMapper.selectList(null);

        for (int i = 0; ObjectUtils.isNotEmpty(rules) && i < rules.size(); i++) {
            Rule rule = rules.get(i);
            if(StringUtils.isNoneBlank(rule.getValue())){
                knowledgeBuilder.add(ResourceFactory.newReaderResource(new StringReader(rule.getValue())), ResourceType.DRL);
                knowledgeBase.addPackages(knowledgeBuilder.getKnowledgePackages());
            }
        }

        return knowledgeBase.newKieSession();
    }
}
