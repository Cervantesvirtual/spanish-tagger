package com.cervantesvirtual.corpus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class TextExtractor {
	
	private String path;
	private String filename;
	
	public TextExtractor(String path, String filename){
		this.setPath(path);
		this.setFilename(filename);
	}

	public String process() throws IOException{
		String result = "";
		
		File input = new File(path + filename);
		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

		String paragraphs = doc.select("body div0 p").text();
		String headers = doc.select("body div0 head").text();
		
		Files.write(Paths.get("src/main/resources/ff498544-82b1-11df-acc7-002185ce6064.txt"), headers.getBytes(StandardCharsets.UTF_8));
		Files.write(Paths.get("src/main/resources/ff498544-82b1-11df-acc7-002185ce6064.txt"), paragraphs.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

		return result;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public static void main(String[] args){
		TextExtractor extractor = new TextExtractor("src/main/resources/", "ff498544-82b1-11df-acc7-002185ce6064.xml");
		try {
			extractor.process();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
