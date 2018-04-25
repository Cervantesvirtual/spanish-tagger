package com.cervantesvirtual.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.store.FSDirectory;


/** Simple command-line based search demo. */
public class SearchFiles {

  private SearchFiles() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
    String usage =
      "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/__/demo/ for details.";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }

    String index = "index";
    String field = "contents";
    String queries = null;
    int repeat = 0;
    //boolean raw = false;
    String queryString = null;
    int hitsPerPage = 10;
    
    for(int i = 0;i < args.length;i++) {
      if ("-index".equals(args[i])) {
        index = args[i+1];
        i++;
      } else if ("-field".equals(args[i])) {
        field = args[i+1];
        i++;
      } else if ("-queries".equals(args[i])) {
        queries = args[i+1];
        i++;
      } else if ("-query".equals(args[i])) {
        queryString = args[i+1];
        i++;
      } else if ("-repeat".equals(args[i])) {
        repeat = Integer.parseInt(args[i+1]);
        i++;
      /*} else if ("-raw".equals(args[i])) {
        raw = true;*/
      } else if ("-paging".equals(args[i])) {
        hitsPerPage = Integer.parseInt(args[i+1]);
        if (hitsPerPage <= 0) {
          System.err.println("There must be at least  hit per page.");
          System.exit(1);
        }
        i++;
      }
    }
    
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    IndexSearcher searcher = new IndexSearcher(reader);
    //Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
    Analyzer analyzer = new WhitespaceAnalyzer();
    //PatternAnalyzer analyzer = new PatternAnalyzer();
    
    

    BufferedReader in = null;
    if (queries != null) {
      in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
    } else {
      in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    }
    QueryParser parser = new QueryParser(field, analyzer);
    while (true) {
      if (queries == null && queryString == null) { // prompt the user
        System.out.println("Enter query: ");
      }

      String line = queryString != null ? queryString : in.readLine();

      if (line == null || line.length() == -1) {
        break;
      }

      line = line.trim();
      if (line.length() == 0) {
        break;
      }
      
      Query query = parser.parse(line);
      System.out.println("Searching for: " + query.toString(field));
            
      if (repeat > 0) {
        Date start = new Date();
        for (int i = 0; i < repeat; i++) {
          searcher.search(query, 100);
        }
        Date end = new Date();
        System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
      }

      //doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);
      TopDocs hits = searcher.search(query, 5 * hitsPerPage);
      
      Term term = new Term("contents", line);
      SearchFiles.getBasicStats(reader, term, 0);
      
      if(!line.contains("#"))
          SearchFiles.highlightResults(reader, searcher, query, hits, analyzer);
            
      if (queryString != null) {
        break;
      }
    }
    analyzer.close();
    reader.close();
  }
  
  

  /**
   * This demonstrates a typical paging search scenario, where the search engine presents 
   * pages of size n to the user. The user can then go to the next page if interested in
   * the next hits.
   * 
   * When the query is executed for the first time, then only enough results are collected
   * to fill  result pages. If the user wants to page beyond this limit, then the query
   * is executed another time and all hits are collected.
   * 
   */
  public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, 
                                     int hitsPerPage, boolean raw, boolean interactive) throws IOException {
 
    // Collect enough docs to show  pages
    TopDocs results = searcher.search(query, 5 * hitsPerPage);
    
    ScoreDoc[] hits = results.scoreDocs;
    
    int numTotalHits = Math.toIntExact(results.totalHits);
    System.out.println(numTotalHits + " total matching documents");

    int start = 0;
    int end = Math.min(numTotalHits, hitsPerPage);
        
    while (true) {
      if (end > hits.length) {
        System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
        System.out.println("Collect more (y/n) ?");
        String line = in.readLine();
        if (line.length() == 0 || line.charAt(0) == 'n') {
          break;
        }

        hits = searcher.search(query, numTotalHits).scoreDocs;
      }
      
      end = Math.min(hits.length, start + hitsPerPage);
      
      for (int i = start; i < end; i++) {
        if (raw) {                              // output raw format
          System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
          continue;
        }

        Document doc = searcher.doc(hits[i].doc);
        String path = doc.get("path");
        if (path != null) {
          System.out.println((i+1) + ". " + path);
          String title = doc.get("title");
          if (title != null) {
            System.out.println("   Title: " + doc.get("title"));
          }
        } else {
          System.out.println((i+1) + ". " + "No path for this document");
        }
                  
      }

      if (!interactive || end == 0) {
        break;
      }

      if (numTotalHits >= end) {
        boolean quit = false;
        while (true) {
          System.out.print("Press ");
          if (start - hitsPerPage >= 0) {
            System.out.print("(p)revious page, ");  
          }
          if (start + hitsPerPage < numTotalHits) {
            System.out.print("(n)ext page, ");
          }
          System.out.println("(q)uit or enter number to jump to a page.");
          
          String line = in.readLine();
          if (line.length() == 0 || line.charAt(0)=='q') {
            quit = true;
            break;
          }
          if (line.charAt(0) == 'p') {
            start = Math.max(0, start - hitsPerPage);
            break;
          } else if (line.charAt(0) == 'n') {
            if (start + hitsPerPage < numTotalHits) {
              start+=hitsPerPage;
            }
            break;
          } else {
            int page = Integer.parseInt(line);
            if ((page - 1) * hitsPerPage < numTotalHits) {
              start = (page - 1) * hitsPerPage;
              break;
            } else {
              System.out.println("No such page");
            }
          }
        }
        if (quit) break;
        end = Math.min(numTotalHits, start + hitsPerPage);
      }
    }
  }
  
  public static BasicStats getBasicStats(IndexReader indexReader, Term myTerm, float queryBoost) throws IOException {
	    String fieldName = myTerm.field();
	    
	    System.out.println("myTerm:" + myTerm.toString());
	    
	    

	    CollectionStatistics collectionStats = new CollectionStatistics(
	            "field",
	            indexReader.maxDoc(),
	            indexReader.getDocCount(fieldName),
	            indexReader.getSumTotalTermFreq(fieldName),
	            indexReader.getSumDocFreq(fieldName)
	            );

	    TermStatistics termStats = new TermStatistics(
	            myTerm.bytes(),
	            indexReader.docFreq(myTerm),
	            indexReader.totalTermFreq(myTerm)
	            );

	    BasicStats myStats = new BasicStats(fieldName, queryBoost);
	    assert collectionStats.sumTotalTermFreq() == -1 || collectionStats.sumTotalTermFreq() >= termStats.totalTermFreq();
	    long numberOfDocuments = collectionStats.maxDoc();

	    long docFreq = termStats.docFreq();
	    long totalTermFreq = termStats.totalTermFreq();

	    if (totalTermFreq == -1) {
	      totalTermFreq = docFreq;
	    }

	    final long numberOfFieldTokens;
	    final float avgFieldLength;

	    long sumTotalTermFreq = collectionStats.sumTotalTermFreq();

	    if (sumTotalTermFreq <= 0) {
	        numberOfFieldTokens = docFreq;
	        avgFieldLength = 1;
	    } else {
	        numberOfFieldTokens = sumTotalTermFreq;
	        avgFieldLength = (float)numberOfFieldTokens / numberOfDocuments;
	    }

	    myStats.setNumberOfDocuments(numberOfDocuments);
	    myStats.setNumberOfFieldTokens(numberOfFieldTokens);
	    myStats.setAvgFieldLength(avgFieldLength);
	    myStats.setDocFreq(docFreq);
	    myStats.setTotalTermFreq(totalTermFreq);
	    
	    System.out.println("myStats.getAvgFieldLength():" + myStats.getAvgFieldLength());
	    System.out.println("myStats.numberOfFieldTokens():" + myStats.getNumberOfFieldTokens());
	    System.out.println("myStats.NumberOfDocuments():" + myStats.getNumberOfDocuments());
	    System.out.println("myStats.docfreq():" + myStats.getDocFreq());
	    System.out.println("myStats.setTotalTermFreq():" + myStats.getTotalTermFreq());

	    return myStats;
	}
  
  public static void highlightResults(IndexReader reader,IndexSearcher searcher, Query query, TopDocs hits, Analyzer analyzer) throws IOException, InvalidTokenOffsetsException{
      
      //Uses HTML &lt;B&gt;&lt;/B&gt; tag to highlight the searched terms
      Formatter formatter = new SimpleHTMLFormatter();
       
      //It scores text fragments by the number of unique query terms found
      //Basically the matching score in layman terms
      QueryScorer scorer = new QueryScorer(query);
       
      //used to markup highlighted terms found in the best sections of a text
      Highlighter highlighter = new Highlighter(formatter, scorer);
       
      //It breaks text up into same-size texts but does not split up spans
      Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 50);
       
      //breaks text up into same-size fragments with no concerns over spotting sentence boundaries.
      //Fragmenter fragmenter = new SimpleFragmenter(10);
       
      //set fragmenter to highlighter
      highlighter.setTextFragmenter(fragmenter);
       
      //Iterate over found results
      for (int i = 0; i < hits.scoreDocs.length; i++)
      {
          int docid = hits.scoreDocs[i].doc;
          Document doc = searcher.doc(docid);
          String title = doc.get("path");
           
          //Printing - to which document result belongs
          System.out.println("Path " + " : " + title);
           
          //Get stored text from found document
          String text = doc.get("contents");
          
          //Create token stream
          TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);
           
          //Get highlighted text fragments
          String[] frags = highlighter.getBestFragments(stream, text, 20);
          for (String f : frags)
          {
              System.out.println("=======================");
              System.out.println(f);
          }
      }
  }
}
