package com.cervantesvirtual.index;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cervantesvirtual.analyzer.SynonymIndexAnalyzer;
import com.cervantesvirtual.analyzer.TextAnalyzer;

/**
 * A very simple demo used in the API documentation (src/java/overview.html).
 *
 * Please try to keep src/java/overview.html up-to-date when making changes to
 * this class.
 */
public class TestDemo {

	static IndexWriter writer;
	static RAMDirectory dir;
	static String field = "contents";
	@BeforeClass
	public static void setup() throws Exception {
		dir = new RAMDirectory();
		Analyzer analyzer = new SynonymIndexAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		writer = new IndexWriter(dir, config);

		String docsPath = "src/main/resources/txtfiles";
		final Path docDir = Paths.get(docsPath);
		IndexFiles.indexDocs(writer, docDir);

		writer.forceMerge(1);
		writer.commit();
		writer.close();
	}

	@Test
	public void testJavaNotDotNet() throws Exception {
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);

		BooleanQuery booleanQuery = new BooleanQuery.Builder()
				.add(new TermQuery(new Term(field, "java")), Occur.MUST)
				.add(new TermQuery(new Term(field, "net")), Occur.MUST)
				.add(new TermQuery(new Term(field, "dot")), Occur.MUST).build();

		System.out.println("booleanQuery:" + booleanQuery);

		searcher.search(booleanQuery, 10);

		Term term = new Term("contents", booleanQuery.toString());
		SearchFiles.getBasicStats(reader, term, 0);

		reader.close();
	}
	
	@Test
	public void testWhitespace() throws Exception {
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);

		QueryParser parser = new QueryParser(field, new WhitespaceAnalyzer());
		
		Query query = parser.parse("pos#noun");
        System.out.println("WHITESPACE pos#noun Searching for: " + query.toString(field));

        TopDocs hits = searcher.search(query, 10);
        
        for (int i = 0; i < hits.scoreDocs.length; i++)
        {
            int docid = hits.scoreDocs[i].doc;
            Document doc = searcher.doc(docid);
            String title = doc.get("path");
             
            //Printing - to which document result belongs
            System.out.println("Path " + " : " + title);
             
            //Get stored text from found document
            //String text = doc.get("contents");
        }

        // calculo de estadisticas para pos#
		Term term = new Term("contents", "pos#noun");
		SearchFiles.getBasicStats(reader, term, 0);
		
		term = new Term("contents", "pos#adjective");
		SearchFiles.getBasicStats(reader, term, 0);
		
		term = new Term("contents", "lemma#ver");
		SearchFiles.getBasicStats(reader, term, 0);

		reader.close();
	}
	
	@Test
	public void testText() throws Exception {
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Analyzer analyzer = new TextAnalyzer();

		QueryParser parser = new QueryParser(field, analyzer);
		
		Query query = parser.parse("callada");
        System.out.println("CALLADA --- Searching for: " + query.toString(field));

        TopDocs hits = searcher.search(query, 10);
        
        SearchFiles.highlightResults(reader, searcher, query, hits, analyzer);

		reader.close();
	}
	
	@Test
	public void testAno() throws Exception {
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Analyzer analyzer = new TextAnalyzer();

		QueryParser parser = new QueryParser(field, analyzer);
		
		Query query = parser.parse("antaño");
        System.out.println("ANTAÑO --- Searching for: " + query.toString(field));

        TopDocs hits = searcher.search(query, 10);
        
        SearchFiles.highlightResults(reader, searcher, query, hits, analyzer);
        
        query = parser.parse("antano");
        System.out.println("ANTANO --- Searching for: " + query.toString(field));

        hits = searcher.search(query, 10);
        
        SearchFiles.highlightResults(reader, searcher, query, hits, analyzer);

		reader.close();
	}
	
	@Test
	public void testDiacritic() throws Exception {
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Analyzer analyzer = new TextAnalyzer();

		QueryParser parser = new QueryParser(field, analyzer);
		
		Query query = parser.parse("ángel");
        System.out.println("ÁNGEL --- Searching for: " + query.toString(field));

        TopDocs hits = searcher.search(query, 10);
        
        SearchFiles.highlightResults(reader, searcher, query, hits, analyzer);

        query = parser.parse("Angel");
        System.out.println("aNGEL --- Searching for: " + query.toString(field));

        hits = searcher.search(query, 10);
        
        SearchFiles.highlightResults(reader, searcher, query, hits, analyzer);

		reader.close();
	}
	
	@Test
	public void testCargaban() throws Exception {
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Analyzer analyzer = new TextAnalyzer();

		QueryParser parser = new QueryParser(field, analyzer);
		
		Query query = parser.parse("cargaban");
        System.out.println("ANO --- Searching for: " + query.toString(field));

        TopDocs hits = searcher.search(query, 10);
        
        SearchFiles.highlightResults(reader, searcher, query, hits, analyzer);

		Term term = new Term("contents", "cargaban");
		SearchFiles.getBasicStats(reader, term, 0);

		reader.close();
	}

	
	@Test
	public void test_pos_noun() throws Exception {
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Analyzer analyzer = new WhitespaceAnalyzer();

		QueryParser parser = new QueryParser(field, analyzer);
		
		Query query = parser.parse("pos#noun");
        System.out.println("POS#NOUN --- Searching for: " + query.toString(field));

        TopDocs hits = searcher.search(query, 10);
        
        System.out.println("###pos#noun hits:" + hits.totalHits);
		Term term = new Term("contents", "pos#noun");
		SearchFiles.getBasicStats(reader, term, 0);

		reader.close();
	}
	
	@Test
	public void test_pos_adjective() throws Exception {
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Analyzer analyzer = new WhitespaceAnalyzer();

		QueryParser parser = new QueryParser(field, analyzer);
		
		Query query = parser.parse("pos#adjective");
        System.out.println("POS#adjective --- Searching for: " + query.toString(field));

        TopDocs hits = searcher.search(query, 10);
        
        System.out.println("###pos#adjective hits:" + hits.totalHits);
		Term term = new Term("contents", "pos#adjective");
		SearchFiles.getBasicStats(reader, term, 0);

		reader.close();
	}
	
	
	@Test
	public void test_pos_verb() throws Exception {
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Analyzer analyzer = new WhitespaceAnalyzer();

		QueryParser parser = new QueryParser(field, analyzer);
		
		Query query = parser.parse("pos#verb");
        System.out.println("POS#verb --- Searching for: " + query.toString(field));

        TopDocs hits = searcher.search(query, 10);
        
        System.out.println("###pos#verb hits:" + hits.totalHits);
		Term term = new Term("contents", "pos#verb");
		SearchFiles.getBasicStats(reader, term, 0);

		reader.close();
	}
}
