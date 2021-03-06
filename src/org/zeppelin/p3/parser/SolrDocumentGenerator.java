package org.zeppelin.p3.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;

public class SolrDocumentGenerator {

	/**
	 * Creates and returns the Solr Document
	 * 
	 * @return
	 */
	public List<SolrInputDocument> createSolrDocuments() {
		// TODO- Remove the hard-coding
		// String ipDir = "H:\\projects\\newspersonaliztion\\corpus";
		// String ipDir = "/home/animesh/git/project3/corpus/00";
		//String ipDir = "/home/animesh/git/project3/corpus";
		String ipDir = "/Users/aniket/Development/workspace/ub535p3/corpus";
		File ipDirectory = new File(ipDir);
		String[] catDirectories = ipDirectory.list();

		String[] files;
		File dir;
		List<SolrInputDocument> solrInputDocuments = new ArrayList<SolrInputDocument>();
		SaxParserNytHandler handler = new SaxParserNytHandler();
		int count = 1;
		for (String cat : catDirectories) {
			dir = new File(ipDir + File.separator + cat);
			files = dir.list();

			if (files == null)
				continue;

			for (String f : files) {
				Document doc = handler.parseDocument(dir.getAbsolutePath()
						+ File.separator + f);
				SolrInputDocument solrDoc = new SolrInputDocument();
				solrDoc.addField("id", "NYT"+count);
				solrDoc.addField("title", doc.getTitle());
				solrDoc.addField("source", doc.getSource());
				solrDoc.addField("published_date", doc.getPublishedDate());
				solrDoc.addField("content", doc.getContent());
				//solrDoc.addField("name", doc.getContent());
				solrDoc.addField("place", doc.getPlace());
				solrDoc.addField("snippet", doc.getSnippet());
				solrDoc.addField("popularityScore", 0);
				
				if (doc.getCategories() != null) {
					for (String category : doc.getCategories()) {
						solrDoc.addField("category", category, 1.0f);
					}
				}
				// add the doc to the list
				solrInputDocuments.add(solrDoc);
				count++;

			}
			System.out.println(count);
		}
		//System.out.println(count + " documents parsed.");
		return solrInputDocuments;
	}
}
