package io.disposia.engine.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.beans.ConstructorProperties;
import java.util.Optional;

/**
 * This is an **toImmutable** DTO, with Jackson serialization capability (see https://blog.pchudzik.com/201704/immutable-dto/)
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class HeadResult {

    private final Integer statusCode;
    private final Optional<String> location;
    private final Optional<String> mimeType;
    private final Optional<String> contentEncoding;
    private final Optional<String> eTag;
    private final Optional<String> lastModified;

    @ConstructorProperties({"statusCode", "location", "mimeType", "contentEncoding", "eTag", "lastModified"})
    public HeadResult(Integer statusCode, Optional<String> location, Optional<String> mimeType, Optional<String> contentEncoding, Optional<String> eTag, Optional<String> lastModified) {
        this.statusCode = statusCode;
        this.location = location;
        this.mimeType = mimeType;
        this.contentEncoding = contentEncoding;
        this.eTag = eTag;
        this.lastModified = lastModified;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Optional<String> getLocation() {
        return location;
    }

    public Optional<String> getMimeType() {
        return mimeType;
    }

    public Optional<String> getContentEncoding() {
        return contentEncoding;
    }

    public Optional<String> geteTag() {
        return eTag;
    }

    public Optional<String> getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return "HeadResult{" +
            "statusCode=" + statusCode +
            ", location=" + location +
            ", mimeType=" + mimeType +
            ", contentEncoding=" + contentEncoding +
            ", eTag=" + eTag +
            ", lastModified=" + lastModified +
            '}';
    }
}
