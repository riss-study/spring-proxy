package hello.proxy.config.v2_dynamicproxy.interceptor;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import java.lang.reflect.Method;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.util.PatternMatchUtils;

public class LogTraceFilterInterceptor implements MethodInterceptor {

  private final Object target;
  private final LogTrace logTrace;
  private final String[] patterns;

  public LogTraceFilterInterceptor(Object target, LogTrace logTrace, String[] patterns) {
    this.target = target;
    this.logTrace = logTrace;
    this.patterns = patterns;
  }

  @Override
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

    String methodName = method.getName();
    if (!PatternMatchUtils.simpleMatch(patterns, methodName)) {
      return methodProxy.invoke(target, args);
    }

    TraceStatus status = null;
    try {
      String message = String.format("%s.%s()",
          method.getDeclaringClass().getSimpleName(),
          methodName);
      status = logTrace.begin(message);

      // 로직 호출
      Object result = methodProxy.invoke(target, args);
      logTrace.end(status);
      return result;
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }
}
