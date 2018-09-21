package exo.engine.parse.rss.rome;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author Maximilian Irro
 */
public class PodcastSimpleChapterParser implements ModuleParser {

    private static final Namespace PSC_NS = Namespace.getNamespace(PodloveSimpleChapterModule.URI);

    @Override
    public String getNamespaceUri() {
        return PodloveSimpleChapterModule.URI;
    }

    @Override
    public Module parse(Element element, Locale locale) {
        final Element chaptersElement = element.getChild("chapters", PSC_NS);
        if (chaptersElement != null) {
            final PodloveSimpleChapterModuleImpl mod = new PodloveSimpleChapterModuleImpl();
            final List<Element> chapterElements = chaptersElement.getChildren("chapter", PSC_NS);
            if (!chapterElements.isEmpty()) {
                final List<PodloveSimpleChapterItem> result = new LinkedList<>();
                for (Element eChapter : chapterElements) {
                    final PodloveSimpleChapterItem sc = parseChapter(eChapter);
                    result.add(sc);
                }
                mod.setChapters(result);
                return mod;
            }
        }

        return null;
    }

    private PodloveSimpleChapterItem parseChapter(Element eChapter) {
        final PodloveSimpleChapterItem chapter = new PodloveSimpleChapterItem();

        final String start = getAttributeValue(eChapter, "start");
        if (start != null) {
            chapter.setStart(start);
        }

        final String title = getAttributeValue(eChapter, "title");
        if (title != null) {
            chapter.setTitle(title);
        }

        final String href = getAttributeValue(eChapter, "href");
        if (href != null) {
            chapter.setHref(href);
        }

        return chapter;
    }

    protected String getAttributeValue(Element e, String attributeName) {
        Attribute attr = e.getAttribute(attributeName);
        if (attr == null) {
            attr = e.getAttribute(attributeName, PSC_NS);
        }
        if (attr != null) {
            return attr.getValue();
        } else {
            return null;
        }
    }

}
