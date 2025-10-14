package hello.proxy.pureproxy.concreteproxy;

import hello.proxy.pureproxy.concreteproxy.code.ConcreteClient;
import hello.proxy.pureproxy.concreteproxy.code.ConcreteLogic;
import hello.proxy.pureproxy.concreteproxy.code.TimeProxy;
import org.junit.jupiter.api.Test;

public class ConcreteProxyTest {

  @Test
  void noProxy() {
    ConcreteLogic logic = new ConcreteLogic();
    ConcreteClient client = new ConcreteClient(logic);
    client.execute();
  }

  @Test
  void addProxy() {
    TimeProxy timeProxy = new TimeProxy(new ConcreteLogic());
    ConcreteClient client = new ConcreteClient(timeProxy);
    client.execute();
  }
}
