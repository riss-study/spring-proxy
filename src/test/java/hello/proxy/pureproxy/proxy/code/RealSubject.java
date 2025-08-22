package hello.proxy.pureproxy.proxy.code;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RealSubject implements Subject {
  @Override
  public String operation() {
    log.info("실제 객체 호출");
    sleep(1000);    // 호출할 때 큰 부하주는 데이터라고 가정
    return "riss data";
  }

  private void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
