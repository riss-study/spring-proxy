package hello.proxy.config.v6_aop.aspect;

import hello.proxy.config.v5_autoproxy.AutoProxyConfig;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * AnnotationAwareAutoProxyCreator 의 또 하나의 추가 기능
 * @Aspect 를 찾아서 이것을 Advisor 로 변환(만들어줌)
 * 그래서 이름 앞에 AnnotationAware 가 붙음 (애노테이션을 인식하는)
 *
 * 정리
 * 자동 프록시 생성기의 2가지 일
 * 1. 스프링 빈에 등록된 빈 중 @Aspect 를 보고 Advisor 로 변환해서 저장
 * 2. 어드바이저 기반으로 프록시를 생성
 *
 * @Aspect 를 어드바이저로 변환 -> 저장 과정
 * 1. 실행: 스프링 애플리케이션 로딩 시점에 자동 프록시 생성기를 호출
 * 2. 모든 @Aspect Bean 조회
 *    AutoProxyCreator 가 spring container 에서 @Aspect 애노테이션이 붙은 스프링 빈을 모두 조회
 * 3. 어드바이저 생성
 *    @Aspect 어드바이저 빌더를 통해 @Apsecct 애노테이션 정보를 기반으로 어드바이저 생성
 *    (Around 내용을 pointcut, 메서드 로직을 advice 로 만들어서 이를 감싼 어드바이저 생성)
 * 4. @Aspect 기반 어드바이저 저장: 생성한 어드바이저를 @Aspect 어드바이저 빌더 내부에 저장
 *
 * @Aspect 어드바이저 빌더
 *    BeanFactoryAspectJAdvisorsBuilder 클래스
 *    @Aspect 정보를 기반으로 포인트컷, 어드바이스, 어드바이저를 생성하고 보관하는 것을 담당
 *    @Aspect 정보를 기반으로 어드바이저를 만들고, @Aspect 어드바이저 빌더 내부 저장소에 캐싱.
 *    캐시에 어드바이저가 이미 만들어져 있는 경우, 캐싱된 어드바이저를 반환
 *
 * [자동 프록시 생성기 작동 과정]
 * 1. 생성: 스프링 빈 대상이 되는 객체 생성(@Bean, component scan 모두 포함)
 * 2. 전달: 생성된 객체를 빈 저장소에 등록하기 직전에 빈 후처리기에 전달
 * 3-1. Advisor 빈 조회: 스프링 컨테이너에서 Advisor 빈을 모두 조회
 * 3-2. @Aspect Advisor 조회: @Aspect 어드바이저 빌더 내부에 저장된 Advisor 를 모두 조회
 * 4. 프록시 적용 대상 체크: AutoProxyConfig.java 상단 pointcut 기능 주석 + 위에 적힌 @Aspect 저장 과정에 의해 조건에 만족하면 프록시 적용 대상
 * @see AutoProxyConfig
 * 5. 프록시 생성: 프록시 적용 대상이면 프록시 생성 후 이를 반환. 이 프록시를 스프링 빈으로 등록. 적용 대상이 아니라면 원본 객체를 반환해서 이를 스프링 빈으로 등록
 * 6. 빈 등록: 반환된 객체는 스프링 빈으로 등록
 */
@Slf4j
@Aspect
public class LogTraceAspect {

  private final LogTrace logTrace;

  public LogTraceAspect(LogTrace logTrace) {
    this.logTrace = logTrace;
  }

  // Around annotation 이 pointcut
  // execute 안의 로직이 advice (부가 로직)
  @Around("execution(* hello.proxy.app..*(..)) && !execution(* hello.proxy.app..noLog(..))")
  public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {

    TraceStatus status = null;
    try {
      String message = joinPoint.getSignature().toShortString();
      status = logTrace.begin(message);

      // 로직 호출
      Object result = joinPoint.proceed();

      logTrace.end(status);
      return result;
    } catch (Exception e) {
      logTrace.exception(status, e);
      throw e;
    }
  }
}
