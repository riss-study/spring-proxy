package hello.proxy.config.v4_postprocessor.postprocessor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

@Slf4j
public class PackageLogTracePostProcessor implements BeanPostProcessor {

  private final String basePackage;
  private final Advisor advisor;

  public PackageLogTracePostProcessor(String basePackage, Advisor advisor) {
    this.basePackage = basePackage;
    this.advisor = advisor;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    log.info("param beanName: {}, bean: {}", beanName, bean.getClass());

    // proxy 적용 대상 여부 체크
    // 프록시 적용 대상이 아니면 원본 그대로 진행
    String packageName = bean.getClass().getPackageName();
    if (!packageName.startsWith(basePackage)) {
      return bean;
    }

    // 프록시 적용 대상이면 프록시를 만들어서 반환
    ProxyFactory factory = new ProxyFactory(bean);
    factory.addAdvisor(advisor);
    Object proxy = factory.getProxy();
    log.info("created proxy - target: {}, proxy: {}", bean.getClass(), proxy.getClass());

    // 스프링부트가 기본으로 등록하는 수많은 빈들이 빈 후처리기를 통과하여 'param beanName: ~' 로그가 실제로 엄청 찍힘
    // 스프링부트가 기본 제공하는 빈 중 프록시 객체 만들 수 없는 빈들도 있음. 모든 객체를 프록시로 만들 경우 오류 발생함
    // 그러므로 실제로 꼭 필요한 클래스만 프록시로 등록해야 함 (여기서는 특정 패키지를 기준으로 해당 패키지, 하위 패키지의 빈들을 프록시 빈으로 생성)
    // 나중에 포인트컷 사용하면 훨씬 더 효과적으로 가능
    // v1, v2 같이 수동을 등록한 빈 + v3 같은 컴포넌트 스캔을 통해 등록한 빈들도 프록시 적용이 가능

    // param beanName: orderRepositoryV3, bean: class hello.proxy.app.v3.OrderRepositoryV3
    // created proxy - target: class hello.proxy.app.v3.OrderRepositoryV3, proxy: class hello.proxy.app.v3.OrderRepositoryV3$$EnhancerBySpringCGLIB$$49293ac8
    // param beanName: orderServiceV3, bean: class hello.proxy.app.v3.OrderServiceV3
    // created proxy - target: class hello.proxy.app.v3.OrderServiceV3, proxy: class hello.proxy.app.v3.OrderServiceV3$$EnhancerBySpringCGLIB$$a3f76005
    // param beanName: orderControllerV3, bean: class hello.proxy.app.v3.OrderControllerV3
    // created proxy - target: class hello.proxy.app.v3.OrderControllerV3, proxy: class hello.proxy.app.v3.OrderControllerV3$$EnhancerBySpringCGLIB$$4b30617e
    // param beanName: hello.proxy.config.AppV1Config, bean: class hello.proxy.config.AppV1Config$$EnhancerBySpringCGLIB$$5194c947
    // param beanName: orderRepositoryV1, bean: class hello.proxy.app.v1.OrderRepositoryV1Impl
    // created proxy - target: class hello.proxy.app.v1.OrderRepositoryV1Impl, proxy: class com.sun.proxy.$Proxy50
    // param beanName: orderServiceV1, bean: class hello.proxy.app.v1.OrderServiceV1Impl
    // created proxy - target: class hello.proxy.app.v1.OrderServiceV1Impl, proxy: class com.sun.proxy.$Proxy51
    // param beanName: orderControllerV1, bean: class hello.proxy.app.v1.OrderControllerV1Impl
    // created proxy - target: class hello.proxy.app.v1.OrderControllerV1Impl, proxy: class com.sun.proxy.$Proxy52
    // param beanName: hello.proxy.config.AppV2Config, bean: class hello.proxy.config.AppV2Config$$EnhancerBySpringCGLIB$$a1decfa8
    // param beanName: orderRepositoryV2, bean: class hello.proxy.app.v2.OrderRepositoryV2
    // created proxy - target: class hello.proxy.app.v2.OrderRepositoryV2, proxy: class hello.proxy.app.v2.OrderRepositoryV2$$EnhancerBySpringCGLIB$$3b47ec42
    // param beanName: orderServiceV2, bean: class hello.proxy.app.v2.OrderServiceV2
    // created proxy - target: class hello.proxy.app.v2.OrderServiceV2, proxy: class hello.proxy.app.v2.OrderServiceV2$$EnhancerBySpringCGLIB$$8f981a65
    // param beanName: orderControllerV2, bean: class hello.proxy.app.v2.OrderControllerV2
    // created proxy - target: class hello.proxy.app.v2.OrderControllerV2, proxy: class hello.proxy.app.v2.OrderControllerV2$$EnhancerBySpringCGLIB$$3d4f12f8
    return proxy;
  }
}
