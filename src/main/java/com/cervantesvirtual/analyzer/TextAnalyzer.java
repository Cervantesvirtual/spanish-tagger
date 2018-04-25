package com.cervantesvirtual.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * The Class TextAnalyzer.
 */
public class TextAnalyzer extends Analyzer {
	
    @Override
	protected TokenStreamComponents createComponents(final String fieldName) {

		final StandardTokenizer src = new StandardTokenizer();
		src.setMaxTokenLength(StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
		TokenStream tok = new LowerCaseFilter(src);
		tok = new ASCIIFoldingFilterBVMC(tok);
		
	    return new TokenStreamComponents(src, tok) {
			@Override
			protected void setReader(final Reader reader) {
				src.setMaxTokenLength(StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
				super.setReader(reader);
			}
		};
	}
}
