package exo.engine.parse.rss.rome;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.Module;

import java.util.List;

/**
 * @author Maximilian Irro
 */
public interface PodloveSimpleChapterModule extends Module, CopyFrom {

    String URI = "http://podlove.org/simple-chapters";

    List<PodloveSimpleChapterItem> getChapters();
    void setChapters(List<PodloveSimpleChapterItem> chapters);
}
