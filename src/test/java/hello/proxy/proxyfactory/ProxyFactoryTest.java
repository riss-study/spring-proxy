package hello.proxy.proxyfactory;

import static org.assertj.core.api.Assertions.assertThat;

import hello.proxy.common.advice.TimeAdvice;
import hello.proxy.common.service.ConcreteService;
import hello.proxy.common.service.ServiceImpl;
import hello.proxy.common.service.ServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;

@Slf4j
public class ProxyFactoryTest {

  @Test
  @DisplayName("인터페이스가 있으면 JDK 동적 프록시 사용")
  void interfaceProxy() {

    ServiceInterface target = new ServiceImpl();

    // JDK 동적 프록시가 제공하는 InvocationHandler 와 CGLIB 이 제공하는 MethodInterceptor 를 추상화한 것
    // 그래서 구체 클래스, 인터페이스 기반임에 따라 자동으로 JDK 동적 프록시이나 CGLIB 프록시로 자동 생성
    // ProxyFactory 를 사용하면, Advice 를 호출하는 전용 InvocationHandler, MethodInterceptor 를 내부에서 사용
    // 프록시팩토리가 프록시 생성 시, JDK 동적 프록시 이면 adviceInvocationHandler 를, Cglib 프록시면 adviceMethodInterceptor 를 붙여놈
    // ProxyFactory 를 사용하여 만든 프록시는 내부에서 adviceInvocationHandler(in JdkDynamicAopProxy), adviceMethodInterceptor(in CglibAopProxy) 가 모두 Advice 를 호출하게 돼있음 (프록시팩토리에서 그렇게 세팅해놓음)
    // 그러므로 개발자는 Advice 만 구현하면 됨
    ProxyFactory proxyFactory = new ProxyFactory(target);
    proxyFactory.addAdvice(new TimeAdvice());
    ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();

    log.info("targetClass: {}", target.getClass());
    log.info("proxyClass: {}", proxy.getClass());
    // targetClass: class hello.proxy.common.service.ServiceImpl
    // proxyClass: class com.sun.proxy.$Proxy13

    // save() 메서드 실행
    proxy.save();

    // AopUtils -> 내가 직접 프록시를 만들었을 땐 안되고, ProxyFactory 를 이용하여 생성 시 proxy 가 맞는 지 체크 가능
    assertThat(AopUtils.isAopProxy(proxy)).isTrue();
    assertThat(AopUtils.isJdkDynamicProxy(proxy)).isTrue();
    assertThat(AopUtils.isCglibProxy(proxy)).isFalse();       // 인터페이스 기반이므로 CGLIB 이 아닌 JDK 동적 프록시를 생성
  }

  @Test
  @DisplayName("구체 클래스만 있으면 CGLIB 사용")
  void concreteProxy() {

    ConcreteService target = new ConcreteService();

    ProxyFactory proxyFactory = new ProxyFactory(target);
    proxyFactory.addAdvice(new TimeAdvice());
    ConcreteService proxy = (ConcreteService) proxyFactory.getProxy();

    log.info("targetClass: {}", target.getClass());
    log.info("proxyClass: {}", proxy.getClass());
    // targetClass: class hello.proxy.common.service.ConcreteService
    // proxyClass: class hello.proxy.common.service.ConcreteService$$EnhancerBySpringCGLIB$$7379361f

    // call() 메서드 실행
    proxy.call();

    assertThat(AopUtils.isAopProxy(proxy)).isTrue();
    assertThat(AopUtils.isJdkDynamicProxy(proxy)).isFalse();
    assertThat(AopUtils.isCglibProxy(proxy)).isTrue();       // 구체 클래스 기반이므로 JDK 동적 프록시가 아닌 CGLIB 프록시를 생성
  }

  @Test
  @DisplayName("ProxyTargetClass 옵션을 사용하면 인터페이스가 있어도 CGLIB 를 사용하고, 클래스 기반 프록시 사용")
  void proxyTargetClass() {

    ServiceInterface target = new ServiceImpl();

    ProxyFactory proxyFactory = new ProxyFactory(target);
    // 이 true 세팅으로 인해, 인터페이스 여부에 상관없이 항상 구체 클래스 기반의 CGLIB 프록시를 생성
    proxyFactory.setProxyTargetClass(true);
    proxyFactory.addAdvice(new TimeAdvice());
    ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();

    log.info("targetClass: {}", target.getClass());
    log.info("proxyClass: {}", proxy.getClass());
    // targetClass: class hello.proxy.common.service.ServiceImpl
    // proxyClass: class hello.proxy.common.service.ServiceImpl$$EnhancerBySpringCGLIB$$f02e8c1

    // save() 메서드 실행
    proxy.save();

    assertThat(AopUtils.isAopProxy(proxy)).isTrue();
    assertThat(AopUtils.isJdkDynamicProxy(proxy)).isFalse();
    assertThat(AopUtils.isCglibProxy(proxy)).isTrue();       // 인터페이스이지만 proxyTargetClass 세팅으로 인해, JDK 동적 프록시가 아닌 구체 클래스 기반의 CGLIB 프록시 생성
  }

  /**
   * 참고!
   * 스프링부트(2.0 이상)는 AOP를 적용할 때 기본적으로 'proxyTargetClass=true'"'로 설정해서 사용
   * 따라서, 인터페이스가 있어도 항상 CGLIB 을 사용해서 구체 클래스 기반 프록시를 생성
   */
}
