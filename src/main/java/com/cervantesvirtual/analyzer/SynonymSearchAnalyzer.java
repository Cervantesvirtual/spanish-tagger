package com.cervantesvirtual.analyzer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;

/**
 * The Class ContentAnalyzer.
 */
public class SynonymSearchAnalyzer extends Analyzer {
	
	private String synonymsFile = "src/main/resources/synonyms.txt";

	@Override
	protected TokenStreamComponents createComponents(final String fieldName) {

		//final StandardTokenizer src = new StandardTokenizer();
		final WhitespaceTokenizer src = new WhitespaceTokenizer();
		//src.setMaxTokenLength(StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
		TokenStream tok = new LowerCaseFilter(src);
		tok = new ASCIIFoldingFilterBVMC(tok);
		
		SolrSynonymParser parser = new SolrSynonymParser(true, true, new WhitespaceAnalyzer());
        
        try {
        	byte[] encoded = Files.readAllBytes(Paths.get(synonymsFile));
        	parser.parse(new StringReader(new String(encoded, "UTF-8")));
			
			final SynonymMap map = parser.build();
			tok = new SynonymGraphFilter(tok,map,true);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return new TokenStreamComponents(src, tok) {
			@Override
			protected void setReader(final Reader reader) {
				//src.setMaxTokenLength(StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
				super.setReader(reader);
			}
		};
	}
}
