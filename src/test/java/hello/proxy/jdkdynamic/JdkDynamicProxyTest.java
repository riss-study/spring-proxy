package hello.proxy.jdkdynamic;

import hello.proxy.jdkdynamic.code.AImpl;
import hello.proxy.jdkdynamic.code.AInterface;
import hello.proxy.jdkdynamic.code.BImpl;
import hello.proxy.jdkdynamic.code.BInterface;
import hello.proxy.jdkdynamic.code.TimeInvocationHandler;
import java.lang.reflect.Proxy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class JdkDynamicProxyTest {

  @Test
  void dynamicA() {
    AInterface target = new AImpl();
    TimeInvocationHandler handler = new TimeInvocationHandler(target);

    AInterface proxy = (AInterface) Proxy.newProxyInstance(AInterface.class.getClassLoader(), new Class[]{AInterface.class}, handler);
    // 어느 클래스 로더에 할지, 어떤 인터페이스 기반으로 프록시를 만들지, 프록시에 사용될 로직(handler)이 뭔지

    proxy.call();   // TimeInvocationHandler.invoke() 호출 역할. 이때 method, args 를 넘겨 주는 역할
    // TImeInvocationHandler 의 invoke 의 파라미터 method 에 call() 이 넘어감

    log.info("targetClass: {}", target.getClass()); // class hello.proxy.jdkdynamic.code.AImpl
    log.info("proxyClass: {}", proxy.getClass()); // class com.sun.proxy.$Proxy12
  }

  @Test
  void dynamicB() {
    BInterface target = new BImpl();
    TimeInvocationHandler handler = new TimeInvocationHandler(target);

    BInterface proxy = (BInterface) Proxy.newProxyInstance(BInterface.class.getClassLoader(), new Class[]{BInterface.class}, handler);
    // 어느 클래스 로더에 할지, 어떤 인터페이스 기반으로 프록시를 만들지, 프록시에 사용될 로직(handler)이 뭔지

    proxy.call();
    // TImeInvocationHandler 의 invoke 의 파라미터 method 에 call() 이 넘어감

    log.info("targetClass: {}", target.getClass()); // class hello.proxy.jdkdynamic.code.BImpl
    log.info("proxyClass: {}", proxy.getClass()); // class com.sun.proxy.$Proxy12
  }
}
