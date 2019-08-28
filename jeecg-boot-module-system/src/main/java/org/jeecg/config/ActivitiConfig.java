package org.jeecg.config;

import javax.sql.DataSource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ActivitiConfig 
{
	@Autowired
    private DataSource dataSource;
    @Autowired
    private PlatformTransactionManager platformTransactionManager;
    
    @Bean
	public SpringProcessEngineConfiguration springProcessEngineConfiguration(){
		SpringProcessEngineConfiguration spec = new SpringProcessEngineConfiguration();
        spec.setDataSource(dataSource);
        spec.setTransactionManager(platformTransactionManager);
		spec.setDatabaseSchemaUpdate("true");
//        spec.setDatabaseSchemaUpdate("drop-create");
		return spec;
	}
    
    @Bean
	public ProcessEngineFactoryBean processEngine() {
		ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
		processEngineFactoryBean.setProcessEngineConfiguration(springProcessEngineConfiguration());
		return processEngineFactoryBean;
	}
	
	@Bean
	public RepositoryService repositoryService() throws Exception {
		return processEngine().getObject().getRepositoryService();
	}
	@Bean
	public RuntimeService runtimeService() throws Exception {
		return processEngine().getObject().getRuntimeService();
	}
	@Bean
	public TaskService taskService() throws Exception {
		return processEngine().getObject().getTaskService();
	}
	@Bean
	public HistoryService historyService() throws Exception {
		return processEngine().getObject().getHistoryService();
	}
    
//    @Override
//    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
//        processEngineConfiguration.setActivityFontName("宋体");
//        processEngineConfiguration.setLabelFontName("宋体");
//        processEngineConfiguration.setAnnotationFontName("宋体");
//        
//        // 是否使用activiti自带用户组织表
//        processEngineConfiguration.setDbIdentityUsed(false);
//        // 启动的时候是否去创建表，如果第一次启动这里必须设置为true
//        processEngineConfiguration.setDatabaseSchemaUpdate("true");
//    }
}
