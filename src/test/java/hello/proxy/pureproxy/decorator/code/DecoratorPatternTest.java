package hello.proxy.pureproxy.decorator.code;

import hello.proxy.pureproxy.proxy.code.MessageDecorator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class DecoratorPatternTest {

  @Test
  void noDecorator() {
    Component realComponent = new RealComponent();
    DecoratorPatternClient client = new DecoratorPatternClient(realComponent);

    client.execute();
  }

  @Test
  void decorator1() {
    Component realComponent = new RealComponent();
    Component messageComponent = new MessageDecorator(realComponent);
    DecoratorPatternClient client = new DecoratorPatternClient(messageComponent);
    client.execute();
  }

}
