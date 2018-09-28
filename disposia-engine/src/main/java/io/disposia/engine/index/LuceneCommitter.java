package io.disposia.engine.index;

import io.disposia.engine.domain.IndexField;
import io.disposia.engine.domain.IndexDoc;
import io.disposia.engine.mapper.IndexMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class LuceneCommitter implements io.disposia.engine.index.IndexCommitter {

    private static final Logger log = LoggerFactory.getLogger(LuceneCommitter.class);

    private final IndexWriter writer;

    private final IndexMapper indexMapper = IndexMapper.INSTANCE;

    public LuceneCommitter(final String indexPath, final boolean create) throws IOException {

        final Directory dir = FSDirectory.open(Paths.get(indexPath));
        final Analyzer analyzer = new StandardAnalyzer();
        final IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        if (create) {
            // Create a new index in the directory, removing any
            // previously indexed documents:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        } else {
            // Add new documents to an existing index:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }

        // Optional: for better indexing performance, if you
        // are indexing many documents, increase the RAM
        // buffer.  But if you do this, increase the max heap
        // size to the JVM (eg add -Xmx512m or -Xmx1g):
        //
        iwc.setRAMBufferSizeMB(256.0);

        this.writer = new IndexWriter(dir, iwc);
        this.writer.commit(); // to create the index if not yet there, and prevent searcher from failing upon creation
    }

    @Override
    public void add(IndexDoc indexDoc) {
        log.debug("Appending document to index : {}", indexDoc);

        final Document luceneDoc = indexMapper.toLucene(indexDoc);
        try {
            this.writer.addDocument(luceneDoc);
        } catch (IOException e) {
            log.error("Error adding index entry for : {} ; reason was : {}, {}", indexDoc, e.getMessage(), e);
        }
    }

    @Override
    public void update(IndexDoc indexDoc) {
        log.debug("Updating document in index : {}", indexDoc);

        final Document luceneDoc = indexMapper.toLucene(indexDoc);
        try {
            writer.updateDocument(new Term(IndexField.EXO, indexDoc.getExo()), luceneDoc);
        } catch (IOException e) {
            log.error("Error updating index entry for : {} ; reason was : {}, {}", indexDoc, e.getMessage(), e);
        }
    }

    @Override
    public void commit() {
        log.debug("Committing index");
        try {
            this.writer.commit();
        } catch (IOException e) {
            log.error("Error committing index; reason was : {}, {}", e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
        try {
            this.writer.close();
        } catch (IOException e) {
            log.error("Exception on destroying IndexWriter; reason was : {}, {}", e.getMessage(), e);
        }
    }

    public IndexWriter getIndexWriter(){
        return this.writer;
    }

}
