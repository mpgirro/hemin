package io.disposia.engine.mapper;


import com.google.common.collect.Lists;
import org.apache.solr.common.SolrDocument;

import java.util.Collection;
import java.util.Date;

public class SolrFieldMapper {

    public static final SolrFieldMapper INSTANCE = new SolrFieldMapper();

    public String firstStringOrNull(SolrDocument doc, String fieldName) {
        /*
        final Collection<Object> os = doc.getFieldValues(fieldName);
        if (os == null || os.isEmpty()) {
            return null;
        }
        return (String) Lists.newArrayList(os).get(0);
        */
        final Object o = firstOrNull(doc, fieldName);
        return o==null ? null : (String) o;
    }

    public Date firstDateOrNull(SolrDocument doc, String fieldName) {
        final Object o = firstOrNull(doc, fieldName);
        return o==null ? null : (Date) o;
    }

    private Object firstOrNull(SolrDocument doc, String fieldName) {
        final Collection<Object> os = doc.getFieldValues(fieldName);
        if (os == null || os.isEmpty()) {
            return null;
        }
        return Lists.newArrayList(os).get(0);
    }

}
