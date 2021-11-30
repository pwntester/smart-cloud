package org.smartframework.cloud.starter.rpc.feign.autoconfigure;

import org.smartframework.cloud.starter.configure.constants.SmartConstant;
import org.smartframework.cloud.starter.configure.properties.SmartProperties;
import org.smartframework.cloud.starter.core.business.util.AspectInterceptorUtil;
import org.smartframework.cloud.starter.core.constants.PackageConfig;
import org.smartframework.cloud.starter.rpc.feign.annotation.SmartFeignClient;
import org.smartframework.cloud.starter.rpc.feign.interceptor.FeignLogInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * feign切面配置
 *
 * @author liyulin
 * @date 2019-07-03
 */
@Configuration
@ConditionalOnExpression(FeignAspectAutoConfiguration.FEIGN_ASPECT_CONDITION)
public class FeignAspectAutoConfiguration {

    /**
     * rpc切面生效条件
     */
    public static final String FEIGN_ASPECT_CONDITION = "${" + SmartConstant.FEIGN_LOG_CONDITION_PROPERTY + ":false}";

    /**
     * feign切面
     *
     * @return
     */
    @Bean
    public AspectJExpressionPointcut feignClientPointcut() {
        AspectJExpressionPointcut feignClientPointcut = new AspectJExpressionPointcut();

        String feignExpression = AspectInterceptorUtil.getFinalExpression(PackageConfig.getBasePackages(), AspectInterceptorUtil
                .getTypeExpression(Arrays.asList(FeignClient.class, SmartFeignClient.class)));
        feignClientPointcut.setExpression(feignExpression);
        return feignClientPointcut;
    }

    /**
     * feign日志
     *
     * @author liyulin
     * @date 2019年7月3日 下午3:58:48
     */
    @Configuration
    @ConditionalOnProperty(name = SmartConstant.FEIGN_LOG_CONDITION_PROPERTY, havingValue = "true")
    class FeignLogAutoConfigure {

        @Bean
        public FeignLogInterceptor feignInterceptor(final SmartProperties smartProperties) {
            return new FeignLogInterceptor(smartProperties.getLog());
        }

        @Bean
        public Advisor feignLogAdvisor(final FeignLogInterceptor feignInterceptor,
                                       final AspectJExpressionPointcut feignClientPointcut) {
            DefaultBeanFactoryPointcutAdvisor feignAdvisor = new DefaultBeanFactoryPointcutAdvisor();
            feignAdvisor.setAdvice(feignInterceptor);
            feignAdvisor.setPointcut(feignClientPointcut);

            return feignAdvisor;
        }

    }

}