package io.hemin.engine.parser.feed.rome;

import com.rometools.rome.feed.CopyFrom;

public class PodloveSimpleChapterItem implements CopyFrom {

    private String start;
    private String title;
    private String href;
    private String image;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public Class<? extends CopyFrom> getInterface() {
        return PodloveSimpleChapterItem.class;
    }

    @Override
    public void copyFrom(CopyFrom obj) {
        PodloveSimpleChapterItem item = (PodloveSimpleChapterItem) obj;
        setStart(item.getStart());
        setTitle(item.getTitle());
        setHref(item.getHref());
        setImage(item.getImage());
    }

    @Override
    public String toString() {
        return "PodloveSimpleChapterItem{" +
            "start='" + start + '\'' +
            ", title='" + title + '\'' +
            ", href='" + href + '\'' +
            ", image='" + image + '\'' +
            '}';
    }
}
