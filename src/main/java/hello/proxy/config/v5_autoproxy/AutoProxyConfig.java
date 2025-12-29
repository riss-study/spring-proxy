package hello.proxy.config.v5_autoproxy;

import hello.proxy.config.AppV1Config;
import hello.proxy.config.AppV2Config;
import hello.proxy.config.v3_proxyfactory.advice.LogTraceAdvice;
import hello.proxy.trace.logtrace.LogTrace;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Bean Post Processor 는 이미 spring boot aop 에서 자동으로 등록 돼있음
 * 이 자동 프록시 생성기(AnnotationAwareAspectJAutoProxyCreator)가 advisor 를 자동으로 다 찾아오므로
 * advisor 만 bean 등록해주면 끝
 *
 * pointcut 기능
 *  1. 프록시 적용 여부 판단 - 생성 단계: 빈 후처리기에서 해당 빈을 프록시로 생성할 건지 필터링 체크
 *      클래스 + 메서드 조건 모두 비교하여 포인트컷 조건에 하나하나 매칭하여 조건에 맞는 게 하나라도 있으면 프록시 생성
 *      v1, v2, v3 controller, service, repository에
 *      각각 request(), order(), save() 메서드가 있으므로 모두 프록시로 생성
 *  2. 어드바이스 적용 여부 판단 - 사용 단계: 프록시 메서드 호출 시, 해당 메서드가 advise 를 적용할 건지 프록시 내부에서 체크
 *      생성이 끝나면 v1, v2, v3 controller, service, repository 는 모두 프록시 빈임
 *      request, order, save 메서드 호출 시 현재 포인트 컷 조건에 만족하므로 advice (logTraceAdvice) 실행 후 target 메서드 실행
 *      no-log 는 현재 포인트컷 조건에 만족하지 않으므로 바로 타켓 메서드만 실행
 *
 *      참고: 모든 곳에 프록시 생성은 비용 낭비이므로, 최소한의 프록시를 적용.
 *      자동 프록시 생성기는 포인트컷으로 필터링해서 어드바이스 사용 가능성이 있는 곳에만 프록시 생성
 *
 *  문제점
 *      근데 이렇게만 하면, AppVxConfig.orderXXXVx() 에 모두 이 포인트컷 조건 만족으로 어드바이스 실행됨 (Config 이므로 스프링 실행 시점에 빈 등록 시)
 *      EnableWebMvcConfiguration.requestMappingHandlerAdapter() 에도 찍혀있음
 */
@Configuration
@Import({ AppV1Config.class, AppV2Config.class })
public class AutoProxyConfig {

  @Bean
  public Advisor advisor1(LogTrace logTrace) {

    NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
    pointcut.setMappedNames("request*", "order*", "save*");

    LogTraceAdvice advice = new LogTraceAdvice(logTrace);
    return new DefaultPointcutAdvisor(pointcut, advice);
  }

}
