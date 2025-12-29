package hello.proxy.postprocessor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class BasicTest {

  @Test
  void basicConfig() {
    ApplicationContext context = new AnnotationConfigApplicationContext(BasicConfig.class);

    // A 빈으로 등록
    A a = context.getBean("beanA", A.class);
    a.hello();

    // B 빈으로 등록 X
    Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(B.class));
  }

  @Slf4j
  @Configuration
  static class BasicConfig {
    @Bean(name = "beanA")
    public A a() {
      return new A();
    }
  }

  @Slf4j
  static class A {
    public void hello() {
      log.info("hello A");
    }
  }

  @Slf4j
  static class B {
    public void hello() {
      log.info("hello B");
    }
  }
}
