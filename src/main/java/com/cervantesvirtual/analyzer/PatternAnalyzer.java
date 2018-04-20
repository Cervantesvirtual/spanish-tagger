package com.cervantesvirtual.analyzer;

import java.io.Reader;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.pattern.SimplePatternTokenizer;

public class PatternAnalyzer extends Analyzer {

@Override
	protected TokenStreamComponents createComponents(String arg0) {
	Pattern pattern = Pattern.compile(" ");
	System.out.println("pattern:" + pattern.pattern());
	
		final SimplePatternTokenizer src = new SimplePatternTokenizer(pattern.pattern());
		//src.setMaxTokenLength(StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
		//TokenStream tok = new StandardFilter(src);
		TokenStream tok = new LowerCaseFilter(src);
		
		return new TokenStreamComponents(src, tok) {
			@Override
			protected void setReader(final Reader reader) {
				super.setReader(reader);
			}
		};
	}
}
