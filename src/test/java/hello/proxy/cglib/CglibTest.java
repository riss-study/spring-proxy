package hello.proxy.cglib;

import hello.proxy.cglib.code.TimeMethodInterceptor;
import hello.proxy.common.service.ConcreteService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.cglib.proxy.Enhancer;

@Slf4j
public class CglibTest {

  @Test
  void cglib() {

    // 실제 객체
    ConcreteService target = new ConcreteService();

    // cglib 를 만드는 코드
    Enhancer enhancer = new Enhancer();
    // 인터페이스를 지정하는 게 아니므로, 구체 클래스를 기반으로 즉, ConcreteService 를 상속 받은 Proxy 를 만들어야 하므로, ConcreteService.class 를 넣어줌
    enhancer.setSuperclass(ConcreteService.class);
    // handler(MethodInterceptor 는 Callback 을 상속받음) 지정 - 프록시에 적용할 실행 로직
    enhancer.setCallback(new TimeMethodInterceptor(target));

    // Proxy 의 부모 클래스 즉, 상속 받은 기반이 구체 클래스 ConcreteService 이므로, 캐스팅 가능
    ConcreteService proxy = (ConcreteService) enhancer.create();

    log.info("targetClass: {}", target.getClass());
    // targetClass: class hello.proxy.common.service.ConcreteService
    log.info("proxyClass: {}", proxy.getClass());
    // proxyClass: class hello.proxy.common.service.ConcreteService$$EnhancerByCGLIB$$25d6b0e3

    proxy.call();
//    TimeMethodInterceptor - execute time proxy
//    ConcreteService - ConcreteService Call
//    TimeMethodInterceptor - timeProxy end. result time: 3 ms
  }
}
