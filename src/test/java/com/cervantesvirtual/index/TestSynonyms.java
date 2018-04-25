package com.cervantesvirtual.index;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermAutomatonQuery;
import org.apache.lucene.search.TokenStreamToTermAutomatonQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.RAMDirectory;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cervantesvirtual.analyzer.SynonymIndexAnalyzer;
import com.cervantesvirtual.analyzer.SynonymSearchAnalyzer;

/**
 * A very simple demo used in the API documentation (src/java/overview.html).
 *
 * Please try to keep src/java/overview.html up-to-date when making changes to
 * this class.
 */
public class TestSynonyms {

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
	public void testAutomata() throws Exception {
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);

		SynonymSearchAnalyzer analyzer = new SynonymSearchAnalyzer();

		TokenStreamToTermAutomatonQuery q = new TokenStreamToTermAutomatonQuery();
		TermAutomatonQuery autQuery = q.toQuery("contents",
				analyzer.tokenStream("contents", "pos#noun pos#adjective"));

		System.out.println("aut:" + autQuery);

		TopDocs hits = searcher.search(autQuery, 10);

		SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
		Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(autQuery));
		for (int i = 0; i < hits.totalHits; i++) {
			int id = hits.scoreDocs[i].doc;
			Document doc = searcher.doc(id);

			// Term vector
			String text = doc.get("contents");
			TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), hits.scoreDocs[i].doc,
					"contents", new SynonymSearchAnalyzer());
			TextFragment[] frag = highlighter.getBestTextFragments(tokenStream, text, false, 10);
			for (int j = 0; j < frag.length; j++) {
				if ((frag[j] != null) && (frag[j].getScore() > 0)) {
					System.out.println((frag[j].toString()));
				}
			}
			System.out.println("-------------");
		}

		reader.close();
	}

	@Test
	public void testCallada() throws Exception {
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);

		SynonymSearchAnalyzer analyzer = new SynonymSearchAnalyzer();

		TokenStreamToTermAutomatonQuery q = new TokenStreamToTermAutomatonQuery();
		TermAutomatonQuery autQuery = q.toQuery("contents", analyzer.tokenStream("contents", "callada viajeros"));

		System.out.println("aut:" + autQuery);

		TopDocs hits = searcher.search(autQuery, 10);

		SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
		Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(autQuery));
		for (int i = 0; i < hits.totalHits; i++) {
			int id = hits.scoreDocs[i].doc;
			Document doc = searcher.doc(id);

			// Term vector
			String text = doc.get("contents");
			TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), hits.scoreDocs[i].doc,
					"contents", new SynonymSearchAnalyzer());
			TextFragment[] frag = highlighter.getBestTextFragments(tokenStream, text, false, 10);
			for (int j = 0; j < frag.length; j++) {
				if ((frag[j] != null) && (frag[j].getScore() > 0)) {
					System.out.println((frag[j].toString()));
				}
			}
			System.out.println("-------------");
		}

		reader.close();
	}

}
