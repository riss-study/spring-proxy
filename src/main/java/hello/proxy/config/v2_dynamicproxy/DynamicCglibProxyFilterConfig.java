package hello.proxy.config.v2_dynamicproxy;

import hello.proxy.app.v2.OrderControllerV2;
import hello.proxy.app.v2.OrderRepositoryV2;
import hello.proxy.app.v2.OrderServiceV2;
import hello.proxy.config.v2_dynamicproxy.interceptor.LogTraceFilterInterceptor;
import hello.proxy.trace.logtrace.LogTrace;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamicCglibProxyFilterConfig {

  private static final String[] PATTERNS = { "request*", "order*", "save*"};

  @Bean
  public OrderControllerV2 orderControllerV2(LogTrace logTrace) {

    OrderServiceV2 orderService = orderServiceV2(logTrace);
    OrderControllerV2 orderController = new OrderControllerV2(orderService);

    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(OrderControllerV2.class);
    enhancer.setCallback(new LogTraceFilterInterceptor(orderController, logTrace, PATTERNS));

    // CGLIB 으로 프록시 생성 시, class 에 기본 생성자 필요. 생성 원하지 않으면, 생성자에 필요한 인자값 넘김 (ServiceV2 Bean 참고)
    OrderControllerV2 proxy = (OrderControllerV2) enhancer.create();

    return proxy;
  }

  @Bean
  public OrderServiceV2 orderServiceV2(LogTrace logTrace) {

    OrderRepositoryV2 orderRepository = orderRepositoryV2(logTrace);
    OrderServiceV2 orderService = new OrderServiceV2(orderRepository);

    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(OrderServiceV2.class);
    enhancer.setCallback(new LogTraceFilterInterceptor(orderService, logTrace, PATTERNS));

    // CGLIB 으로 프록시 생성 시, class 에 기본 생성자 필요. 생성 원하지 않으면, 생성자에 필요한 인자값 넘김
    OrderServiceV2 proxy = (OrderServiceV2) enhancer.create(
        new Class[]{OrderRepositoryV2.class},
        new Object[]{orderRepository}
    );

    return proxy;
  }

  @Bean
  public OrderRepositoryV2 orderRepositoryV2(LogTrace logTrace) {

    OrderRepositoryV2 orderRepository = new OrderRepositoryV2();

    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(OrderRepositoryV2.class);
    enhancer.setCallback(new LogTraceFilterInterceptor(orderRepository, logTrace, PATTERNS));
    OrderRepositoryV2 proxy = (OrderRepositoryV2) enhancer.create();

    return proxy;
  }
}
