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
    ObjectA a = context.getBean("beanA", ObjectA.class);
    a.hello();

    // B 빈으로 등록 X
    Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(ObjectB.class));
  }

  @Slf4j
  @Configuration
  static class BasicConfig {
    @Bean(name = "beanA")
    public ObjectA a() {
      return new ObjectA();
    }
  }

  @Slf4j
  static class ObjectA {
    public void hello() {
      log.info("hello A");
    }
  }

  @Slf4j
  static class ObjectB {
    public void hello() {
      log.info("hello B");
    }
  }
}
