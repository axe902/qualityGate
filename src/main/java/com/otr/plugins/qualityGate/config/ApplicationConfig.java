package com.otr.plugins.qualityGate.config;

import com.otr.plugins.qualityGate.exceptions.ApplicationStartException;
import com.otr.plugins.qualityGate.exceptions.ResourceLoadingException;
import com.otr.plugins.qualityGate.model.gitlab.GitLabSettings;
import com.otr.plugins.qualityGate.model.jira.JiraSettings;
import com.otr.plugins.qualityGate.model.mail.MailSettings;
import com.otr.plugins.qualityGate.service.gitlab.GitLabApiExecutor;
import com.otr.plugins.qualityGate.service.jira.JiraTaskService;
import com.otr.plugins.qualityGate.service.jira.NonVerifyingJiraClient;
import com.otr.plugins.qualityGate.service.mail.SendMailService;
import com.otr.plugins.qualityGate.utils.PropertyUtils;
import com.otr.plugins.qualityGate.utils.ResourceLoader;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.context.support.StandardServletEnvironment;

import java.util.Set;

@Getter
@Setter
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties(prefix = "quality-gate")
@Configuration
@Slf4j
public class ApplicationConfig {

    @Autowired
    StandardServletEnvironment env;

    String rootSettingsPath;
    String jiraSettingsPath;
    String mailSettingsPath;
    String gitlabSettingsPath;
    boolean startCommandLineRunner;

    Set<String> issueTypes;
    Set<String> issueLinks;

    @Bean
    public NonVerifyingJiraClient jiraClient() throws ApplicationStartException {
        try {
            JiraSettings settings = ResourceLoader.loadJsonFile(rootSettingsPath, jiraSettingsPath, JiraSettings.class);
            return new NonVerifyingJiraClient(settings.url(), settings.createCredentials());
        } catch (ResourceLoadingException e) {
            log.error(e.getMessage(), e);
            throw new ApplicationStartException(e.getMessage(), e);
        }
    }

    @Bean
    public JiraTaskService jiraTaskService() throws ApplicationStartException {
        return new JiraTaskService(issueTypes, issueLinks, jiraClient());
    }


    @Bean
    public GitLabApiExecutor gitLabApiExecutor() throws ApplicationStartException {
        try {

            GitLabSettings settings = ResourceLoader.loadJsonFile(rootSettingsPath, gitlabSettingsPath, GitLabSettings.class);
            return new GitLabApiExecutor(settings);
        } catch (ResourceLoadingException e) {
            log.error(e.getMessage(), e);
            throw new ApplicationStartException(e.getMessage(), e);
        }
    }

    @Bean
    public SendMailService sendMailService() {
        MailSettings mailSettings;
        try {
            mailSettings = ResourceLoader.loadJsonFile(rootSettingsPath, mailSettingsPath, MailSettings.class);
            mailSettings.setUsername(PropertyUtils.resolve(mailSettings.getUsername(), env));
            mailSettings.setPassword(PropertyUtils.resolve(mailSettings.getPassword(), env));
            mailSettings.setSender(PropertyUtils.resolve(mailSettings.getSender(), env));
            if (CollectionUtils.isEmpty(mailSettings.getRecipients())) {
                mailSettings.setRecipients(PropertyUtils.convertToList(env.getProperty("email.to")));
            }
        } catch (ResourceLoadingException e) {
            // If the settings file is missing, we simply disable sending notifications
            log.warn(e.getMessage(), e);
            mailSettings = new MailSettings(false);
        }

        return new SendMailService(mailSettings);
    }
}
