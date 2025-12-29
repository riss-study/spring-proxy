package hello.proxy.advisor;

import hello.proxy.common.service.ServiceImpl;
import hello.proxy.common.service.ServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * [해당 테스트 중요 포인트]
 * - AOP 적용 수 만큼 프록시가 생성된다고 착각 x
 * - 스프링은 AOP 적용 시, 최적화를 진행하여 프록시는 1개 만들고, 하나의 프록시에 여러 어드바이저 적용
 * -> 즉, 여러 AOP가 동시에 적용되어도, 스프링의 AOP는 'target'마다 하나의 프록시만 생성!
 */
public class MultiAdvisorTest {

  @Test
  @DisplayName("여러 프록시 (적용해야하는 어드바이저가 N개라면 N개의 프록시를 생성)")
  void multiAdvisorTest1() {
    // client -> proxy2(advisor2) -> proxy1(advisor1) -> target

    // proxy1 생성
    ServiceInterface target = new ServiceImpl();
    ProxyFactory proxyFactory1 = new ProxyFactory(target);
    DefaultPointcutAdvisor advisor1 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice1());
    proxyFactory1.addAdvisor(advisor1);

    ServiceInterface proxy1 = (ServiceInterface) proxyFactory1.getProxy();

    // proxy2 생성, target -> proxy1 입력
    ProxyFactory proxyFactory2 = new ProxyFactory(proxy1);    // advisor1 이 걸려있는 proxy1 을 target 으로 넣어야 함
    DefaultPointcutAdvisor advisor2 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice2());
    proxyFactory2.addAdvisor(advisor2);
    ServiceInterface proxy2 = (ServiceInterface) proxyFactory2.getProxy();

    proxy2.save();

    // [Test worker] INFO hello.proxy.advisor.MultiAdvisorTest$Advice2 - advice 2 호출
    // [Test worker] INFO hello.proxy.advisor.MultiAdvisorTest$Advice1 - advice 1 호출
    // [Test worker] INFO hello.proxy.common.service.ServiceImpl - Call save()
    // [Test worker] INFO hello.proxy.advisor.MultiAdvisorTest$Advice1 - advice 1 끝
    // [Test worker] INFO hello.proxy.advisor.MultiAdvisorTest$Advice2 - advice 2 끝
  }

  @Test
  @DisplayName("하나의 프록시, 여러 어드바이저 (적용해야하는 어드바이저가 N개여도 1개의 프록시 생성, 스프링이 제공)")
  void multiAdvisorTest2() {
    // client -> proxy -> advisor2 -> advisor1 -> target

    // 어드바이저 1, 2 생성
    DefaultPointcutAdvisor advisor1 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice1());
    DefaultPointcutAdvisor advisor2 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice2());

    // proxy 생성
    ServiceInterface target = new ServiceImpl();
    ProxyFactory proxyFactory = new ProxyFactory(target);

    // 기대 호출 순서가 2 -> 1 이므로 addAdvisor 순서 맞춰야 함
    proxyFactory.addAdvisor(advisor2);
    proxyFactory.addAdvisor(advisor1);

    ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();

    proxy.save();

    // [Test worker] INFO hello.proxy.advisor.MultiAdvisorTest$Advice2 - advice 2 호출
    // [Test worker] INFO hello.proxy.advisor.MultiAdvisorTest$Advice1 - advice 1 호출
    // [Test worker] INFO hello.proxy.common.service.ServiceImpl - Call save()
    // [Test worker] INFO hello.proxy.advisor.MultiAdvisorTest$Advice1 - advice 1 끝
    // [Test worker] INFO hello.proxy.advisor.MultiAdvisorTest$Advice2 - advice 2 끝
  }

  @Slf4j
  static class Advice1 implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      log.info("advice 1 호출");
      Object result = invocation.proceed();
      log.info("advice 1 끝");
      return result;
    }
  }

  @Slf4j
  static class Advice2 implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      log.info("advice 2 호출");
      Object result = invocation.proceed();
      log.info("advice 2 끝");
      return result;
    }
  }
}
