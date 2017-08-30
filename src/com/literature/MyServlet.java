package com.Assignment1;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.*;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.*;

/**
 * Servlet implementation class MyServlet
 */
@WebServlet(description = "servlet", urlPatterns = { "/MyServlet" })
public class MyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MyServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    private static final String filePath = "/Users/yatlam/YorkU/4020/"; //declares the path of where your files are located at
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//perform send request to ESearch API everytime we run this servlet
		send();
		//output html on browser
		response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        try{
            DocumentBuilderFactory docFactory =  DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.parse(filePath+"group3_result.xml");
            //read from xml generated, and display its values in table view
            //table headers
            pw.println("<table border=2><tr><th></th><th>DOCNO</th><th>PMID</th><th>Title</th><th>Author</th></tr>");
            //loop through <literature> tag as created in xml
            for (int i=0; i<doc.getElementsByTagName("literature").getLength(); i++){
            	int no = i + 1;
            	Node n = doc.getElementsByTagName("literature").item(i);
            	org.w3c.dom.Element ele = (org.w3c.dom.Element) n;
            	//display row number
            	pw.println("<tr><td>"+no+"</td>");
            	//display docno
            	pw.println("<td width='100'>"+ele.getElementsByTagName("docno").item(0).getTextContent()+"</td>");
            	//display pmid
            	if (ele.getElementsByTagName("pmid").item(0).getTextContent().contains(",")) {
	            	String[] spmid = ele.getElementsByTagName("pmid").item(0).getTextContent().split(",");
	            	//split pmids by delimiter ,
	            	pw.println("<td width='100'>");
	            	for(int j =0; j < spmid.length ; j++) {
	            		if (j == spmid.length-1) {
	            			//no , if last pmid
	            			pw.println(spmid[j]);
	            		} else {
	            			//add linebreak for each pmid
	            			pw.println(spmid[j]+",<br>");
	            		}
	            	}
	            	pw.println("</td>");
            	} else {
            		//if there is only 1 or not found
            		pw.println("<td width='100'>"+ele.getElementsByTagName("pmid").item(0).getTextContent()+"</td>");
            	}
            	//display literature title
            	pw.println("<td width='250'>"+ele.getElementsByTagName("title").item(0).getTextContent()+"</td>");
            	//display author of the literature
            	pw.println("<td width='200'>"+ele.getElementsByTagName("author").item(0).getTextContent()+"</td>"); 
                pw.println("</tr>");
             }
             pw.println("</table>");
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	private static void send()
    {
		String spacer = "          "; //delimiter between literature title and author
		String text, author, title;
		//declare list of literatures
		LiteratureList result = new LiteratureList();
		try {
            //read the file provided as String
			String dataset = FileUtils.readFileToString(new File(filePath+"ITEC4020-A1-dataset.txt"));
            //jsoup library allows us to parse even if the document is not well-formed
			Document doc = Jsoup.parse(dataset);
			//loop through every <DOC></DOC> tag
            for (Element element : doc.getElementsByTag("DOC"))
            {
                //getting the value in tag <DOCNO></DOCNO>
            	String docno = element.getElementsByTag("DOCNO").text();
                //check if the value has "_0." - identify as literature title
            	if (docno.contains("_0."))
                {
                    //java object that defines properties (XML tags)
            		Literature literature = new Literature();
                    //wrap each node with a <pre> tag to indicate it is preformatted
                    element.getElementsByTag("TEXT").wrap("<pre></pre>");
                    //get the value in tag <TEXT></TEXT>
                    text = element.getElementsByTag("TEXT").text();
                    //check if there is delimiter between literature title and author
                    if (text.contains(spacer))
                    {
                        //split title and author
                    	//title is the value before the delimiter
                    	String[] token = text.split(spacer);
                    	title = token[0].trim();
                        //check to see if last character of title is 1
                    	if (title.charAt(title.length()-1)=='1'){
                    		//if so remove the 1 to increase accuracy
                        	title = title.substring(0, title.length()-1);
                        }
                    	//author is the value after the delimiter
                    	author = token[1].trim();
                    }
                    else
                    {
                        //if not, then specify no author
                    	title = text;
                        author = "No Author";
                    }
                    //creating the url for ESearch API
                    //replace all spaces in title with + sign
                    URL url = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&field=title&term=" + title.replaceAll(" ", "+") + "+AND+J+Clin+Endocrinol+Metab[journal]");
                    //using field=title to indicate search by title
                    //Using AND+J+Clin+Endocrinol+Metab[journal] to narrow down searches
                    System.out.println(url);
                    //getting pmid in the xml returned from Pubmed Server 
                    String pmid = retrievePmid(url);
                    //assign values to each literature object
                    literature.setAuthor(author);
					literature.setDocno(docno);
                    literature.setPmid(pmid);
					literature.setTitle(title);
					//add all literature objects to the result list
                    result.add(literature);
                }
            }
            //use the result list to create xml
            //JAXB API is used for XML/Java binding
            JAXBContext context = JAXBContext.newInstance(LiteratureList.class);
            //Marshaller converts Java content trees into XML data
            Marshaller m = context.createMarshaller();
            //format XML data with linebreaks and indentations
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            //writes the result list into an xml file
            m.marshal(result, new File(filePath+"group3_result.xml"));
            System.out.println("created xml file");
        } catch (Exception ex) {
        	
        }
    }
	
	private static String retrievePmid(URL url) throws Exception
    {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        //parsing well-formed xml returned from ESearch response
        org.w3c.dom.Document pubmedDoc = docBuilder.parse(url.openStream());
        //getting the result within the <IdList> tag
        NodeList idList = pubmedDoc.getElementsByTagName("IdList");
        String pmid = null;
        //check if it returns any <IdList>
        if (idList.getLength() > 0) {
	        for (int i = 0; i < idList.getLength(); i++)
	        {
	        	//loop through <IdList> for <id> tags
	        	org.w3c.dom.Element idTag = (org.w3c.dom.Element) idList.item(i);
	            NodeList id = idTag.getElementsByTagName("Id");
	            //check if it returns any <Id>
	            if (id.getLength() > 0)
	            {
	            	//else get first <Id> tag's value
	            	pmid = id.item(0).getTextContent();
	            	//check if it returned more than 1 <Id> tag
	                if (id.getLength() > 1)
		            {
		                for (int count = 1; count < id.getLength(); count++)
		                {
		                    //loop through the <Id> tags and concatenate them
		                	pmid = pmid.concat("," + id.item(count).getTextContent());
		                }
		            }
	            }
	            else
	            {
	            	//if no <Id> tag returned, specify Not Found
	            	pmid = "Not Found";
	                break;
	            }
	        }
        }
        else
        {
        	//if no <IdList> tag returned, specify not found
        	pmid = "Not Found";
        }
        return pmid;
    }
}
