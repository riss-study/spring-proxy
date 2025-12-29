package hello.proxy.config.v3_proxyfactory;

import hello.proxy.app.v2.OrderControllerV2;
import hello.proxy.app.v2.OrderRepositoryV2;
import hello.proxy.app.v2.OrderServiceV2;
import hello.proxy.config.v3_proxyfactory.advice.LogTraceAdvice;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 문제점1: V1, V2 둘다 결국 설정이 너무 많음
 *  - 스프링 빈이 100개 있으면, 100개의 동적 프록시 생성 코드를 만들어야 함
 *  - 빈 생성 코드 = (빈 직접 등록 + 프록시 적용 코드). 프록시 적용 코드는 거의 중복임
 * 문제점2: 컴포넌트 스캔 불가능
 *   - 지금까진 프록시를 스프링 빈으로 직접 등록했음
 *   - 컴포넌트 스캔 사용하면 스프링이 실제 객체를 스프링 빈으로 자동으로 바로 등록해버림
 *   - 부가기능이 있는 프록시를 내가 직접 등록할 수 없음
 *   => 빈 후처리기 사용하면 문제1, 2 해결
 */
@Slf4j
@Configuration
public class ProxyFactoryConfigV2 {

  @Bean
  public OrderControllerV2 orderControllerV2(LogTrace logTrace) {

    OrderControllerV2 orderController = new OrderControllerV2(orderServiceV2(logTrace));
    ProxyFactory factory = new ProxyFactory(orderController);
    factory.addAdvisor(getAdvisor(logTrace));
    OrderControllerV2 proxy = (OrderControllerV2) factory.getProxy();

    log.info("proxyFactory proxy: {}, target: {}", proxy.getClass(), orderController.getClass());
    // proxyFactory proxy: class hello.proxy.app.v2.OrderControllerV2$$EnhancerBySpringCGLIB$$831c9909, target: class hello.proxy.app.v2.OrderControllerV2

    return proxy;
  }

  @Bean
  public OrderServiceV2 orderServiceV2(LogTrace logTrace) {
    OrderServiceV2 orderService = new OrderServiceV2(orderRepositoryV2(logTrace));

    ProxyFactory factory = new ProxyFactory(orderService);
    factory.addAdvisor(getAdvisor(logTrace));
    OrderServiceV2 proxy = (OrderServiceV2) factory.getProxy();

    log.info("proxyFactory proxy: {}, target: {}", proxy.getClass(), orderService.getClass());
    // proxyFactory proxy: class hello.proxy.app.v2.OrderServiceV2$$EnhancerBySpringCGLIB$$d565a076, target: class hello.proxy.app.v2.OrderServiceV2

    return proxy;
  }

  @Bean
  public OrderRepositoryV2 orderRepositoryV2(LogTrace logTrace) {
    OrderRepositoryV2 orderRepository = new OrderRepositoryV2();

    ProxyFactory factory = new ProxyFactory(orderRepository);
    factory.addAdvisor(getAdvisor(logTrace));
    OrderRepositoryV2 proxy = (OrderRepositoryV2) factory.getProxy();

    log.info("proxyFactory proxy: {}, target: {}", proxy.getClass(), orderRepository.getClass());
    // proxyFactory proxy: class hello.proxy.app.v2.OrderRepositoryV2$$EnhancerBySpringCGLIB$$81157253, target: class hello.proxy.app.v2.OrderRepositoryV2

    return proxy;
  }

  private Advisor getAdvisor(LogTrace logTrace) {
    // pointcut
    NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
    pointcut.setMappedNames("request*", "order*", "save*");

    // advice
    LogTraceAdvice advice = new LogTraceAdvice(logTrace);
    return new DefaultPointcutAdvisor(pointcut, advice);
  }
}
