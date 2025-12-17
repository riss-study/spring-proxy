package hello.proxy.config.v2_dynamicproxy.handler;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.util.PatternMatchUtils;

public class LogTraceFilterHandler implements InvocationHandler {

  private final Object target;
  private final LogTrace logTrace;
  private final String[] patterns;

  public LogTraceFilterHandler(Object target, LogTrace logTrace, String[] patterns) {
    this.target = target;
    this.logTrace = logTrace;
    this.patterns = patterns;

  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    // method name filter
    String methodName = method.getName();

    // save, request, reque*, *est not match
    if (!PatternMatchUtils.simpleMatch(patterns, methodName)) {
      // logTrace 호출 x, 실제 메서드만 호출(invoke)
      return method.invoke(target, args);
    }

    TraceStatus status = null;
    try {
      String message = String.format("%s.%s()",
          method.getDeclaringClass().getSimpleName(),
          methodName);
      status = logTrace.begin(message);

      // 로직 호출
      Object result = method.invoke(target, args);
      logTrace.end(status);
      return result;
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }
}
