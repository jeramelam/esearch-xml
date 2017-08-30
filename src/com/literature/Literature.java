package com.Assignment1;

import javax.xml.bind.annotation.XmlType;

//defines properties tag name and sequence for element literature
@XmlType(propOrder = {"docno", "pmid", "title", "author"})
public class Literature
{
    private String docno, pmid, title, author;

    public String getDocno()
    {
        return docno;
    }

    public void setDocno(String docno)
    {
        this.docno = docno;
    }

    public String getPmid()
    {
        return pmid;
    }

    public void setPmid(String pmid)
    {
        this.pmid = pmid;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

}
