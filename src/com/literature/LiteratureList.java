package com.Assignment1;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

//specify "result" as the root tag in xml
@XmlRootElement(name = "result")
@XmlSeeAlso(
{
    Literature.class
})
public class LiteratureList extends ArrayList<Literature>
{
	private static final long serialVersionUID = 1L;
	//elements under result are named literature 
    @XmlElement(name = "literature")
    //specifies it is a list of literature elements
    public List<Literature> getLiterature()
    {
        return this;
    }
}