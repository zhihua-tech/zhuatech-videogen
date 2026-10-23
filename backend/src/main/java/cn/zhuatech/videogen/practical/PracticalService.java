/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.videogen.practical;

import static cn.zhuatech.videogen.practical.PracticalInput.*;

import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@Service
public class PracticalService {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  public Map<String, Object> execute(Map<String, Object> r) {
    validate(r);
    keys(r, "fps", "scenes");
    int fps = integer(r, "fps", 1, 120);
    var scenes = rows(r, "scenes", 1, 300);
    Set<String> ids = new HashSet<>();
    int previousOverlap = 0;
    long end = 0;
    List<Map<String, Object>> out = new ArrayList<>();
    for (int i = 0; i < scenes.size(); i++) {
      var s = scenes.get(i);
      keys(s, "id", "durationFrames", "transitionOut");
      String id = id(s, "id");
      unique(ids, id);
      int duration = integer(s, "durationFrames", 1, 1000000),
          overlap = integer(s, "transitionOut", 0, 100000);
      if (previousOverlap >= duration || previousOverlap + overlap > duration)
        throw new IllegalArgumentException("转场重叠超过镜头可用帧");
      if (i == scenes.size() - 1 && overlap != 0) throw new IllegalArgumentException("末镜头不能有出场转场");
      if (overlap >= duration) throw new IllegalArgumentException("出场转场须小于镜头时长");
      long start = i == 0 ? 0 : end - previousOverlap;
      end = start + duration;
      out.add(
          Map.of(
              "id",
              id,
              "startFrame",
              start,
              "endFrameExclusive",
              end,
              "durationFrames",
              duration,
              "transitionOut",
              overlap));
      previousOverlap = overlap;
    }
    return Map.of(
        "scenes",
        out,
        "totalFrames",
        end,
        "durationSeconds",
        BigDecimal.valueOf(end).divide(BigDecimal.valueOf(fps), 6, RoundingMode.HALF_UP),
        "renderPerformed",
        false);
  }
}
