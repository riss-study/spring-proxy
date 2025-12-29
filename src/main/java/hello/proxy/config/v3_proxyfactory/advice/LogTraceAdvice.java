package hello.proxy.config.v3_proxyfactory.advice;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class LogTraceAdvice implements MethodInterceptor {

  // 생성자로 target 을 받을 필요가 없어짐
  private final LogTrace logTrace;

  public LogTraceAdvice(LogTrace logTrace) {
    this.logTrace = logTrace;
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    TraceStatus status = null;
    try {
      Method method = invocation.getMethod();
      String message = String.format("%s.%s()",
          method.getDeclaringClass().getSimpleName(),
          method.getName());
      status = logTrace.begin(message);

      // 로직 호출
      Object result = invocation.proceed();

      logTrace.end(status);
      return result;
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }
}
