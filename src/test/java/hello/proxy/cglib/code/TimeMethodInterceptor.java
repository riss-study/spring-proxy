package hello.proxy.cglib.code;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

@Slf4j
public class TimeMethodInterceptor implements MethodInterceptor {

  private final Object target;

  public TimeMethodInterceptor(Object target) {
    this.target = target;
  }

  @Override
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
    log.info("execute time proxy");
    long startTime = System.currentTimeMillis();

    // CGLIB 메뉴얼은 methodProxy 라는 것을 쓰면 성능상 권장함 (내부 최적화 하는 느낌)
    Object result = methodProxy.invoke(target, args);
//    Object result = method.invoke(target, args);

    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("timeProxy end. result time: {} ms", resultTime);

    return result;
  }
}
