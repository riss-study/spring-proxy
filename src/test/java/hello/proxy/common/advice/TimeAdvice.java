package hello.proxy.common.advice;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

@Slf4j
public class TimeAdvice implements MethodInterceptor {

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    log.info("execute timeAdvice");
    long startTime = System.currentTimeMillis();

    // target, method, args 등 모든 정보를 이미 MethodInvocation 객체가 갖고 있음
    // target 을 알아서 찾아서, args 인수도 넘겨서 알아서 실행해줌
    Object result = invocation.proceed();
//    Object result = method.invoke(target, args);

    long endTime = System.currentTimeMillis();
    long resultTime = endTime - startTime;
    log.info("timeAdvice end. result time: {} ms", resultTime);

    return result;
  }
}
